package com.outbrain.glu.data;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.ibm.icu.util.GregorianCalendar;


/**
 * Describes the state of the ongoing deployments.
 *
 * @author Ran Tavory
 *
 */
/*package*/ class HibernateDeploymentsDao extends HibernateDaoSupport implements DeploymentsDao {

  private static final Object[] ACTIVE_STATES = {DeploymentStatus.NOT_STARTED,
                                                 DeploymentStatus.GLU_DEPLOYING_FIRST_HOST,
                                                 DeploymentStatus.GLU_DEPLOYING_MORE_HOSTS,
                                                 DeploymentStatus.GLU_LOADING_MODEL,
                                                 DeploymentStatus.PENDING_YUM,
                                                 DeploymentStatus.WORKING_SVN};

  private static final Object[] DONE_STATES = {DeploymentStatus.DONE,
                                               DeploymentStatus.GLU_NOTHING_TO_DO,
                                               DeploymentStatus.PREPARE_ONLY_DONE,
                                               DeploymentStatus.SUCCESS_SVN_ONLY};

  @SuppressWarnings("serial")
  private static final Set<DeploymentStatus> DONE_STATES_SET = new HashSet<DeploymentStatus>(){{
    for (Object o : DONE_STATES) {
      add((DeploymentStatus)o);
    }
  }};

  private static final Object[] ERROR_STATES = {DeploymentStatus.ERROR,
                                               DeploymentStatus.ERROR_TIMEOUT,
                                               DeploymentStatus.GLU_CANT_CREATE_EXECUTION};

  private static final Logger log = LoggerFactory.getLogger(HibernateDeploymentsDao.class);

  public HibernateDeploymentsDao() {}

  @Override
  public long getNumActive(boolean includeArchived) {
    return (Long) getHibernateTemplate().findByNamedParam("select count(*) from Deployment " +
		"where status in (:statuses) and archived in (false, :archived)",
		new String[] {"statuses", "archived"},
		new Object[] {ACTIVE_STATES, Boolean.valueOf(includeArchived)}).get(0);
  }

  @Override
  public long getNumError(boolean includeArchived) {
    return (Long) getHibernateTemplate().findByNamedParam("select count(*) from Deployment " +
        "where status in (:statuses) and archived in (false, :archived)",
        new String[] {"statuses", "archived"},
        new Object[] {ERROR_STATES, includeArchived}).get(0);
  }

  @Override
  public long getNumDone(boolean includeArchived) {
    return (Long) getHibernateTemplate().findByNamedParam("select count(*) from Deployment " +
        "where status in (:statuses) and archived in (false, :archived)",
        new String[] {"statuses", "archived"},
        new Object[] {DONE_STATES, includeArchived}).get(0);
  }

  @Override
  public long getNumTotal(boolean includeArchived) {
    return (Long) getHibernateTemplate().findByNamedParam("select count(*) from Deployment " +
        "where archived in (false, :archived)",
        new String[] {"archived"},
        new Object[] {includeArchived}).get(0);
  }

  @Override
  public void cancel(Long id) {
    Deployment d = element(id);
    d.setStatus(DeploymentStatus.CANCELED);
    save(d);
  }

  @Override
  public void save(Deployment d) {
    getHibernateTemplate().saveOrUpdate(d);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Deployment> listDeployments(boolean includeArchived) {
    return getHibernateTemplate().find("from Deployment where archived in (false, ?)", includeArchived);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Deployment> listDeploymentsReverseChronological(boolean includeArchived, long limit) {
    Session session = getSession();
    Query q = session.createQuery("from Deployment where archived in " +
        "(false, :archived) order by startTime desc");
    q.setMaxResults((int) limit);
    q.setParameter("archived", includeArchived);
    return q.list();
  }

  @Override
  public void archive(Deployment d) {
    d.setArchived(true);
    save(d);
  }

  @Override
  public void archive(Long id) {
    Deployment d = element(id);
    archive(d);
  }

  @Override
  public Deployment element(Long id) {
    @SuppressWarnings("unchecked")
    List<Deployment> ds = getHibernateTemplate().find("from Deployment where id = ?", id);
    if (ds == null || ds.isEmpty()) {
      return null;
    }
    return ds.get(0);
  }

  @Override
  public long getNumActive() {
    return getNumActive(false);
  }

  @Override
  public long getNumTotal() {
    return getNumTotal(false);
  }

  @Override
  public long getNumDone() {
    return getNumDone(false);
  }

  @Override
  public long getNumError() {
    return getNumError(false);
  }

  @Override
  public void clear() {
    getHibernateTemplate().deleteAll(listDeployments(true));
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Map<Date, SuccessFailCounts> getDeploymentsHistogramByDate(final Date start, final Date end) {

    // This query selects the number of deployments for each day (group by day,month,year) and for
    // each status (group by status).
    List l = getHibernateTemplate().findByNamedParam("select day(d.startTime), month(d.startTime)," +
    		" year(d.startTime), d.status, count(*)" +
    		" from Deployment as d" +
    		" inner join d.clusters as cluster" +
    		" where d.startTime between :start and :end" +
    		" and cluster.name not in (:testClusters)" +
    		" group by day(d.startTime), month(d.startTime), year(d.startTime), d.status",
    		new String[]{"start", "end", "testClusters"},
    		new Object[]{start, end, Cluster.TEST_CLUSTERS});

    // Massage the result to collect successful v/s unsuccessful deployment counts.
    Map<Date, SuccessFailCounts> ret = new HashMap<Date, SuccessFailCounts>(l.size());
    for (Object object : l) {
      Object[] arr = (Object[])object;
      int day = (Integer)arr[0];
      int month = (Integer)arr[1];
      int year = (Integer)arr[2];
      DeploymentStatus status = (DeploymentStatus)arr[3];
      long count = (Long)arr[4];
      GregorianCalendar cal = new GregorianCalendar(year,
          month - 1 /* Java months are zero based, SQL aren't*/, day, 0, 0, 0);
      Date date = cal.getTime();
      SuccessFailCounts counts = ret.get(date);
      if (counts == null) {
        counts = new SuccessFailCounts(0, 0);
        ret.put(date, counts);
      }
      if (DONE_STATES_SET.contains(status)) {
        counts.setSuccess((int)count);
      } else {
        counts.setFailed((int)count);
      }
    }
    return ret;
  }

  @Override
  public void delete(Long id) {
    Deployment d = element(id);
    if (d == null) {
      log.error("Can't delete deployment wiht ID {}, element not found.", id);
    } else {
      // Delete all associations first
      for (Tag tag : d.getTags()) {
        tag.removeDeployment(d);
        if (tag.getDeployments().isEmpty()) {
          getHibernateTemplate().delete(tag);
        } else {
          getHibernateTemplate().saveOrUpdate(tag);
        }
      }
      d.getTags().clear();

      Committer committer = d.getCommitter();
      committer.removeDeployment(d);
      if (committer.getDeployments().isEmpty()) {
        getHibernateTemplate().delete(committer);
      } else {
        getHibernateTemplate().saveOrUpdate(committer);
      }
      d.setCommitter(null);

      for (Module module : d.getModules()) {
        module.removeDeployment(d);
        if (module.getDeployments().isEmpty()) {
          getHibernateTemplate().delete(module);
        } else {
          getHibernateTemplate().saveOrUpdate(module);
        }
      }
      d.getModules().clear();

      for (Cluster cluster : d.getClusters()) {
        cluster.removeDeployment(d);
        if (cluster.getDeployments().isEmpty()) {
          getHibernateTemplate().delete(cluster);
        } else {
          getHibernateTemplate().saveOrUpdate(cluster);
        }
      }
      d.getClusters().clear();

      getHibernateTemplate().delete(d);
    }
  }
}
