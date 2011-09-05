package com.outbrain.glu.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

/**
 * This class describes a diff result between two glu models. It is given a list of entries (the
 * diff) and a scope on which it may operatio.
 *
 * @author Ran Tavory
 *
 */
public class GluModelDiff {

  private static final Logger log = LoggerFactory.getLogger(GluModelDiff.class);

  private final GluScope scope;
  private final Set<GluEntry> entries;

  public GluModelDiff(Set<GluEntry> entries, GluScope scope) {
    Assert.notNull(entries, "Entries can't be null");
    Assert.notNull(scope, "Scope can't be null");
    this.scope = scope;
    this.entries = Collections.unmodifiableSet(entries);
  }

  public Set<GluEntry> getEntries() {
    return entries;
  }

  /**
   * Returns a glu DSL representation of the diff. For example
   * metadata.product='www';tags.hasAny('testapi1;stgweb1')
   *
   * @param nAgetns
   *          the number of agents to be selected. If negative then all agents are selected.
   * @param nClusters
   *          the number of clusters to choose from. If negative then all clusters (data centers)
   *          are selected
   * @return A glu DSL with the selection of the first nAgents
   */
  public String getDsl(int nAgents, int nClusters) {
    return scope.getDsl()
        + (nAgents < 0 && nClusters < 0 ? "" : ";" + getFirstNAgentSelector(nAgents, nClusters));
  }

  /**
   *
   * @return true iff the diff is empty
   */
  public boolean isEmpty() {
    return entries.isEmpty();
  }

  private String getFirstNAgentSelector(int nAgents, int nClusters) {
    Set<String> agents = getFirstNAgents(nAgents, nClusters);
    return getGluDslFromAgents(agents);
  }

  private String getGluDslFromAgents(Set<String> agents) {
    StringBuffer sb = new StringBuffer();
    sb.append("or{");
    for (String agent : agents) {
      sb.append("agent='");
      sb.append(agent);
      sb.append("';");
    }
    sb.append("}");
    return sb.toString();
  }

  private Set<String> getFirstNAgents(int nAgents, int nClusters) {
    // A map of clusters to agents.
    // For any cluster such as ny or chi there's a list of agent names.
    SortedSetMultimap<String, String> clusters2agents = TreeMultimap.create();
    for (GluEntry entry : entries) {
      String cluster = entry.getCluster();
      String agent = entry.getAgent().getName();
      Set<String> agents = clusters2agents.get(cluster);
      if (agents == null || agents.isEmpty()) {
        // This is the first agenet for this cluster. Let's see if clusters are over quota
        if (clusters2agents.keySet().size() < nClusters || nClusters < 0) {
          // Clusters are still under quota. Add a new cluster.
          clusters2agents.put(cluster, agent);
        }
      } else if (agents.size() < nAgents || nAgents < 0) {
        // agents are still under quota, add more to this cluster
        clusters2agents.put(cluster, agent);
      }
    }
    Set<String> agents = new HashSet<String>(clusters2agents.values());
    log.info("Selecting {} agents in {} clusters. Agents are: {}",
        new Object[] { nAgents, nClusters, agents });
    return agents;
  }

  @Override
  public String toString() {
    return entries.toString();
  }

  public String getDsl(Map<String, Integer> dcServiceCount, float maxFractionInEachDc) {
    return scope.getDsl()
        + ";"
        + getGluDslFromAgents(getFractionOfServiceHostsSelector(dcServiceCount, maxFractionInEachDc));
  }

  /**
   * Returns a set of agent names. The agents count in each cluster is no more than the allowed
   * number of agents in each DC per the maxFractionInEachDc parameter
   *
   * @param dcServiceCount
   * @param maxFractionInEachDc
   * @return
   */
  private Set<String> getFractionOfServiceHostsSelector(Map<String, Integer> dcServiceCount,
      float maxFractionInEachDc) {
    // The current count of DC-SERVICE found so far.
    Map<String, Integer> currentCounts = new HashMap<String, Integer>(dcServiceCount.size());
    Set<String> agents = new HashSet<String>();
    SortedSetMultimap<String, String> agents2services = getAgentsToServicesMap();
    for (GluEntry entry : entries) {
      String cluster = entry.getCluster();
      String service = entry.getServiceName();
      String agent = entry.getAgent().getName();
      String key = cluster + "-" + service;
      int currentServiceAtDcCount = zeroForNull(currentCounts.get(key));
      int desiredServiceAtDcCount = (int) Math.ceil(zeroForNull(dcServiceCount.get(key))
          * maxFractionInEachDc);
      if (currentServiceAtDcCount < desiredServiceAtDcCount) {
        log.debug("Selecting agent {} for key {}, unless there are too many for a differnt " +
        		"service on this agent...", agent, key);
        if (isAnyServiceOverAgentQuota(agents2services, agent, cluster, dcServiceCount,
            maxFractionInEachDc, currentCounts)) {
          log.debug("One of the services is over quota. Will not select this agent");
        } else {
          agents.add(agent);
          increaseCountForAllServicesOnAgent(agents2services, cluster, agent, currentCounts);
        }
      }
    }
    log.debug("Selected agents: {}", agents);
    return agents;
  }

  private void increaseCountForAllServicesOnAgent(SortedSetMultimap<String, String> agents2services,
      String cluster, String agent, Map<String, Integer> currentCounts) {
    for (String service: agents2services.get(agent)) {
      String key = cluster + "-" + service;
      int currentServiceAtDcCount = zeroForNull(currentCounts.get(key));
      currentCounts.put(key, currentServiceAtDcCount + 1);
    }
  }

  private boolean isAnyServiceOverAgentQuota(SortedSetMultimap<String, String> agents2services,
      String agent, String cluster, Map<String, Integer> dcServiceCount,
      float maxFractionInEachDc, Map<String, Integer> currentCounts) {
    for (String service: agents2services.get(agent)) {
      String key = "cluster" + "-" + service;
      int currentServiceAtDcCount = zeroForNull(currentCounts.get(key));
      int desiredServiceAtDcCount = (int) Math.ceil(zeroForNull(dcServiceCount.get(key))
          * maxFractionInEachDc);
      if (currentServiceAtDcCount > desiredServiceAtDcCount) {
        return true;
      }
    }
    return false;
  }

  private SortedSetMultimap<String, String> getAgentsToServicesMap() {
    SortedSetMultimap<String, String> agents2services = TreeMultimap.create();
    for (GluEntry entry : entries) {
      String service = entry.getServiceName();
      String agent = entry.getAgent().getName();
      agents2services.put(agent, service);
    }
    return agents2services;
  }

  private int zeroForNull(Integer integer) {
    return integer == null ? 0 : integer;
  }
}
