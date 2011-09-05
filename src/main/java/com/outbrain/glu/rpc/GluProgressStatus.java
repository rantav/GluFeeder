package com.outbrain.glu.rpc;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Holds the status of an execution
 *
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="GluProgressStatuss")
public class GluProgressStatus {

  public enum Description {
    IN_PROGRESS, FAILED, COMPLETED, PARTIAL
  }

  @Id
  @GeneratedValue
  private Long id;

  /** 0 - 100 */
  private int percentProgress;

  @Enumerated(EnumType.STRING)
  private Description description;


  public GluProgressStatus(){}

  public GluProgressStatus(Description description, int percentProgress) {
    this.description = description;
    this.percentProgress = percentProgress;
  }

  public int getPercentProgress() {
    return percentProgress;
  }

  public void setPercentProgress(int percentProgress) {
    this.percentProgress = percentProgress;
  }

  public Description getDescription() {
    return description;
  }

  public void setDescription(Description description) {
    this.description = description;
  }

  @Override
  public String toString() {
    if (description.equals(Description.IN_PROGRESS)) {
      return String.valueOf(percentProgress) + "%";
    }
    return description.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GluProgressStatus)) {
      return false;
    }
    GluProgressStatus other = (GluProgressStatus)obj;
    return other.description.equals(description) && other.percentProgress == percentProgress;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
