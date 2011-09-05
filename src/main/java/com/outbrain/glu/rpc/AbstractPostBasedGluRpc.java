package com.outbrain.glu.rpc;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.util.Assert;

/**
 * Base class for all glu actions.
 *
 * @author Ran Tavory
 *
 */
/*package*/ abstract class AbstractPostBasedGluRpc extends AbstractGluRpc {

  public AbstractPostBasedGluRpc(String gluUrl, String fabricName, HttpClient client,
      PostMethod postMethod, String username, String password) {
    super(gluUrl, fabricName, client, postMethod, username, password);
  }

  protected String post(String url, NameValuePair[] parameters) throws IOException {
    PostMethod post = (PostMethod)method;
    post.setRequestBody(parameters);
    post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
    log.debug("Posting request to {} {}", url, Arrays.toString(parameters));
    return post(url, Arrays.toString(parameters));
  }

  protected String post(String url, String body, String contentType) throws IOException {
    Assert.notNull(body, "body cannot be null");
    Assert.notNull(contentType, "contentType cannot be null");
    PostMethod post = (PostMethod)method;
    post.addRequestHeader("Content-Type", contentType);
    post.setRequestEntity(new StringRequestEntity(body, contentType, "UTF-8"));
    return post(url, body);
  }

  protected String post(String url, String debugData) throws IOException {
    executeMethod(url, debugData);
    if (method.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
      log.info("Glu says that 'no content' for url {}", url);
      return null;
    }
    return method.getResponseBodyAsString();
  }
}
