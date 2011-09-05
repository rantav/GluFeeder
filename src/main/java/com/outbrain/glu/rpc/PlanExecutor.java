package com.outbrain.glu.rpc;


import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Executes a plan.
 *
 * @author Ran Tavory
 *
 */
public class PlanExecutor extends AbstractPostBasedGluRpc {

  public PlanExecutor(String gluUrl, String fabricName, HttpClient client, PostMethod postMethod,
      String username, String password) {
    super(gluUrl, fabricName, client, postMethod, username, password);
  }

  /**
   * Executes a plan on glu.
   * <p>
   * The planId should be retrieved by invoking {@see #loadModel(String)}.
   * <p>
   * The returned executionId can be used to track the execution of the plan.
   *
   * @param executionPlanId
   * @return the executionId. This ID can be used to track execution progress.
   * @throws IOException
   */
  public String executePlan(String executionPlanId) throws IOException {
    String responseText = post(getExecutePlanUrl(executionPlanId), "executePlan()");
    String executionId = extractExecutionId(responseText);
    log.info("Plan {} is executing now.", executionId);
    return executionId;
  }

  private String extractExecutionId(String text) {
    if (text == null) {
      return null;
    }
    return text.trim();
  }

  private String getExecutePlanUrl(String planId) {
    return gluUrl + GLU_API_BASE + fabric + "/plan/" + planId + "/execution";
  }
}
