package com.outbrain.glu.data;

import java.util.List;
import java.util.Set;


public interface ClustersDao {

  /** Get a by ID (it's name, e.g. la or ny)*/
  Cluster element(String id);

  List<Cluster> listAll();

  /**
   * Gets the set of clusters from the DB and creates new tags for any tag that doesn't exist yet.
   * @param string a comma delimited list of tag names
   * @return
   */
  Set<Cluster> getOrCreate(String clustersList);

  Set<Cluster> getOrCreate(Set<String> clusters);

  Set<Cluster> listToday();

}
