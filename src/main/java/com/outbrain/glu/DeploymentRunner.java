package com.outbrain.glu;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.HttpException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.Assert;

import com.outbrain.glu.data.Committer;
import com.outbrain.glu.data.Deployment;
import com.outbrain.glu.data.DeploymentStatus;
import com.outbrain.glu.data.DeploymentsDao;
import com.outbrain.glu.data.Module;
import com.outbrain.glu.model.GluModel;
import com.outbrain.glu.model.GluModelDiff;
import com.outbrain.glu.rpc.GluProgressStatus;
import com.outbrain.glu.rpc.GluProxy;
import com.outbrain.glu.rpc.ModelType;
import com.outbrain.glu.yammer.YammerApi;
import com.outbrain.yummer.Yummer;
import com.outbrain.yummer.YummerRuntimeException;

/**
 * A single deployment runner task which takes care of a deployment from time of request until it
 * ends.
 *
 * @author Ran Tavory
 *
 */
public class DeploymentRunner implements Callable<DeploymentStatus> {

  private static final Logger log = LoggerFactory.getLogger(DeploymentRunner.class);

  private static final float FRACTION_OF_CLUSTER_IN_EACH_ITERATION = 0.33f;

  private static final long YUM_TIMEOUT_MILLISEC = 1 * 60 * 1000; // one minute

  private Deployment deployment;
  private final DeploymentsDao deploymentsDao;
  private final GluProxy glu;
  private final Yummer yummer;
  private final YammerApi yammer;
  private final MailSender mailSender;

  public DeploymentRunner(Deployment deployment, GluProxy proxy, Yummer yummer, YammerApi yammer,
      DeploymentsDao deploymentsDao, MailSender mailSender) {
    Assert.notNull(deployment, "deployment cannot be null");
    Assert.notNull(proxy, "gluProxy can't be null");
    Assert.notNull(yummer, "yummer is null");
    Assert.notNull(yammer, "yammer is null");
    Assert.notNull(deploymentsDao, "deploymentsDao is null");
    Assert.notNull(mailSender, "mailSender is null");
    this.deployment = deployment;
    this.glu = proxy;
    this.yummer = yummer;
    this.yammer = yammer;
    this.deploymentsDao = deploymentsDao;
    this.mailSender = mailSender;
  }

  @Override
  public DeploymentStatus call() throws Exception {
    try {
      boolean ready = waitForYum();
      if (!ready) {
        return deployment.getStatus();
      }
      loadModel();
      if (deployment.isPrepareOnly()) {
        log.info("Glu preperation is done. The request was to prepare only, so I'm done now");
        deployment.endWithStatus(DeploymentStatus.PREPARE_ONLY_DONE);
        deploymentsDao.save(deployment);
        return deployment.getStatus();
      }

      Map<String, Integer> dcServiceCount = getDcServiceCount(deployment.getJsonModel());
      if (deployFirst()) {
        for (int i = 1;
            deployMore(dcServiceCount, FRACTION_OF_CLUSTER_IN_EACH_ITERATION, i);
            ++i) {
        }
      }
      deployment.endWithStatus(DeploymentStatus.DONE);
      deploymentsDao.save(deployment);
    } catch (HttpException e) {
      deployment.setError(e);
      log.error("Error during deployment", e);
    } catch (IOException e) {
      deployment.setError(e);
      log.error("Error during deployment", e);
    } catch (GluException e) {
      deployment.setError(e);
      log.error("Error executing in glu", e);
    } catch (RuntimeException e) {
      deployment.setError(e);
      log.error("Error during deployment", e);
    }

    if (deployment.getStatus().isError()) {
      alertError();
    }
    deploymentsDao.save(deployment);
    return deployment.getStatus();
  }

  private void alertError() {
    if (deployment.isTestOnly()) {
      log.info("Not alerting an error since this is a test only deployment");
      return;
    }
    yammer.postUpdate(deployment.getModulesAsString(), "#fail #fail #fail " + deployment.getTags(),
        deployment.getModulesRevision(), deployment.getCurrentPhaseGluExecutionId(),
        deployment.getCommitter(),
        "#fail #fail #fail Deployment failed: " + deployment.getCommitLogMessage());

    // Also send a regular email to the committer
    if (System.getProperty("noMailServer") == null) {
      mailSender.send(createFailedMessage(deployment.getModulesAsString(),
          "" + deployment.getTags(),
          deployment.getModulesRevision(),
          deployment.getCurrentPhaseGluExecutionId(),
          deployment.getCommitter(),
          deployment.getCommitLogMessage()));
    }
  }

