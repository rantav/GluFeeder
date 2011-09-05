package com.outbrain.glu.data;

import java.util.List;
import java.util.Set;

/**
 * 
 * @author ran
 *
 */
public interface ModulesDao {

  /** Get a by ID (it's name, e.g. ImageServer)*/
  Module element(String id);

  List<Module> listAll();

  /**
   * List all deployments for all modules, with a limit on the number of deployments per module
   * @param deploymentsLimit
   * @return
   */
  public Set<Module> listToday();

  /**
   * Gets the set of modules from the DB and creates new modules for any module that doesn't exist yet.
   * @param string a comma delimited list of modules names
   * @return
   */
  Set<Module> getOrCreate(String modulesList);

}
