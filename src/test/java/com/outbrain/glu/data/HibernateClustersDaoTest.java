package com.outbrain.glu.data;


import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:gluService-jdbc.xml",
                                   "classpath:gluService-jdbc-test.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class HibernateClustersDaoTest {

  @Resource
  private DeploymentsDao deploymentsDao;
  @Resource
  private CommittersDao committersDao;
  @Resource
  private ModulesDao modulesDao;
  @Resource
  private ClustersDao clustersDao;

  @Test
  @Transactional
  public void testElement() {
    Deployment d = new Deployment(modulesDao.getOrCreate("m1,m2"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    d.setClusters(clustersDao.getOrCreate("c1,c2"));
    deploymentsDao.save(d);

    // Test element of Clusters
    Cluster c1 = clustersDao.element("c1");
    Assert.assertNotNull(c1);
    Assert.assertEquals(d, c1.getDeployments().iterator().next());

    // Test elememt of Deployment
    d = deploymentsDao.element(d.getId());
    Set<Cluster> clusters = new HashSet<Cluster>();
    clusters.add(new Cluster("c1"));
    clusters.add(new Cluster("c2"));
    Assert.assertEquals(clusters, d.getClusters());
  }
}
