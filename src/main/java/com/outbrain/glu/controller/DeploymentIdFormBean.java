package com.outbrain.glu.controller;

/**
 *
 * A generic form used to handle specific deployments identified by their ID field
 * (how surprising...)
 * @author Ran Tavory
 *
 */
public class DeploymentIdFormBean extends StatusFormBean {

  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
