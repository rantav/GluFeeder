package com.outbrain.glu.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a pair of min and max version
 *
 * @author Ran Tavory
 *
 */
public class MaxMinVersion {
  public static String GLU_SERVER;
  private int min, max;
  private String module;
  private Set<String> minAgents = new HashSet<String>();
  private Set<String> maxAgents = new HashSet<String>();

  public MaxMinVersion(String module, int min, int max, Set<String> minAgents, Set<String> maxAgents) {
    this.module = module;
    this.min = min;
    this.max = max;
    this.minAgents = new HashSet<String>(minAgents);
    this.maxAgents = new HashSet<String>(maxAgents);
  }

  public int getMin() {
    return min;
  }
  public void setMin(int min) {
    this.min = min;
  }
  public int getMax() {
    return max;
  }
  public void setMax(int max) {
    this.max = max;
  }
  public String getModule() {
    return module;
  }
  public void setModule(String module) {
    this.module = module;
  }
  public Set<String> getMinAgents() {
    return minAgents;
  }
  public String getMinAgentsHtml() {
    return agentsToHtml(minAgents);
  }
  public void setMinAgents(Set<String> minAgents) {
    this.minAgents = minAgents;
  }
  public Set<String> getMaxAgents() {
    return maxAgents;
  }
  public String getMaxAgentsHtml() {
    if (min == max) {
      return "SAME";
    }
    return agentsToHtml(maxAgents);
  }
  public void setMaxAgents(Set<String> maxAgents) {
    this.maxAgents = maxAgents;
  }
  private String agentsToHtml(Set<String> agents) {
    StringBuffer sb = new StringBuffer();
    for (String agent : agents) {
      // http://glu/console/agents/view/anent-name
      sb.append(String.format("<a href=\"%sagents/view/%s\">%s</a><br/>", GLU_SERVER, agent, agent));
    }
    return sb.toString();
  }

  /**
   * Adding a version means that the object checks if this version is either higher than the max or
   * lower than the min and if it is, updates max/min accordingly and sets the agents set to the
   * given agent.
   * If the version equals max/min then the agent is added to the set of already existing agents.
   * @param version
   * @param agent
   */
  public void addVersion(int version, String agent) {
    if (version == min) {
      minAgents.add(agent);
    } else if (version < min) {
      min = version;
      minAgents.clear();
      minAgents.add(agent);
    }

    if (version == max) {
      maxAgents.add(agent);
    } else if (version > max) {
      max = version;
      maxAgents.clear();
      maxAgents.add(agent);
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
