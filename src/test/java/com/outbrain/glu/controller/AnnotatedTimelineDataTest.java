package com.outbrain.glu.controller;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.outbrain.glu.data.ClustersDao;
import com.outbrain.glu.data.CommittersDao;
import com.outbrain.glu.data.Deployment;
import com.outbrain.glu.data.DeploymentsDao;
import com.outbrain.glu.data.ModulesDao;
import com.outbrain.glu.data.TagsDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:gluService-jdbc.xml", "classpath:gluService-jdbc-test.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class AnnotatedTimelineDataTest {

  @Resource
  private TagsDao tagsDao;
  @Resource
  private CommittersDao committersDao;
  @Resource
  private ModulesDao modulesDao;
  @Resource
  private ClustersDao clustersDao;
  @Resource
  private DeploymentsDao deploymentsDao;

  @Test
  @Transactional
  public void testEmpty() {
    List<Deployment> ds = new ArrayList<Deployment>();
    AnnotatedTimelineData data = new AnnotatedTimelineData(ds);
    Assert.assertTrue(data.getRows().isEmpty());
  }

  @Test
  @Transactional
  public void testSimple() {
    List<Deployment> ds = new ArrayList<Deployment>();
    ds.add(createDeployment("module", 5, "ran", "tag", "ny"));
    AnnotatedTimelineData data = new AnnotatedTimelineData(ds);
    Assert.assertEquals(1, data.getRows().size());
  }

  @Test
  @Transactional
  public void testClusterTest() {
    List<Deployment> ds = new ArrayList<Deployment>();
    ds.add(createDeployment("module", 5, "ran", "tag", "test"));
    AnnotatedTimelineData data = new AnnotatedTimelineData(ds);
    Assert.assertEquals(0, data.getRows().size());
  }

  @Test
  @Transactional
  public void testClustersMix() {
    List<Deployment> ds = new ArrayList<Deployment>();
    ds.add(createDeployment("module", 5, "ran", "tag", "test"));
    ds.add(createDeployment("module2", 5, "ran", "tag", "ny"));
    AnnotatedTimelineData data = new AnnotatedTimelineData(ds);
    Assert.assertEquals(1, data.getRows().size());
  }

  private Deployment createDeployment(String modules, long revision, String committer, String tags,
      String clusters) {
    Deployment d = new Deployment(modulesDao.getOrCreate(modules), revision, tagsDao.getOrCreate(tags),
        committersDao.getOrCreate(committer), 0, "message");
    d.setClusters(clustersDao.getOrCreate(clusters));
    deploymentsDao.save(d);
    return d;
  }
}
