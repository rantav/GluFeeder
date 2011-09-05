package com.outbrain.glu.data;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:gluService-jdbc.xml",
                                   "classpath:gluService-jdbc-test.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class HibernateModulesDaoTest {

  @Resource
  private DeploymentsDao deploymentsDao;
  @Resource
  private CommittersDao committersDao;
  @Resource
  private ModulesDao modulesDao;

  @Test
  @Transactional
  public void testElement() {
    Deployment d = new Deployment(modulesDao.getOrCreate("m1,m2"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d);

    Deployment d2 = new Deployment(modulesDao.getOrCreate("m1,m3,m4,m5"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d2);

    Module m1 = modulesDao.element("m1");
    Set<Deployment> deployments = m1.getDeployments();
    Assert.assertEquals(2, deployments.size());

    Module m5 = modulesDao.element("m5");
    deployments = m5.getDeployments();
    Assert.assertEquals(1, deployments.size());
  }

  @Test
  @Transactional
  public void testListAll() {
    Deployment d = new Deployment(modulesDao.getOrCreate("m1,m2"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d);

    Deployment d2 = new Deployment(modulesDao.getOrCreate("m1,m3,m4,m5"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d2);

    List<Module> modules = modulesDao.listAll();
    Assert.assertEquals(5, modules.size());
  }

  @Test
  @Transactional
  public void testListToday() {
    Deployment d = new Deployment(modulesDao.getOrCreate("m1"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d);

    Deployment d2 = new Deployment(modulesDao.getOrCreate("m1,m2"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d2);

    Set<Module> modules = modulesDao.listToday();
    Assert.assertEquals(2, modules.size());
  }

  @Test
  @Transactional
  @Ignore
  public void testListToday_withDeploymentsYesterday() {
    Deployment d = new Deployment(modulesDao.getOrCreate("m1"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    deploymentsDao.save(d);

    Deployment d2 = new Deployment(modulesDao.getOrCreate("m1,m2"), 0, null,
        committersDao.getOrCreate("committer"), 4, "message");
    GregorianCalendar yesterday = new GregorianCalendar();
    yesterday.roll(Calendar.DAY_OF_MONTH, -1);
    d2.setStartTime(yesterday.getTime());
    deploymentsDao.save(d2);

    Set<Module> modules = modulesDao.listToday();
    Assert.assertEquals(2, modules.size());
    Assert.assertEquals("m1", modules.iterator().next().getName());
  }
}
