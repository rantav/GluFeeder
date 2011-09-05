package com.outbrain.glu.data;

/**
 * Describes the deployment status.
 *
 * @author Ran Tavory
 *
 */
public enum DeploymentStatus {
  /** Not yet started. The process had just begun */
  NOT_STARTED,
  /** Deployment was successful */
  DONE,
  /** Deployment was successful but the request was for SVN only, so glu's not in the picture */
  SUCCESS_SVN_ONLY,
  /** Currently communicating with SVN */
  WORKING_SVN,
  /** Generic error */
  ERROR,
  /** Deployment has timed out */
  ERROR_TIMEOUT,
  /** Deployment was cancled by the user */
  CANCELED,
  /** Glu is already using the current model. Nothing to do */
  GLU_NOTHING_TO_DO,
  /** Error asking glu to execute a plan */
  GLU_CANT_EXECUTE_PLAN,
  /** Error asking glu to create an execution plan */
  GLU_CANT_CREATE_EXECUTION,
  /** Only glu preperation was requested, and it was successful.*/
  PREPARE_ONLY_DONE,
  /** Waiting for yum to have all the RPMs ready */
  PENDING_YUM,
  /** Glu is currently loading the model */
  GLU_LOADING_MODEL,
  /** Glu is now deploying a first host */
  GLU_DEPLOYING_FIRST_HOST,
  /** Deploying mosts hosts. This is the second or more round of host deployments */
  GLU_DEPLOYING_MORE_HOSTS,
  /** Waited for yum and timedout */
  YUM_TIMEOUT;

  public boolean isError() {
    return equals(ERROR) || equals(ERROR_TIMEOUT) || equals(GLU_CANT_CREATE_EXECUTION)
        || equals(GLU_CANT_EXECUTE_PLAN);
  }
}
