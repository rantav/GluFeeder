package com.outbrain.glu.data;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.collection.PersistentSet;

import com.outbrain.glu.model.GluStringUtils;

/**
 * Represents a glu cluster, such as test, ny, la etc
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="Clusters")
public class Cluster {

  @SuppressWarnings("serial")
  public static Set<String> TEST_CLUSTERS = new HashSet<String>(){{
    add("test");
  }};

  @Id
  private String name = "";

  @ManyToMany(fetch = FetchType.EAGER)
  @Sort(type = SortType.NATURAL)
  private SortedSet<Deployment> deployments = new TreeSet<Deployment>();

  public Cluster() {}

  public Cluster(String s) {
    name = s;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SortedSet<Deployment> getDeployments() {
    return deployments;
  }

  public void setDeployments(SortedSet<Deployment> deployments) {
    this.deployments = deployments;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Cluster)) {
      return false;
    }
    return name.equals(((Cluster)obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public void addDeployment(Deployment deployment) {
    if (deployments instanceof PersistentSet && ((PersistentSet) deployments).wasInitialized()) {
      deployments.add(deployment);
    }
  }

  public void removeDeployment(Deployment deployment) {
    rebuildDeploymentsHashCodeCache();
    deployments.remove(deployment);
  }

  public static Set<Cluster> fromString(String clustersList) {
    Set<String> split = GluStringUtils.splitTrim(clustersList);
    Set<Cluster> clusters = new HashSet<Cluster>(split.size());
    for (String s : split) {
      Cluster c = new Cluster(s);
      clusters.add(c);
    }
    return clusters;
  }

  public static Set<Cluster> fromStringSet(Set<String> clusters) {
    Set<Cluster> ret = new HashSet<Cluster>(clusters.size());
    for (String s : clusters) {
      Cluster t = new Cluster(s);
      ret.add(t);
    }
    return ret;
  }

  @Transient
  public boolean isProduction() {
    return isProduction(getName());
  }

  public static boolean isProduction(String clusterName) {
    return !TEST_CLUSTERS.contains(clusterName);
  }

  private void rebuildDeploymentsHashCodeCache() {
    Set<Deployment> ds = new HashSet<Deployment>();
    ds.addAll(deployments);
    deployments.clear();
    deployments.addAll(ds);
  }

}
