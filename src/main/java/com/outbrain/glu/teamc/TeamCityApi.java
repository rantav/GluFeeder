package com.outbrain.glu.teamc;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * A thin layer of API to teamcity
 *
 * @author Ran Tavory
 *
 */
public class TeamCityApi {

  private static final Logger log = LoggerFactory.getLogger(TeamCityApi.class);

  private final String TRIGGER_BUILD_PATH = "/httpAuth/action.html";

  private final String userinfo;
  private final String host;
  private final int port;
  private final String buildTriggerId;
  protected final HttpClient client;
  protected final GetMethod method;

  public TeamCityApi(String host, int port, String usernamePassword, String buildTriggerId,
      HttpClient client, GetMethod method) {
    Assert.notNull(host, "host is null");
    Assert.notNull(port, "port is null");
    Assert.notNull(usernamePassword, "usernamePassword is null");
    Assert.notNull(buildTriggerId, "buildTriggerId is null");
    Assert.notNull(client, "client is null");
    Assert.notNull(method, "methos is null");
    this.host = host;
    this.port = port;
    this.userinfo = usernamePassword;
    this.buildTriggerId = buildTriggerId;
    this.client = client;
    this.method = method;
  }

  public void build(String modules, String tags, String committer, String message, boolean prepareOnly)
      throws NullPointerException, HttpException, IOException {
    method.setURI(new URI("http", userinfo, host, port, TRIGGER_BUILD_PATH));
    method.setPath(TRIGGER_BUILD_PATH);
    method.setQueryString(createBuildParams(modules, tags, committer, message, prepareOnly));
    setupAuthentication();
    int status = client.executeMethod(method);
    if (status != 200) {
      String msg = "TeamCity request failed with status " + status;
      log.error(msg);
      throw new HttpException(message);
    }
  }

  private void setupAuthentication() {
    String[] split = userinfo.split(":");
    String username = split[0];
    String password = split[1];
    client.getState().setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(username, password));
    client.getParams().setAuthenticationPreemptive(true);
    method.setDoAuthentication(true);
  }

  private NameValuePair[] createBuildParams(String modules, String tags, String committer,
      String message, boolean prepareOnly) {
    return new NameValuePair[] {
      new NameValuePair("add2Queue", buildTriggerId),
      new NameValuePair("system.name", "ob.module"),
      new NameValuePair("system.value", modules),
      new NameValuePair("system.name", "ob.tags"),
      new NameValuePair("system.value", tags),
      new NameValuePair("system.name", "commitRevision"),
      new NameValuePair("system.value", "latest"),
      new NameValuePair("system.name", "committer"),
      new NameValuePair("system.value", committer),
      new NameValuePair("system.name", "commitMessage"),
      new NameValuePair("system.value", message),
      new NameValuePair("system.name", "prepareOnly"),
      new NameValuePair("system.value", prepareOnly ? "on" : "off"),
      new NameValuePair("moveToTop", "true")
    };
  }

  public String getServerUrl() {
    return "http://" + host + ":" + port;
  }
}
