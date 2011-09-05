package com.outbrain.glu;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.tmatesoft.svn.core.SVNException;

import com.outbrain.glu.data.Committer;
import com.outbrain.glu.svn.SVNUpdater;
import com.outbrain.glu.yammer.YammerApi;

public class DeploymentUpdaterTest {

  @Mock private SVNUpdater svnUpdater;
  @Mock private YammerApi yammer;
  private DeploymentUpdater updater;
  private String svnFileContent;
  private final Committer committer = new Committer("committer");

  @Before
  public void setup() throws SVNException, IOException, URISyntaxException {
    // top secret :)
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testApplyTransformation() throws Exception{
    // top secret :)
  }

  @Test
  public void testApplyTransformation_noChange() throws Exception{
    // top secret :)
  }


  @Test
  public void testCommitToScm() throws Exception{
    // top secret :)
  }

  @Test
  public void testTellYammer_simple() throws IOException {
    // top secret :)
  }

  @Test
  public void testTellYammer_testAndProdMixed() throws IOException {
    // top secret :)
  }

  @Test
  public void testTellYammer_prodOnly() throws IOException {
    // top secret :)
  }
}
