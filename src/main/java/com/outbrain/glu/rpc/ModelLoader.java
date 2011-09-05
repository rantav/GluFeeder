package com.outbrain.glu.rpc;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Loads a new model to glu
 *
 * @author Ran Tavory
 *
 */
public class ModelLoader extends AbstractPostBasedGluRpc {

  public ModelLoader(String gluUrl, String fabricName, HttpClient client, PostMethod postMethod,
      String username, String password) {
    super(gluUrl, fabricName, client, postMethod, username, password);
  }

  private final String SYSTEM_MODEL = "/system/model";
  private final Pattern ID_PATTERN = Pattern.compile("id=(.*)");

  /**
   * Loads the jsonModel string into glu.
   * <p>
   * To post an update to glu we issue a POST request to
   * http://glu/console/rest/v1/outbrain/system/model The body of the request should
   * contain the json model. See https://github.com/linkedin/glu/wiki/Console
   *
   * @param jsonModel
   * @throws IOException
   * @throws HttpException
   * @return modelId the ID of the newly created model. null if the model was
   *         already up to date
   */
  public String loadModel(String jsonModel) throws HttpException, IOException {
    String responseText = post(getPostModelUrl(), jsonModel, "text/json");
    String modelId = extractSystemId(responseText);
    if (modelId == null) {
      log.warn("The modelId is null. This is normal only when there are no diffs to the current model");
    } else {
      log.info("Model loaded successfuly to glu. The new model ID is {}", modelId);
    }
    return modelId;
  }

  private String extractSystemId(String responseText) {
    if (responseText == null) {
      return null;
    }
    Matcher m = ID_PATTERN.matcher(responseText);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  private String getPostModelUrl() {
    return gluUrl + GLU_API_BASE + fabric + SYSTEM_MODEL;
  }

}
