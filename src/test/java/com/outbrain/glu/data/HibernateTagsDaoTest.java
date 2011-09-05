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
public class HibernateTagsDaoTest {

  @Resource
  private TagsDao tagsDao;
  @Resource
  private DeploymentsDao deploymentsDao;
  @Resource
  private CommittersDao committersDao;

  @Test
  @Transactional
  public void testElement() {
    Deployment d = createNewDeployment();
    d.setTags(tagsDao.getOrCreate("t1,t2"));
    deploymentsDao.save(d);

    Deployment d2 = createNewDeployment();
    d2.setTags(tagsDao.getOrCreate("t1,t3"));
    deploymentsDao.save(d2);
    Tag t1 = tagsDao.element("t1");
    Set<Deployment> deployments = t1.getDeployments();
    Assert.assertEquals(2, deployments.size());
  }

  private Deployment createNewDeployment() {
    return new Deployment(null, 0, null, committersDao.getOrCreate("committer"), 4, "message");
  }
}
