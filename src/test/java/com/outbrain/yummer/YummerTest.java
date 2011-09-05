package com.outbrain.yummer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.outbrain.glu.data.Module;

/**
 *
 * @author Ran Tavory
 *
 */
public class YummerTest {

  @Mock
  Runtime runtime;
  @Mock
  Process process;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = YummerRuntimeException.class)
  public void testList_errorStatus() throws IOException, InterruptedException,
      YummerRuntimeException {
    Yummer y = new Yummer(runtime);
    Mockito.when(runtime.exec("yum list module")).thenReturn(process);
    Mockito.when(process.exitValue()).thenReturn(1);
    y.list(new Module("module"));
  }

  @Test
  public void testList_zeroItems() throws IOException, InterruptedException, YummerRuntimeException {
    Yummer y = new Yummer(runtime);
    Mockito.when(runtime.exec("yum list module")).thenReturn(process);
    Mockito.when(process.exitValue()).thenReturn(0);
    String yumReturn = "Loaded plugins: fastestmirror\n" + "Error: No matching Packages to list";
    Mockito.when(process.getInputStream()).thenReturn(IOUtils.toInputStream(yumReturn));
    List<RpmInfo> l = y.list(new Module("module"));
    Assert.assertEquals("Should return an empty list", 0, l.size());
  }

  @Test
  public void testList_oneItem() throws IOException, InterruptedException, YummerRuntimeException {
    Yummer y = new Yummer(runtime);
    Mockito.when(runtime.exec("yum list module")).thenReturn(process);
    Mockito.when(process.exitValue()).thenReturn(0);
    String yumReturn = "ImageServer.noarch                                                                                       30862-1                                                                                        ob";
    Mockito.when(process.getInputStream()).thenReturn(IOUtils.toInputStream(yumReturn));
    List<RpmInfo> l = y.list(new Module("module"));
    Assert.assertEquals("Should return an empty list", 1, l.size());
    RpmInfo rpm = l.get(0);
    Assert.assertEquals("ImageServer", rpm.getModule());
    Assert.assertEquals("30862", rpm.getVersion());
    Assert.assertEquals("ob", rpm.getRepo());
  }

  @Test
  public void testList_multipleItem() throws IOException, InterruptedException,
      YummerRuntimeException {
    Yummer y = new Yummer(runtime);
    Mockito.when(runtime.exec("yum list module")).thenReturn(process);
    Mockito.when(process.exitValue()).thenReturn(0);
    String yumReturn = "Installed Packages\n"
        + "ImageServer.noarch                                                                                    31218-1                                                                                    installed\n"
        + "Available Packages\n"
        + "ImageServer.noarch                                                                                    30862-1                                                                                    ob\n"
        + "ImageServer.noarch                                                                                    30868-1                                                                                    ob\n"
        + "ImageServer.noarch                                                                                    30919-1                                                                                    ob\n"
        + "ImageServer.noarch                                                                                    31010-1                                                                                    ob\n"
        + "ImageServer.noarch                                                                                    31218-1                                                                                    ob";
    Mockito.when(process.getInputStream()).thenReturn(IOUtils.toInputStream(yumReturn));
    List<RpmInfo> l = y.list(new Module("module"));
    Assert.assertEquals("Should return an empty list", 6, l.size());
    RpmInfo rpm = l.get(0);
    Assert.assertEquals("ImageServer", rpm.getModule());
    Assert.assertEquals("31218", rpm.getVersion());
    Assert.assertEquals("installed", rpm.getRepo());
  }

  @SuppressWarnings("serial")
  @Test
  public void testCheckModuleExistance_singleRepo() throws IOException, InterruptedException,
      YummerRuntimeException {
    Yummer y = new Yummer(runtime);
    Mockito.when(runtime.exec("yum list ImageServer")).thenReturn(process);
    Mockito.when(runtime.exec("yum list ImageX")).thenReturn(process);
    Mockito.when(process.exitValue()).thenReturn(0);
    String yumReturn = "ImageServer.noarch                                                                                       30862-1                                                                                        ob";
    Mockito.when(process.getInputStream()).thenReturn(IOUtils.toInputStream(yumReturn));
    Assert.assertTrue("ImageServer-30862 should exist",
        y.checkModuleExistanceInRepos(new Module("ImageServer"), "30862", new HashSet<String>(){{add("ob");}}));
    Assert.assertFalse("ImageServer-30862 should exist",
        y.checkModuleExistanceInRepos(new Module("ImageServer"), "30862", new HashSet<String>(){{add("ob-x");}}));
    Assert.assertFalse("ImageServer-30861 should not exist",
        y.checkModuleExistanceInRepos(new Module("ImageServer"), "30861", new HashSet<String>(){{add("ob");}}));
    Assert.assertFalse("ImageX-30862 should not exist",
        y.checkModuleExistanceInRepos(new Module("ImageX"), "30862", new HashSet<String>(){{add("ob");}}));
  }

  @SuppressWarnings("serial")
  @Test
  public void testCheckModuleExistance_multipleRepos() throws IOException, InterruptedException,
      YummerRuntimeException {
    Yummer y = new Yummer(runtime);
    Mockito.when(runtime.exec("yum list ImageServer")).thenReturn(process);
    Mockito.when(process.exitValue()).thenReturn(0);
    String yumReturn = "ImageServer.noarch                                                                                       30862-1                                                                                        ob";
    Mockito.when(process.getInputStream()).thenReturn(IOUtils.toInputStream(yumReturn));
    Assert.assertTrue(
        "ImageServer-30862 should exist",
        y.checkModuleExistanceInRepos(new Module("ImageServer"), "30862",
            new HashSet<String>(){{add("ob");add("xxx");}}));
    Assert.assertFalse("ImageServer-30862 should no exist in repo xxx",
        y.checkModuleExistanceInRepos(new Module("ImageServer"), "30862", new HashSet<String>(){{add("xxx");}}));
    Assert.assertFalse(
        "ImageServer-30862 should no exist in repo xxx or yyy",
        y.checkModuleExistanceInRepos(new Module("ImageServer"), "30862",
            new HashSet<String>(){{add("xxx");add("yyy");}}));
  }
}
