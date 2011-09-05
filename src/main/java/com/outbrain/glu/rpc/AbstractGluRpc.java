package com.outbrain.glu.rpc;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author ran
 *
 */
/*package*/ class AbstractGluRpc {

  protected final String GLU_API_BASE = "rest/v1/";
  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final String gluUrl;
  protected final String fabric;
  protected final HttpClient client;
  protected final HttpMethod method;

    public AbstractGluRpc(String gluUrl, String fabricName, HttpClient client, HttpMethod method,
        String username, String password) {
    Assert.notNull(gluUrl, "gluUrl is null");
    Assert.notNull(fabricName, "fabric is null");
    Assert.notNull(client, "http client is null");
    Assert.notNull(method, "method is null");
    this.gluUrl = gluUrl;
    this.fabric = fabricName;
    this.client = client;
    this.method = method;
    setupAuthentication(username, password);
  }

  public String getGluUrl() {
    return gluUrl;
  }

  protected void setupAuthentication(String username, String password) {
    client.getState().setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(username, password));
    client.getParams().setAuthenticationPreemptive(true);
    method.setDoAuthentication(true);
  }

  /**
   * Executes the HTTP method on the HTTP client.
   * @param url
   * @throws HttpException
   * @throws IOException
   */
  protected void executeMethod(String url, String debugData) throws HttpException, IOException {
    method.setURI(new URI(url, true));
    log.debug("Sending a {} request to glu to {}", method.getClass().getSimpleName(), url);
    int status;
    String responseText;
    Header[] responseHeaders;
    try {
      status = client.executeMethod(method);
      responseText = method.getResponseBodyAsString();
      responseHeaders = method.getResponseHeaders();
    } finally {
      method.releaseConnection();
    }
    log.debug("Request returned with status {} and headers {}", status,
        Arrays.toString(method.getResponseHeaders()));
    if (status < 200 || status >= 300) {
      String message = "Glu returned an error for the request HEAD " + url + "\n" +
          "Debug Data: " + debugData + "\n" +
          "Status code: " + status + "\n" + "Response headers: " +
          Arrays.toString(responseHeaders) + "\nResponse Text: " + responseText;
      log.error(message);
      throw new HttpException(message);
    }
  }
}