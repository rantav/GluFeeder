package com.outbrain.glu.rpc;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.outbrain.glu.GluExecutionOrder;
import com.outbrain.glu.GluPlanAction;

/**
 * Creates an execution plan.
 *
 * @author Ran Tavory
 *
 */
public class ExecutionPlanCreator extends AbstractPostBasedGluRpc {

  public ExecutionPlanCreator(String gluUrl, String fabricName, HttpClient client,
      PostMethod postMethod, String username, String password) {
    super(gluUrl, fabricName, client, postMethod, username, password);
  }

  /**
   * Creates an execution plan.
   * <p>
   *
   * @param planAction
   * @param order
   * @param systemFilter
   * @return the ID of the plan. Use this ID to call executePlan.
   */
  public String createExecutionPlan(GluPlanAction planAction, GluExecutionOrder order,
      String systemFilter) throws HttpException, IOException {
    log.info("Creating execution plan {} {}", order, systemFilter);
    String responseText = post(getCreateExecutePlanUrl(),
        createExecutionPostParameters(planAction, order, systemFilter));
    String executionId = extractExecutionPlanId(responseText);
    if (executionId == null) {
      log.error("executionId is null when trying to create a plan");
    } else {
      log.info("Execution plan is ready for execution. Plan ID is {}", executionId);
    }
    return executionId;
  }

  private NameValuePair[] createExecutionPostParameters(GluPlanAction planAction,
      GluExecutionOrder order,
      String systemFilter) {
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    if (planAction != null) {
      params.add(new NameValuePair("planAction", planAction.toString().toLowerCase()));
    }
    if (order != null) {
      params.add(new NameValuePair("order", order.toString().toLowerCase()));
    }
    if (systemFilter != null) {
      params.add(new NameValuePair("systemFilter", systemFilter));
    }
    return params.toArray(new NameValuePair[0]);
  }

  private String extractExecutionPlanId(String text) {
    if (text == null) {
      return null;
    }
    return text.trim();
  }


  private String getCreateExecutePlanUrl() {
    return gluUrl + GLU_API_BASE + fabric + "/plans";
  }
}
