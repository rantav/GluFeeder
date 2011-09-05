package com.outbrain.glu.data;


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
@ContextConfiguration(locations = {"classpath:gluService-jdbc.xml", "classpath:gluService-jdbc-test.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class HibernateCommittersDaoTest {

  @Resource
  private DeploymentsDao deploymentsDao;
  @Resource
  private CommittersDao committersDao;

  @Test
  @Transactional
  public void testRelationshipWithDeployments() {
    Deployment d1 = new Deployment(null, 0, null, committersDao.getOrCreate("ran"), 4, "message");
    deploymentsDao.save(d1);

    Deployment d2 = new Deployment(null, 0, null, committersDao.getOrCreate("eran"), 4, "message");
    deploymentsDao.save(d2);

    Deployment d3 = new Deployment(null, 0, null, committersDao.getOrCreate("ran"), 4, "message");
    deploymentsDao.save(d3);

    Committer ran = committersDao.element("ran");
    Set<Deployment> deployments = ran.getDeployments();
    Assert.assertEquals(2, deployments.size());

    Committer eran = committersDao.element("eran");
    deployments = eran.getDeployments();
    Assert.assertEquals(1, deployments.size());
  }
}
