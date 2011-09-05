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

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.collection.PersistentSet;

import com.outbrain.glu.model.GluStringUtils;

/**
 * A tag is a glu tag, such as ny, stg etc.
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="Tags")
public class Tag {

  @Id
  private String name = "";

  @ManyToMany(fetch = FetchType.EAGER)
  @Sort(type = SortType.NATURAL)
  private SortedSet<Deployment> deployments = new TreeSet<Deployment>();

  public static Set<Tag> fromString(String tagsList) {
    Set<String> split = GluStringUtils.splitTrim(tagsList);
    Set<Tag> tags = new HashSet<Tag>(split.size());
    for (String s : split) {
      Tag t = new Tag(s);
      tags.add(t);
    }
    return tags;
  }

  public static Set<Tag> fromStringSet(Set<String> stringTagSet) {
    Set<Tag> ret = new HashSet<Tag>(stringTagSet.size());
    for (String s : stringTagSet) {
      Tag t = new Tag(s);
      ret.add(t);
    }
    return ret;
  }

  public Tag() {}

  public Tag(String s) {
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
    if (obj == null || !(obj instanceof Tag)) {
      return false;
    }
    return name.equals(((Tag)obj).name);
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

  private void rebuildDeploymentsHashCodeCache() {
    Set<Deployment> ds = new HashSet<Deployment>();
    ds.addAll(deployments);
    deployments.clear();
    deployments.addAll(ds);
  }

}
