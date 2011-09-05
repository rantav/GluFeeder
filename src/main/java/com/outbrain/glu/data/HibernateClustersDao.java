package com.outbrain.glu.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author ran
 *
 */
public class HibernateClustersDao extends HibernateDaoSupport implements ClustersDao {

  @Override
  public Cluster element(String name) {
    @SuppressWarnings("unchecked")
    List<Cluster> clusters = getHibernateTemplate().find("from Cluster where name = ?", name);
    if (clusters == null || clusters.isEmpty()) {
      return null;
    }
    return clusters.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Cluster> listAll() {
    return getHibernateTemplate().find("from Cluster");
  }

  @Override
  public Set<Cluster> getOrCreate(String clustersList) {
    Set<String> set = new HashSet<String>();
    Collections.addAll(set, clustersList.split(","));
    return getOrCreate(set);
  }

  @Override
  public Set<Cluster> getOrCreate(Set<String> clusters) {
    Set<Cluster> local = Cluster.fromStringSet(clusters);
    @SuppressWarnings("unchecked")
    List<Cluster> dbList = getHibernateTemplate().findByNamedParam(
        "from Cluster where name in (:clustersList)", "clustersList", StringUtils.join(clusters, ","));
    Set<Cluster> dbClusters = new HashSet<Cluster>(dbList);
    Set<Cluster> missing = new HashSet<Cluster>(local);
    missing.removeAll(dbClusters);
    getHibernateTemplate().saveOrUpdateAll(missing);
    dbClusters.addAll(missing);
    return dbClusters;
  }

  @Override
  public Set<Cluster> listToday() {
    @SuppressWarnings("unchecked")
    List<Deployment> deployments = getHibernateTemplate().find("from Deployment d where d.startTime >= TODAY");
    Map<String, Cluster> clustersMap = new HashMap<String, Cluster>();
    Set<Cluster> ret = new HashSet<Cluster>();
    for (Deployment deployment : deployments) {
      Set<Cluster> clusters = deployment.getClusters();
      for (Cluster cluster : clusters) {
        Cluster minimalCluster = clustersMap.get(cluster.getName());
        if (minimalCluster == null) {
          minimalCluster = new Cluster(cluster.getName());
          clustersMap.put(minimalCluster.getName(), minimalCluster);
          ret.add(minimalCluster);
        }
        minimalCluster.getDeployments().add(deployment);
      }
    }
    return ret;
  }

}
