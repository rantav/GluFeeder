package com.outbrain.glu.data;

/**
 * A simple class that holds two numbers, one for success and another for failed counts.
 * It represents the number of successful v/s failed deployments during a time period.
 * @author Ran Tavory
 *
 */
public class SuccessFailCounts {

  private int success, failed;

  public SuccessFailCounts(int successCount, int failedCount) {
    success = successCount;
    failed = failedCount;
  }

  public int getSuccess() {
    return success;
  }

  public void setSuccess(int success) {
    this.success = success;
  }

  public int getFailed() {
    return failed;
  }

  public void setFailed(int failed) {
    this.failed = failed;
  }
}
