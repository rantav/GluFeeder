package com.outbrain.glu.rpc;


import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

import com.outbrain.glu.rpc.GluProgressStatus.Description;

/**
 * Reads the status of an execution.
 * Uses the HEAD method described in Glu's api at https://github.com/linkedin/glu/wiki/Console
 *
  <pre>
  HEAD /plan/<planId>/execution/<executionId>
  </pre>
 * @author Ran Tavory
 *
 */
public class ExecutionStatusReader extends AbstractGluRpc {

  private static final String COMPLETION_HEADER = "X-LinkedIn-GLU-Completion";

  public ExecutionStatusReader(String gluUrl, String fabricName, HttpClient client,
      HeadMethod headMethod,
      String username, String password) {
    super(gluUrl, fabricName, client, headMethod, username, password);
  }

  /**
   * Checks the status of an executing plan.
   *
   * @param executionPlanId
   * @return A number between 0-100 which represents the percentage of the plan's completion.
   * @throws IOException
   */
  public GluProgressStatus checkExecutionStatus(String planId, String executionId) throws IOException {
    Header[] headers = head(getExecutionStatusUrl(planId, executionId));
    GluProgressStatus completionStatus = extractCompletionStatus(headers);
    if (completionStatus.getDescription().equals(Description.IN_PROGRESS)) {
      log.debug("Execution status for {}/{}: {}", new Object[]{planId, executionId, completionStatus});
    } else {
      log.info("Execution status for {}/{}: {}", new Object[]{planId, executionId, completionStatus});
    }
    return completionStatus;
  }

  private GluProgressStatus extractCompletionStatus(Header[] headers) {
    for (Header h: headers) {
      if (COMPLETION_HEADER.equalsIgnoreCase(h.getName())) {
        String value = h.getValue();
        String[] split = value.split(":");
        if (split.length == 1) {
          // Just a number, e.g.
          // X-LinkedIn-GLU-Completion: 55
          return new GluProgressStatus(Description.IN_PROGRESS, Integer.valueOf(value));
        } else {
          // A number with a status, e.g.
          // X-LinkedIn-GLU-Completion: 100:COMPLETED
          int perscents = Integer.valueOf(split[0]);
          Description description = Description.valueOf(split[1]);
          return new GluProgressStatus(description, perscents);
        }
      }
    }
    throw new IllegalArgumentException("Headers do not contain " + COMPLETION_HEADER +
        ". But they should...");
  }

  protected Header[] head(String url) throws IOException {
    executeMethod(url, "HEAD status");
    return method.getResponseHeaders();
  }

  private String getExecutionStatusUrl(String planId, String executionId) {
    return gluUrl + GLU_API_BASE + fabric + "/plan/" + planId + "/execution/" + executionId;
  }

  public static void main(String[] args) throws IOException {
    ExecutionStatusReader reader = new ExecutionStatusReader("http://glu.outbrain.com:8080/console/",
        "outbrain", new HttpClient(), new HeadMethod(), "api", "api");
    System.out.println(reader.checkExecutionStatus(args[0], args[1]));
  }
}
