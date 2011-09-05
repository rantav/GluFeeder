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
 * A Module is glu jargon for Service, such as an ImageServer
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="Modules")
public class Module {

  @Id
  private String name = "";

  @ManyToMany(fetch = FetchType.EAGER)
  @Sort(type = SortType.NATURAL)
  private SortedSet<Deployment> deployments = new TreeSet<Deployment>();

  public Module(){}

  public Module(String s) {
    name = s;
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

  public void addDeployment(Deployment d) {
    if (deployments instanceof PersistentSet && ((PersistentSet) deployments).wasInitialized()) {
      deployments.add(d);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Module)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    Module other = (Module) obj;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  public static Set<Module> fromString(String modulesString) {
    Set<String> split = GluStringUtils.splitTrim(modulesString);
    Set<Module> modules = new HashSet<Module>(split.size());
    for (String s : split) {
      Module m = new Module(s);
      modules.add(m);
    }
    return modules;
  }

  public void removeDeployment(Deployment d) {
    rebuildDeploymentsHashCodeCache();
    deployments.remove(d);
  }

  private void rebuildDeploymentsHashCodeCache() {
    Set<Deployment> ds = new HashSet<Deployment>();
    ds.addAll(deployments);
    deployments.clear();
    deployments.addAll(ds);
  }

}
