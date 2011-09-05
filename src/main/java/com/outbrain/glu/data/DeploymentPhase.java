package com.outbrain.glu.data;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.outbrain.glu.GluExecutionOrder;
import com.outbrain.glu.rpc.GluProgressStatus;

/**
 * Describes one of a few phases of a single deployment.
 *
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="DeploymentPhases")
public class DeploymentPhase {

  private Long id;
  private Deployment deployment;
  private String systemFilter;
  private GluExecutionOrder order = GluExecutionOrder.SEQUENTIAL;
  private String executionId;
  private String planId;
  private GluProgressStatus progress;

  public DeploymentPhase(){}

  public DeploymentPhase(String systemFilter, GluExecutionOrder order) {
    this.systemFilter = systemFilter;
    this.order = order;
  }

  @Override
  public String toString() {
    return String.format("%s %s %s", order, executionId, systemFilter);
  }

  public String getSystemFilter() {
    return systemFilter;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getPlanId() {
    return planId;
  }

  public void setPlanId(String planId) {
    this.planId = planId;
  }

  @OneToOne(cascade = CascadeType.ALL)
  public GluProgressStatus getProgress() {
    return progress;
  }

  public void setProgress(GluProgressStatus status) {
    this.progress = status;
  }

  @Id
  @GeneratedValue
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Enumerated(EnumType.STRING)
  public GluExecutionOrder getExecutionOrder() {
    return order;
  }
  public void setExecutionOrder(GluExecutionOrder order) {
    this.order = order;
  }

  public void setSystemFilter(String systemFilter) {
    this.systemFilter = systemFilter;
  }

  @ManyToOne
  @JoinColumn(nullable=false)
  public Deployment getDeployment() {
    return deployment;
  }

  public void setDeployment(Deployment deployment) {
    this.deployment = deployment;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof DeploymentPhase)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    DeploymentPhase other = (DeploymentPhase) obj;
    return new EqualsBuilder().append(id, other.id).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).toHashCode();
  }
}
