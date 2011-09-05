package com.outbrain.glu.nagios;

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

import com.outbrain.glu.data.Committer;

/**
 * A proxy for Nagios that sends messages about deployments to it.
 *
 *
 * @author Ran Tavory
 *
 */
public class NagiosProxy {

  private static final Logger log = LoggerFactory.getLogger(NagiosProxy.class);
  private final String notificationUrl, username, password;
  private final HttpClient client;
  private final GetMethod method;
  public NagiosProxy(String notificationUrl, String username, String password, HttpClient client,
      GetMethod method) {
    Assert.notNull(notificationUrl, "notificationUrl is null");
    Assert.notNull(username, "username is null");
    Assert.notNull(password, "password is null");
    Assert.notNull(client, "client is null");
    Assert.notNull(method, "method is null");
    this.notificationUrl = notificationUrl;
    this.username = username;
    this.password = password;
    this.client = client;
    this.method = method;
  }

  private void setupAuthentication() {
    client.getState().setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(username, password));
    client.getParams().setAuthenticationPreemptive(true);
    method.setDoAuthentication(true);
  }


  /**
   * Tells Nagios about a release.
   *
   * Sends a get request to something such as this:
   * https://nagios/nagios/cgi-bin/glu-deployment.pl?username=ran&rev=12345
   *
   * @param committer The committer of the release
   * @param revision SVN revision of this release.
   * @throws IOException
   */
  public void tellNagios(Committer committer, String revision) throws IOException {
    log.info("Telling nagios about a deployment from {}, revision {}", committer, revision);
    method.setURI(new URI(notificationUrl,true));
    method.setQueryString(new NameValuePair[] { new NameValuePair("username", committer.toString()),
                                               new NameValuePair("rev", revision) });
    setupAuthentication();
    int status = client.executeMethod(method);
    if (status != 200) {
      String msg = "Nagios request failed with status " + status;
      log.error(msg);
      throw new HttpException(msg);
    }
  }
}
