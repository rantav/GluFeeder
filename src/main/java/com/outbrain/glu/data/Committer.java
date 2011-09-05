package com.outbrain.glu.data;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.collection.PersistentSet;

/**
 * Describes a committer - a person that commits code (or sometimes a role account such as svnsync)
 *
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="Committers")
public class Committer {

  @Id
  private String name = "";

  @OneToMany(mappedBy="committer", fetch = FetchType.EAGER)
  @Sort(type = SortType.NATURAL)
  private SortedSet<Deployment> deployments = new TreeSet<Deployment>();

  public Committer(){}

  public Committer(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public SortedSet<Deployment> getDeployments() {
    return deployments;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDeployments(SortedSet<Deployment> deployments) {
    this.deployments = deployments;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Committer)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    Committer other = (Committer) obj;
    return new EqualsBuilder().append(name, other.name).isEquals();
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  public void addDeployment(Deployment deployment) {
    if (deployments instanceof PersistentSet && ((PersistentSet) deployments).wasInitialized()) {
      deployments.add(deployment);
    }
  }

  public boolean removeDeployment(Deployment deployment) {
    rebuildDeploymentsHashCodeCache();
    return deployments.remove(deployment);
  }

  private void rebuildDeploymentsHashCodeCache() {
    Set<Deployment> ds = new HashSet<Deployment>();
    ds.addAll(deployments);
    deployments.clear();
    deployments.addAll(ds);
  }
}