  private SimpleMailMessage createFailedMessage(String modules, String tags, long version,
      String gluExecutionId, Committer committer, String commitMessage) {
    String subject = "Deployment failure #fail #fail #fail";
    String body = String.format("Failed to deploy %s (r %s) to %s. " +
        "%s/%s?showErrorsOnly=false By @%s; Failed: %s",
        modules, version, tags, yammer.getGluExecutionBaseUrl(), gluExecutionId, committer,
        commitMessage);
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(committer.getName() + "@outbrain.com");
    msg.setFrom(yammer.getFromEmailAddress());
    msg.setSubject(subject);
    msg.setText(body);
    return msg;
  }

  /**
   * Counts the number of instances each service has in each DC.
   * The returned map is of DC-SERVICE -> Integer.
   * For example: ny-ImageServer -> 25
   *
   * @param jsonModel The model from which the number of instances are being counted.
   *
   * @return A map from DC-SERVICE to the number of occurrences of this service in the DC.
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  /*package*/ Map<String, Integer> getDcServiceCount(String jsonModel)
      throws JsonParseException, JsonMappingException, IOException {
    GluModel model = new GluModel(jsonModel, ModelType.LIVE);
    return model.getDcServiceCount();
  }

  private void loadModel() throws HttpException, IOException {
    deployment.setStatus(DeploymentStatus.GLU_LOADING_MODEL);
    deploymentsDao.save(deployment);
    String systemId = glu.loadModel(deployment.getJsonModel());
    deployment.setGluSystemId(systemId);
    deploymentsDao.save(deployment);
    if (systemId == null) {
      log.info("The json is already loaded into glu, no new systemId. Will continue anyway. " +
		"Diff is: {}", deployment.getJsonDiff());
    }
  }

  /**
   * Deploys a first host.
   * Deploys one host in one DC.
   * @return true if there are possibly more hosts to deploy
   * @throws GluException
   * @throws IOException
   * @throws HttpException
   */
  private boolean deployFirst() throws GluException, HttpException, IOException {
    log.info("Deploying first host of the service {}", deployment.getModulesAsString());
    deployment.setStatus(DeploymentStatus.GLU_DEPLOYING_FIRST_HOST);
    deploymentsDao.save(deployment);
    log.debug("Querying glu for the current diff....");
    GluModelDiff diff = glu.getModelDiff(deployment.getScope());
    log.debug("Current diff is: {}", diff);
    if (diff.isEmpty()) {
      log.info("No hosts to deploy; Diff is empty");
      return false;
    }

    String systemFilter = diff.getDsl(1, 1);

    executeDeploymentPhase(GluExecutionOrder.SEQUENTIAL, systemFilter);
    return true;
  }

