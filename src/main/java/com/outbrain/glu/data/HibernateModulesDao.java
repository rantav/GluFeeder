package com.outbrain.glu.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateModulesDao  extends HibernateDaoSupport implements ModulesDao {

  @Override
  public Module element(String name) {
    @SuppressWarnings("unchecked")
    List<Module> modules = getHibernateTemplate().find("from Module where name = ?", name);
    if (modules == null || modules.isEmpty()) {
      return null;
    }
    return modules.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Module> listAll() {
    return getHibernateTemplate().find("from Module");
  }

  @Override
  public Set<Module> listToday() {
    @SuppressWarnings("unchecked")
    List<Deployment> deployments = getHibernateTemplate().find("from Deployment d where d.startTime >= TODAY");
    Map<String, Module> modulesMap = new HashMap<String, Module>();
    Set<Module> ret = new HashSet<Module>();
    for (Deployment deployment : deployments) {
      Set<Module> modules = deployment.getModules();
      for (Module module : modules) {
        Module minimalModule = modulesMap.get(module.getName());
        if (minimalModule == null) {
          minimalModule = new Module(module.getName());
          modulesMap.put(minimalModule.getName(), minimalModule);
          ret.add(minimalModule);
        }
        minimalModule.getDeployments().add(deployment);
      }
    }
    return ret;
  }

  @Override
  public Set<Module> getOrCreate(String modulesList) {
    Set<Module> localModules = Module.fromString(modulesList);
    @SuppressWarnings("unchecked")
    List<Module> dbModulesList = getHibernateTemplate().findByNamedParam(
        "from Module where name in (:modulesList)", "modulesList", modulesList.split(","));
    Set<Module> dbModules = new HashSet<Module>(dbModulesList);
    Set<Module> missingModules = new HashSet<Module>(localModules);
    missingModules.removeAll(dbModules);
    getHibernateTemplate().saveOrUpdateAll(missingModules);
    dbModules.addAll(missingModules);
    return dbModules;
  }

}
