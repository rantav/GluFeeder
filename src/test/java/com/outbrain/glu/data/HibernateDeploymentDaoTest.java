package com.outbrain.glu.data;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.outbrain.glu.GluExecutionOrder;
import com.outbrain.glu.rpc.GluProgressStatus;
import com.outbrain.glu.rpc.GluProgressStatus.Description;

/**
 *
 * @author Ran Tavory
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:gluService-jdbc.xml", "classpath:gluService-jdbc-test.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class HibernateDeploymentDaoTest {

  @Resource
  private DeploymentsDao deploymentsDao;
  @Resource
  private TagsDao tagsDao;
  @Resource
  private CommittersDao committersDao;
  @Resource
  private ModulesDao modulesDao;
  @Resource
  private ClustersDao clustersDao;

  @Test
  @Transactional
  public void testSave() {
    Deployment d = new Deployment(modulesDao.getOrCreate("modulesList"), 1 /*modulesRevision*/,
        tagsDao.getOrCreate("t1,t1, t3 "),
        committersDao.getOrCreate("committer"), 2, "commitLogMessage");
    deploymentsDao.save(d);

    Deployment d2 = deploymentsDao.element(d.getId());
    Assert.assertEquals(d, d2);
  }

  @Test
  @Transactional
  public void testList() {
    List<Deployment> ds = deploymentsDao.listDeployments(false);
    Assert.assertEquals(0, ds.size());

    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    ds = deploymentsDao.listDeployments(false);
    Assert.assertEquals(1, ds.size());

    d = createNewDeployment();
    deploymentsDao.save(d);
    ds = deploymentsDao.listDeployments(false);
    Assert.assertEquals(2, ds.size());

    ds = deploymentsDao.listDeployments(true);
    Assert.assertEquals(2, ds.size());

    deploymentsDao.archive(d);
    ds = deploymentsDao.listDeployments(false);
    Assert.assertEquals(1, ds.size());

    ds = deploymentsDao.listDeployments(true);
    Assert.assertEquals(2, ds.size());
  }

  @Test
  @Transactional
  public void testNumActive_fresh() {
    Assert.assertEquals(0, deploymentsDao.getNumActive(false));
    Assert.assertEquals(0, deploymentsDao.getNumActive(true));
  }

  @Test
  @Transactional
  public void testNumActive_one() {
    deploymentsDao.save(createNewDeployment());
    Assert.assertEquals(1, deploymentsDao.getNumActive(false));
    Assert.assertEquals(1, deploymentsDao.getNumActive(true));
  }

  @Test
  @Transactional
  public void testNumActive_many() {
    deploymentsDao.save(createNewDeployment());
    deploymentsDao.save(createNewDeployment());
    deploymentsDao.save(createNewDeployment());
    deploymentsDao.save(createNewDeployment());
    deploymentsDao.save(createNewDeployment());
    Assert.assertEquals(5, deploymentsDao.getNumActive(false));
  }

  @Test
  @Transactional
  public void testNumActive_oneInactive() {
    Deployment d = createNewDeployment();
    d.endWithStatus(DeploymentStatus.DONE);
    deploymentsDao.save(d);
    Assert.assertEquals(0, deploymentsDao.getNumActive(false));
  }

  @Test
  @Transactional
  public void testNumActive_oneErrorod() {
    Deployment d = createNewDeployment();
    d.setError(new Exception());
    deploymentsDao.save(d);
    Assert.assertEquals(0, deploymentsDao.getNumActive(false));
  }


  @Test
  @Transactional
  public void testNumActive_withArchived() {
    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    deploymentsDao.archive(d);
    Assert.assertEquals(0, deploymentsDao.getNumActive(false));
    Assert.assertEquals(1, deploymentsDao.getNumActive(true));
  }

  @Test
  @Transactional
  public void testNumDone_fresh() {
    Assert.assertEquals(0, deploymentsDao.getNumDone(false));
    Assert.assertEquals(0, deploymentsDao.getNumDone(true));
  }

  @Test
  @Transactional
  public void testNumDone_oneActive() {
    deploymentsDao.save(createNewDeployment());
    Assert.assertEquals(0, deploymentsDao.getNumDone(false));
  }

  @Test
  @Transactional
  public void testNumDone_oneDone() {
    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    d.endWithStatus(DeploymentStatus.DONE);
    Assert.assertEquals(1, deploymentsDao.getNumDone(false));
    Assert.assertEquals(1, deploymentsDao.getNumDone(true));
  }

  @Test
  @Transactional
  public void testNumDone_many() {
    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    d.endWithStatus(DeploymentStatus.DONE);
    d = createNewDeployment();
    deploymentsDao.save(d);
    d.endWithStatus(DeploymentStatus.DONE);
    Assert.assertEquals(2, deploymentsDao.getNumDone(false));
  }

  @Test
  @Transactional
  public void testNumDone_archived() {
    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    d.endWithStatus(DeploymentStatus.DONE);
    deploymentsDao.archive(d);
    Assert.assertEquals(0, deploymentsDao.getNumDone(false));
    Assert.assertEquals(1, deploymentsDao.getNumDone(true));
  }

  @Test
  @Transactional
  public void testNumDone_oneErrrod() {
    Deployment res = createNewDeployment();
    deploymentsDao.save(res);
    res.setError(new Exception());
    Assert.assertEquals(0, deploymentsDao.getNumDone(false));
  }

  @Test
  @Transactional
  public void testSort_fresh() {
    Assert.assertEquals(0, deploymentsDao.listDeploymentsReverseChronological(false, 5).size());
  }

  @Test
  @Transactional
  public void testSort_one() {
    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    List<Deployment> ds = deploymentsDao.listDeploymentsReverseChronological(false, 5);
    Assert.assertEquals(1, ds.size());
    Assert.assertEquals(d, ds.get(0));
  }

  @Test
  @Transactional
  public void testSort_byTime() throws InterruptedException {
    Deployment inProgress1 = createNewDeployment();
    Thread.sleep(10);
    Deployment inProgress2 = createNewDeployment();
    Thread.sleep(10);
    Deployment inProgress3 = createNewDeployment();
    deploymentsDao.save(inProgress1);
    deploymentsDao.save(inProgress3);
    deploymentsDao.save(inProgress2);

    List<Deployment> ds = deploymentsDao.listDeploymentsReverseChronological(false, 5);
    Assert.assertEquals(inProgress3, ds.get(0));
    Assert.assertEquals(inProgress2, ds.get(1));
    Assert.assertEquals(inProgress1, ds.get(2));
  }

  @Test
  @Transactional
  public void testSort_archived() throws InterruptedException {
    Deployment inProgress1 = createNewDeployment();
    Thread.sleep(10);
    Deployment inProgress2 = createNewDeployment();
    Thread.sleep(10);
    Deployment inProgress3 = createNewDeployment();
    deploymentsDao.save(inProgress1);
    deploymentsDao.save(inProgress3);
    deploymentsDao.save(inProgress2);
    deploymentsDao.archive(inProgress1);

    List<Deployment> ds = deploymentsDao.listDeploymentsReverseChronological(false, 5);
    Assert.assertEquals(2, ds.size());
    Assert.assertEquals(inProgress3, ds.get(0));
    Assert.assertEquals(inProgress2, ds.get(1));
  }

  @Test
  @Transactional
  public void testSort_limit() throws InterruptedException {
    Deployment inProgress1 = createNewDeployment();
    Thread.sleep(10);
    Deployment inProgress2 = createNewDeployment();
    Thread.sleep(10);
    Deployment inProgress3 = createNewDeployment();
    deploymentsDao.save(inProgress1);
    deploymentsDao.save(inProgress3);
    deploymentsDao.save(inProgress2);

    List<Deployment> ds = deploymentsDao.listDeploymentsReverseChronological(false, 2);
    Assert.assertEquals(2, ds.size());
    Assert.assertEquals(inProgress3, ds.get(0));
    Assert.assertEquals(inProgress2, ds.get(1));
  }

  @Test
  @Transactional
  public void testArchive() throws InterruptedException {
    Deployment d1 = createNewDeployment();
    Deployment d2 = createNewDeployment();
    deploymentsDao.save(d1);
    deploymentsDao.save(d2);
    Assert.assertEquals(2, deploymentsDao.getNumActive(false));
    deploymentsDao.archive(d2.getId());
    Assert.assertEquals(1, deploymentsDao.getNumActive(false));
  }


  @Test
  @Transactional
  public void testGetNumError() throws InterruptedException {
    Deployment d1 = createNewDeployment();
    Deployment d2 = createNewDeployment();
    deploymentsDao.save(d1);
    deploymentsDao.save(d2);
    Assert.assertEquals(0, deploymentsDao.getNumError(false));

    d1.setStatus(DeploymentStatus.ERROR);
    deploymentsDao.save(d1);
    Assert.assertEquals(1, deploymentsDao.getNumError(false));

    d2.setStatus(DeploymentStatus.ERROR_TIMEOUT);
    deploymentsDao.save(d2);
    Assert.assertEquals(2, deploymentsDao.getNumError(false));

    deploymentsDao.archive(d2);
    Assert.assertEquals(1, deploymentsDao.getNumError(false));
    Assert.assertEquals(2, deploymentsDao.getNumError(true));
  }


  @Test
  @Transactional
  public void testGetTotal() throws InterruptedException {
    Deployment d1 = createNewDeployment();
    Deployment d2 = createNewDeployment();
    deploymentsDao.save(d1);
    deploymentsDao.save(d2);
    Assert.assertEquals(2, deploymentsDao.getNumTotal(false));
    Assert.assertEquals(2, deploymentsDao.getNumTotal(true));

    d1.setStatus(DeploymentStatus.ERROR);
    deploymentsDao.save(d1);
    Assert.assertEquals(2, deploymentsDao.getNumTotal(false));
  }

  @Test
  @Transactional
  public void testCancel() {
    Deployment d = createNewDeployment();
    deploymentsDao.save(d);
    Assert.assertEquals(1, deploymentsDao.getNumActive(false));
    deploymentsDao.cancel(d.getId());
    Assert.assertEquals(0, deploymentsDao.getNumActive(false));
  }

  @Test
  @Transactional
  public void testPhases() {
    Deployment d = createNewDeployment();
    d.addDeploymentPhase("systemFilter", GluExecutionOrder.PARALLEL);
    deploymentsDao.save(d);

    d = deploymentsDao.element(d.getId());
    List<DeploymentPhase> phases = d.getDeploymentPhases();
    Assert.assertEquals(1, phases.size());
  }

  @Test
  @Transactional
  public void testPhaseProgress() {
    Deployment d = createNewDeployment();
    d.addDeploymentPhase("systemFilter", GluExecutionOrder.PARALLEL);
    d.getDeploymentPhases().get(0).setProgress(new GluProgressStatus(Description.IN_PROGRESS, 50));
    deploymentsDao.save(d);

    d = deploymentsDao.element(d.getId());
    List<DeploymentPhase> phases = d.getDeploymentPhases();
    GluProgressStatus progress = phases.get(0).getProgress();
    Assert.assertEquals(50, progress.getPercentProgress());
    Assert.assertEquals(Description.IN_PROGRESS, progress.getDescription());
  }

  @Test
  @Transactional
  public void testElement() {
    Deployment d = createNewDeployment();
    d.setTags(tagsDao.getOrCreate("t1,t2"));
    deploymentsDao.save(d);

    d = deploymentsDao.element(d.getId());
    Assert.assertNotNull(d);
    Set<Tag> tags = d.getTags();
    Assert.assertEquals(Tag.fromString("t1,t2"), tags);
    Committer committer = d.getCommitter();
    Assert.assertNotNull("Committer is null", committer);
    Assert.assertEquals(new Committer("committer"), committer);
  }

  @Test
  @Transactional
  public void testTagsRelationship() {
    Deployment d = createNewDeployment();
    d.setTags(tagsDao.getOrCreate("t1,t2"));
    deploymentsDao.save(d);

    Deployment d2 = createNewDeployment();
    d2.setTags(tagsDao.getOrCreate("t1,t3"));
    deploymentsDao.save(d2);

    d = deploymentsDao.element(d.getId());
    Set<Tag> tags = d.getTags();
    Assert.assertEquals(Tag.fromString("t1,t2"), tags);

    for (Tag tag : tags) {
      if (tag.equals(new Tag("t1"))) {
        Assert.assertEquals(2, tag.getDeployments().size());
      }
    }
  }

  @Test
  @Transactional
  public void testGetDeploymentsHistogramByDate() {
    // Create 3 successful deployments and one failed
    GregorianCalendar now = new GregorianCalendar();
    deploymentsDao.save(createNewDoneDeployment("c1"));
    deploymentsDao.save(createNewDoneDeployment("c2"));
    deploymentsDao.save(createNewDoneDeployment("test"));
    Deployment failed = createNewDeployment();
    failed.setClusters(clustersDao.getOrCreate("c1"));
    failed.setError(new Exception());
    deploymentsDao.save(failed);

    // Create another deployment with a later date. This one should not be included in the results.
    Date then = new Date();
    deploymentsDao.save(createNewDoneDeployment("c1"));

    Map<Date, SuccessFailCounts> histogram = deploymentsDao.getDeploymentsHistogramByDate(now.getTime(), then);
    Assert.assertNotNull(histogram);
    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    SuccessFailCounts counts = histogram.get(now.getTime());
    Assert.assertNotNull("counts should not be null", counts);

    Assert.assertEquals(2, counts.getSuccess());
    Assert.assertEquals(1, counts.getFailed());
  }

  @Test
  @Transactional
  public void testDelete() {
    Set<Tag> tags = tagsDao.getOrCreate("t1,t2");
    Deployment d1 = new Deployment(modulesDao.getOrCreate("module1"), 1 /*modulesRevision*/,
        tags, committersDao.getOrCreate("committer1"), 1, "commitLogMessage1");
    deploymentsDao.save(d1);
    d1 = deploymentsDao.element(d1.getId());
    Assert.assertNotNull(d1);

    Deployment d2 = new Deployment(modulesDao.getOrCreate("module2"), 2 /*modulesRevision*/,
        tags, committersDao.getOrCreate("committer2"), 2, "commitLogMessage2");
    deploymentsDao.save(d2);

    Assert.assertNotNull("t1 is null", tagsDao.element("t1"));
    Assert.assertNotNull("committer1 should not be null", committersDao.element("committer1"));
    Assert.assertNotNull("module1 should not be null", modulesDao.element("module1"));

    Long id1 = d1.getId();
    deploymentsDao.delete(id1);
    d1 = deploymentsDao.element(id1);
    Assert.assertNull(d1);

    // make sure all orphaned tags/modules/committer are deleted and non-orphaned ones stay
    Assert.assertNotNull("t1 is null after deletion. it shouldn't", tagsDao.element("t1"));
    Assert.assertNotNull("t2 is null after deletion. it shouldn't", tagsDao.element("t2"));
    Assert.assertNull("committer1 should be null", committersDao.element("committer1"));
    Assert.assertNull("module1 should be null", modulesDao.element("module1"));
  }

  private Deployment createNewDoneDeployment(String cluster) {
    Deployment dep = createNewDeployment();
    dep.setStatus(DeploymentStatus.DONE);
    dep.setClusters(clustersDao.getOrCreate(cluster));
    return dep;
  }

  private Deployment createNewDeployment() {
    return new Deployment(null, 0, null, committersDao.getOrCreate("committer"), 4, "message");
  }
}
