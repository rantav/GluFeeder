package com.outbrain.glu.data;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Ran Tavory
 *
 */
public class DeploymentTest {

  private Deployment res;

  @Before
  public void setup() {
    res = new Deployment(null, 0, null, new Committer("committer"), 4, "message");
  }

  @After
  public void teardown() {
    res = null;
  }

  @Test
  public void testHasErrors_fresh() {
    Assert.assertFalse("Should not have errors when just created", res.hasErrors());
  }

  @Test
  public void testHasErrors_withErrors() {
    res.setError(new Exception());
    Assert.assertTrue("Should have errors after setting an exception", res.hasErrors());
  }


  @Test
  public void testIsActive_fresh() {
    Assert.assertTrue("Should be active when just created", res.isActive());
  }

  @Test
  public void testIsActive_withErrors() {
    res.setError(new Exception());
    Assert.assertFalse("Should be inactive after setting an exception", res.isActive());
  }

  @Test
  public void testIsActive_ended() {
    res.endWithStatus(DeploymentStatus.DONE);
    Assert.assertFalse("Should be inactive after end() was called", res.isActive());
  }

  @Test
  public void testGetModulesAsList_null() {
    res = new Deployment(null, 0, null, new Committer("committer"), 4, "message");
    Assert.assertTrue(res.getModules().isEmpty());
  }

  @Test
  public void testGetModulesAsList_empty() {
    res = new Deployment(Module.fromString(""), 0, null, new Committer("committer"), 4, "message");
    Assert.assertTrue(res.getModules().isEmpty());
  }

  @Test
  public void testGetModulesAsList_one() {
    res = new Deployment(Module.fromString("m1"), 0, null, new Committer("committer"), 4, "message");
    Assert.assertEquals(1, res.getModules().size());
  }

  @Test
  public void testGetModulesAsList_many() {
    res = new Deployment(Module.fromString("m1,m2,m3"), 0, null, new Committer("committer"), 4,
        "message");
    Assert.assertEquals(3, res.getModules().size());
  }
}
