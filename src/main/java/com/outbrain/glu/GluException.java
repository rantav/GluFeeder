package com.outbrain.glu;

import com.outbrain.glu.data.DeploymentStatus;

@SuppressWarnings("serial")
public class GluException extends Exception {

  public GluException(DeploymentStatus status) {
    super(status.toString());
  }
}
