package com.outbrain.glu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.util.Assert;
import org.tmatesoft.svn.core.SVNException;

import com.outbrain.glu.data.Committer;
import com.outbrain.glu.data.Deployment;
import com.outbrain.glu.data.DeploymentStatus;
import com.outbrain.glu.data.DeploymentsDao;
import com.outbrain.glu.data.Module;
import com.outbrain.glu.data.Tag;
import com.outbrain.glu.model.GluEntry;
import com.outbrain.glu.model.GluModel;
import com.outbrain.glu.model.GluModelFilter;
import com.outbrain.glu.nagios.NagiosProxy;
import com.outbrain.glu.rpc.GluProxy;
import com.outbrain.glu.rpc.ModelType;
import com.outbrain.glu.svn.SVNUpdater;
import com.outbrain.glu.svn.SVNUpdaterFactory;
import com.outbrain.glu.yammer.YammerApi;
import com.outbrain.yummer.Yummer;
import com.outbrain.yummer.YummerRuntimeException;

/**
 *
 * Reads a json deployment descriptor, updates it and commits it to glu and to scm.
 *
 * This class is not thread safe.
 *
 * @author Ran Tavory
 * @author Itai
 *
 */
public class DeploymentUpdater {

  private static final Logger log = LoggerFactory.getLogger(DeploymentUpdater.class);
  private final ExecutorService pool;
  private final String path;
  private String json;
  private String jsonDiff;
  /** A model based reflection of the string jsonDiff member */
  private Set<GluEntry> diffEntries;
  private final SVNUpdaterFactory svnUpdaterFactory;
  private final GluProxy gluProxy;
  private final Yummer yummer;
  private final NagiosProxy nagios;
  private final Set<String> yumRepos;
  private final YammerApi yammer;
  private final DeploymentsDao deploymentsDao;
  private final MailSender mailSender;

  public DeploymentUpdater(SVNUpdaterFactory svnUpdaterFactory, String path, GluProxy gluProxy,
      Yummer yummer, Set<String> yumRepos, YammerApi yammer, ExecutorService pool,
      NagiosProxy nagios, DeploymentsDao deploymentsDao,  MailSender mailSender) {
    Assert.notNull(svnUpdaterFactory, "svnUpdaterFactory is null");
    Assert.notNull(path, "path is null");
    Assert.notNull(gluProxy, "gluProxy is null");
    Assert.notNull(yummer, "yummer is null");
    Assert.notNull(yumRepos, "yumRepos are null");
    Assert.notNull(yammer, "yammer is null");
    Assert.notNull(pool, "pool executor service is null");
    Assert.notNull(nagios, "nagios is null");
    Assert.notNull(deploymentsDao, "deploymentsDao is null");
    Assert.notNull(mailSender, "mailServer is null");
    this.svnUpdaterFactory = svnUpdaterFactory;
    this.path = path;
    this.gluProxy = gluProxy;
    this.yummer = yummer;
    this.yumRepos = yumRepos;
    this.yammer = yammer;
    this.pool = pool;
    this.nagios = nagios;
    this.deploymentsDao = deploymentsDao;
    this.mailSender = mailSender;
  }

  /**
   * Initializes the class by reading the json model document from svn.
   *
   * @throws SVNException
   */
  public void init() throws SVNException {
    readFromScm();
  }

  public long commitToScm(String username, String message) throws SVNException{
    SVNUpdater svnUpdater = svnUpdaterFactory.createUpdater(path);
    // remove all hashtags to prevent feedback loops
    message = message.replace("#", "");
    long revision = svnUpdater.commit(json, username, message);
    return revision;
  }

  private void readFromScm() throws SVNException {
    SVNUpdater svnUpdater = svnUpdaterFactory.createUpdater(path);
    json = svnUpdater.getFileContent();
  }

  /**
   * Applies a transformation to the json document.
   * This method changes the internal saved representation of the model. It does not commit it to
   * glu or to scm, just changes a local instance.
   * @param serviceName
   * @param tags a list of coma delimited tags
   * @param version
   * @param svnUpdater
   * @return the diff changes
   * @throws SVNException
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  public String applyTransformation(String serviceName, String tags, long version)
      throws SVNException, JsonParseException, JsonMappingException, IOException {
    Assert.notNull(json, "Json document is null, can't apply transformation");
    GluModel tp = new GluModel(json, ModelType.DESIRED);
    Set<GluEntry> diffs = tp.update(
        new GluModelFilter(Module.fromString(serviceName), Tag.fromString(tags)), version);
    json = tp.prettyPrint();
    jsonDiff = toJson(diffs);
    diffEntries = diffs;
    return jsonDiff;
  }

  private String toJson(Set<GluEntry> diffs) throws JsonGenerationException, JsonMappingException, IOException{
    ObjectMapper m = new ObjectMapper();
    ArrayNode res = new ArrayNode(m.getNodeFactory());
    for (GluEntry service : diffs){
      res.add(service.getJsonNode());
    }
    return prettyPrint(m, res);
  }

  private String prettyPrint(ObjectMapper m, JsonNode jsonObj) throws JsonGenerationException, JsonMappingException, IOException{
    m.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    m.writeValue(stream, jsonObj);
    return new String(stream.toByteArray());
  }

  public String getGluUrl() {
    return gluProxy.getUrl();
  }

  /**
   * Submits a deployment request to glu and returns right away.
   *
   * @param context
   * @return true if submitted successfully.
   * @throws HttpException
   * @throws IOException
   * @throws InterruptedException
   * @throws YummerRuntimeException
   */
  public Future<DeploymentStatus> submitToGlu(Deployment context) throws HttpException, IOException,
      InterruptedException, YummerRuntimeException {
    context.setYumRepos(yumRepos);
    context.setJsonModel(json);

    DeploymentRunner runner = new DeploymentRunner(context, gluProxy, yummer, yammer, deploymentsDao,
        mailSender);
    Future<DeploymentStatus> future = pool.submit(runner);
    log.info("Committed to glu. Current status: {}", context.getStatus());
    return future;
  }

  /**
   * Schedules an execution for later.
   */
  public String getJsonModel() {
    return json;
  }

  /**
   * Tells yammer about this release
   * @param gluExecutionId
   * @throws IOException
   */
  public void tellYammerAndNagios(String modules, String tags, long version, String gluExecutionId,
      Committer committer, String commitMessage) throws IOException {
    if (!anyDiffEntryIsProduction()) {
      log.info("The deployment is only for test. Will not tell yammer about this. Tags: {}", tags);
      return;
    }
    yammer.postUpdate(modules, tags, version, gluExecutionId, committer, commitMessage);
    nagios.tellNagios(committer, String.valueOf(version));
  }

  /**
   * Checks whether any of the diff entries is a production cluster entry
   * @return
   */
  private boolean anyDiffEntryIsProduction() {
    if (diffEntries == null) {
      return true;
    }
    for (GluEntry entry : diffEntries) {
      if (entry.isProduction()) {
        return false; // at least one production entry
      }
    }
    return true;
  }

  public String getJsonDiff() {
    return jsonDiff;
  }

  public Set<String> getClusters() {
    Set<String> ret = new HashSet<String>();
    for (GluEntry entry : diffEntries) {
      ret.add(entry.getCluster());
    }
    return ret;
  }
}
