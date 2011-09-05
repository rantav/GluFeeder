package com.outbrain.glu.rpc;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.outbrain.glu.model.GluModel;

/**
 * Get the model as seen by glu.
 * Can either get the desired model or the current model.
 *
 * @author Ran Tavory
 *
 */
public class GetModel extends AbstractGluRpc {


  public GetModel(String gluUrl, String fabricName, HttpClient client, GetMethod getMethod,
      String username, String password) {
    super(gluUrl, fabricName, client, getMethod, username, password);
  }

  public GluModel getModel(ModelType type) throws HttpException, IOException {
    String modelStr = getModelString(type);
    log.debug("{} Model is: {}", type, modelStr);
    return new GluModel(modelStr, type);
  }

  private String getModelString(ModelType type) throws HttpException, IOException {
    executeMethod(getModelUrl(type), "Getting model " + type);
    return method.getResponseBodyAsString();
  }

  private String getModelUrl(ModelType type) {
    return gluUrl + GLU_API_BASE + fabric + "/system/" + type.getUrlPart();
  }
}