  /**
   * Waits until the given plan execution is finished.
   *
   * @throws GluException if the execution didn't end with success.
   * @throws IOException
   */
  private void poll(String planId, String executionId) throws GluException, IOException {
    GluProgressStatus status;
    for (status = glu.getExecutionStatus(planId, executionId);
        status.getDescription() == GluProgressStatus.Description.IN_PROGRESS; ) {
      deployment.touch();
      deploymentsDao.save(deployment);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        // ignore
      }
      status = glu.getExecutionStatus(planId, executionId);
      deployment.setCurrentPhaseProgress(status);
      deploymentsDao.save(deployment);
    }
    if (status.getDescription() != GluProgressStatus.Description.COMPLETED) {
      throw new GluException(DeploymentStatus.ERROR);
    }
  }

  /**
   * Deploys more hosts.
   * The number of hosts to deployed is given as a fraction of the total number of service
   * instances per each DC.
   * For example of in ny there are 30 ImageServer instances and the fraction is 0.3 then in each
   * invocation of deployMore there would be at most 10 instances deployed in nydc.
   *
   * @param dcServiceCount A map of CD-SERVICE -> Integer. This is the number of instances of each
   * service in each DC, so for example ny-ImageServer -> 5 would mean that the image server has 5
   * instances in ny.
   * @param maxFractionInEachDc The fraction of the number of hosts of each service in each DC that
   * are allowed to be deployed at once.
   * The return value would count the number of still undeplpoyed hosts.
   * @return true if there are (possibly) more hosts to deploy
   * @throws IOException
   * @throws HttpException
   * @throws GluException
   */
  private boolean deployMore(Map<String, Integer> dcServiceCount, float maxFractionInEachDc, int iter)
    throws HttpException, IOException, GluException {
    log.info("Deploying more hosts, iteration {}", iter);
    log.debug("Deploying more hosts, iteration {}. dcServiceCount={}", iter, dcServiceCount);
    deployment.setStatus(DeploymentStatus.GLU_DEPLOYING_MORE_HOSTS);
    deploymentsDao.save(deployment);

    log.debug("Querying glu for the current diff....");
    GluModelDiff diff = glu.getModelDiff(deployment.getScope());
    log.info("Current diff is: {}", diff);
    if (diff.isEmpty()) {
      log.info("No hosts to deploy; I'm done :)");
      return false;
    }

    String systemFilter = diff.getDsl(dcServiceCount, maxFractionInEachDc);

    executeDeploymentPhase(GluExecutionOrder.PARALLEL, systemFilter);
    return true;
  }

  /**
   *
   * @param executionOrder
   * @param systemFilter
   * @throws HttpException
   * @throws IOException
   * @throws GluException
   */
  private void executeDeploymentPhase(GluExecutionOrder executionOrder, String systemFilter)
      throws HttpException, IOException, GluException {
    deployment.addDeploymentPhase(systemFilter, executionOrder);
    deploymentsDao.save(deployment);

    String planId = glu.createExecutionPlan(GluPlanAction.DEPLOY, executionOrder, systemFilter);
    deployment.setCurrentPhaseGluPlanId(planId);
    deploymentsDao.save(deployment);

    if (planId == null) {
      log.error("Unable to create an execution for the system ID {}", deployment.getGluSystemId());
      deployment.endWithStatus(DeploymentStatus.GLU_CANT_CREATE_EXECUTION);
      deploymentsDao.save(deployment);
      throw new GluException(DeploymentStatus.GLU_CANT_CREATE_EXECUTION);
    }
    log.info("Created an execution plan with ID {}. Will execute it now.", planId);
    String executionId = glu.executePlan(planId);
    if (executionId == null) {
      log.error("Unable to execute plan {}", planId);
      deployment.endWithStatus(DeploymentStatus.GLU_CANT_EXECUTE_PLAN);
      deploymentsDao.save(deployment);
      throw new GluException(DeploymentStatus.GLU_CANT_CREATE_EXECUTION);
    }
    log.info("Created an execution {}", executionId);
    deployment.setCurrentPhaseGluExecutionId(executionId);
    deploymentsDao.save(deployment);

    log.info("Glu is now running the plan. Will now wait for it to finish...{}/{}", planId, executionId);
    poll(planId, executionId);
    log.info("Glu has finished running plan {} and execution {}", planId, executionId);
  }

  /**
   * Polls and waits for yum to be ready.
   * Yum checks whether the module exists in all yum repositories and if not then waits
   * (possibly indefinitely) for them.
   * The method blocks until yum is ready.
   *
   * @return true if yum is ready. false if it's not (and it was timed out) or if this deployment
   * was cancled
   */
  private boolean waitForYum() {
    deployment.setStatus(DeploymentStatus.PENDING_YUM);
    deploymentsDao.save(deployment);
    long startTime = System.currentTimeMillis();
    while (true) {
      // Refresh the deployment instance
      deployment = deploymentsDao.element(deployment.getId());
      if (deployment.isCanceled()) {
        log.info("Deployment was cancaled. Will not continue checking yum " + deployment);
        return false;
      }

      long currentTime = System.currentTimeMillis();
      if (currentTime - startTime > YUM_TIMEOUT_MILLISEC) {
        deployment.endWithStatus(DeploymentStatus.YUM_TIMEOUT);
        deploymentsDao.save(deployment);
        log.info("Deployment has timedout over YUM. " + deployment);
        return false;
      }

      deployment.touch();
      deploymentsDao.save(deployment);
      try {
        if (isYumReady(deployment.getModules(), deployment.getModulesRevision(), deployment.getYumRepos())) {
          log.info("Yum is ready for {}-{}", deployment.getModulesAsString(), deployment.getModulesRevision());
          break;
        }
      } catch (IOException e) {
        log.error("Error when talking to yum. Will sleep for 5s and try again", e);
      } catch (InterruptedException e) {
        log.error("Error when talking to yum. Will sleep for 5s and try again", e);
      } catch (YummerRuntimeException e) {
        log.error("Error when talking to yum. Will sleep for 5s and try again", e);
      }
      log.info("Yum isn't ready yet, will sleep for 5 seconds... ({}-{})",
          deployment.getModulesAsString(), deployment.getModulesRevision());
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        log.info("Interrupted while sleeping. That's OK, I'm not mad...");
      }
    }
    return true;
  }

  private boolean isYumReady(Set<Module> modules, long version, Set<String> repos)
      throws IOException, InterruptedException, YummerRuntimeException {
    for (Module module : modules) {
      log.info("Checking whether module {}-{} exists in the repo", module, version);
      if (!yummer.checkModuleExistanceInRepos(module, String.valueOf(version), repos)) {
        log.info("Module {}-{} does't exist in all repos", module, version);
        return false;
      }
      log.info("Module {}-{} Exist in all repos", module, version);
    }
    return true;
  }
}
