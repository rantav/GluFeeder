package com.outbrain.glu.controller;

/**
 * Form submitted to the deployer.
 *
 * @author Ran Tavory
 */
public class DeploymentFormBean {

  private String serviceName;
  private String tags;
  private long version;
  private boolean submitToGlu;
  private boolean prepareOnly = false;
  private String source;
  private String committer = "DontKnow (UI)";
  private String commitRevision = "unknown";
  private String commitMessage = "DontKnow (UI)";

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public boolean isSubmitToGlu() {
    return submitToGlu;
  }

  public void setSubmitToGlu(boolean submitToGlu) {
    this.submitToGlu = submitToGlu;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getCommitter() {
    return committer;
  }

  public void setCommitter(String committer) {
    if (committer == null || committer.isEmpty()) {
      this.committer = "Don't know";
    } else {
      this.committer = committer;
    }
  }

  public String getCommitRevision() {
    return commitRevision;
  }
  public long getCommitRevisionLong() {
    try {
      return Long.valueOf(commitRevision);
    } catch (NumberFormatException e) {
      return 0;
    }

  }
  public void setCommitRevision(String commitRevision) {
    this.commitRevision = commitRevision;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public boolean isPrepareOnly() {
    return prepareOnly;
  }

  public void setPrepareOnly(boolean prepareOnly) {
    this.prepareOnly = prepareOnly;
  }
}
