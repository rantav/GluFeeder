package com.outbrain.glu.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tmatesoft.svn.core.SVNException;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.outbrain.glu.DeploymentUpdater;
import com.outbrain.glu.data.Cluster;
import com.outbrain.glu.data.ClustersDao;
import com.outbrain.glu.data.Committer;
import com.outbrain.glu.data.CommittersDao;
import com.outbrain.glu.data.Deployment;
import com.outbrain.glu.data.DeploymentStatus;
import com.outbrain.glu.data.DeploymentsDao;
import com.outbrain.glu.data.Module;
import com.outbrain.glu.data.ModulesDao;
import com.outbrain.glu.data.Tag;
import com.outbrain.glu.data.TagsDao;
import com.outbrain.glu.rpc.GluProxy;
import com.outbrain.glu.svn.SVNGluJson;
import com.outbrain.glu.svn.SvnApi;
import com.outbrain.glu.teamc.TeamCityApi;
import com.outbrain.yummer.YummerRuntimeException;

/**
 *
 * @author ran
 *
 */
@Controller
public class GluController {

  private final static Logger log = LoggerFactory.getLogger(GluController.class);

  private final DeploymentUpdater deploymentUpdater;
  private final TeamCityApi teamCityApi;
  private final SvnApi svn;

  private final DeploymentsDao deploymentsDao;
  private final CommittersDao committersDao;
  private final TagsDao tagsDao;
  private final ModulesDao modulesDao;
  private final ClustersDao clustersDao;

  private final SVNGluJson svnGluJson;
  private final GluProxy gluProxy;

  public GluController(DeploymentUpdater deploymentUpdater,
                       DeploymentsDao deployments,
                       CommittersDao committersDao,
                       TagsDao tagsDao,
                       ModulesDao modulesDao,
                       ClustersDao clustersDao,
                       TeamCityApi teamCityApi,
                       SVNGluJson svnGluJson,
                       SvnApi svn,
                       GluProxy gluProxy) {
    Assert.notNull(deploymentUpdater, "deploymentUpdater is null");
    Assert.notNull(deployments, "deployments is null");
    Assert.notNull(committersDao, "commitersDao is null");
    Assert.notNull(tagsDao, "tagsDao is null");
    Assert.notNull(modulesDao, "modulesDao is null");
    Assert.notNull(clustersDao, "clustersDao is null");
    Assert.notNull(teamCityApi, "teamCityApi is null");
    Assert.notNull(svnGluJson, "svnGluJson is null");
    Assert.notNull(svn, "svn api is null");
    Assert.notNull(gluProxy, "gluProxy is null");
    this.deploymentUpdater = deploymentUpdater;
    this.deploymentsDao = deployments;
    this.committersDao = committersDao;
    this.tagsDao = tagsDao;
    this.modulesDao = modulesDao;
    this.teamCityApi = teamCityApi;
    this.svnGluJson = svnGluJson;
    this.svn = svn;
    this.gluProxy = gluProxy;
    this.clustersDao = clustersDao;
  }

  @RequestMapping("/deploy")
  public String deploy(final DeploymentFormBean deployInfo, final Map<String, Object> model)
      throws SVNException, HttpException, IOException, YummerRuntimeException, InterruptedException {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    if (deployInfo.getServiceName() == null) {
      model.put("gluJsonStr", svnGluJson.fetchGluJson());
    } else {
      long commitRevision = deployInfo.getVersion() != 0 ? deployInfo.getVersion()
                                                        : deployInfo.getCommitRevisionLong();
      String committerName = getCommitter(deployInfo.getCommitter(), commitRevision);
      Committer committer = committersDao.getOrCreate(committerName);
      Deployment deployment = new Deployment(modulesDao.getOrCreate(deployInfo.getServiceName()),
                                          deployInfo.getVersion(),
                                          tagsDao.getOrCreate(deployInfo.getTags()),
                                          committer,
                                          deployInfo.getCommitRevisionLong(),
                                          deployInfo.getCommitMessage());
      deploymentsDao.save(deployment);
      model.put("status", deployment);
      try {
        deployment.setStatus(DeploymentStatus.WORKING_SVN);
        deployment.setSource(deployInfo.getSource());
        deployment.setPrepareOnly(deployInfo.isPrepareOnly());
        deploymentsDao.save(deployment);
        deploymentUpdater.init();
        String jsonDiff = deploymentUpdater.applyTransformation(deployInfo.getServiceName(),
            deployInfo.getTags(), deployInfo.getVersion());
        if (!"[]".equals(jsonDiff.trim().replaceAll(" ", ""))) {
          log.info("Commiting to scm...");
          log.debug("Commiting this file to scm: {}", deploymentUpdater.getJsonModel());
          deployment.setJsonScmRevision(deploymentUpdater.commitToScm(deployInfo.getCommitter(),
              deployInfo.getCommitMessage()));
          deployment.setJsonModel(deploymentUpdater.getJsonModel());
          deployment.setJsonDiff(deploymentUpdater.getJsonDiff());
          deployment.setClusters(clustersDao.getOrCreate(deploymentUpdater.getClusters()));
          deploymentsDao.save(deployment);
        } else {
          log.info("Not committing to scm. There are no diffs.");
        }
        model.put("jsonDiff", jsonDiff);
        model.put("form", deployInfo);
        if (deployInfo.isSubmitToGlu()) {
          String tags = deployInfo.getTags() + (deployment.isPrepareOnly() ? " #prepareonly" : "");
          deploymentUpdater.tellYammerAndNagios(deployInfo.getServiceName(), tags,
              deployInfo.getVersion(), deployment.getCurrentPhaseGluExecutionId(),
              committer, deployInfo.getCommitMessage());
          deploymentUpdater.submitToGlu(deployment);
          if (deployment.isPrepareOnly()) {
            deployment.endWithStatus(DeploymentStatus.PREPARE_ONLY_DONE);
            return "success";
          }
        } else {
          deployment.endWithStatus(DeploymentStatus.SUCCESS_SVN_ONLY);
          deploymentsDao.save(deployment);
        }
        return "success";
      } catch (RuntimeException e) {
        deployment.setError(e);
        deploymentsDao.save(deployment);
        throw e;
      } catch (SVNException e) {
        deployment.setError(e);
        deploymentsDao.save(deployment);
        throw e;
      } catch (HttpException e) {
        deployment.setError(e);
        deploymentsDao.save(deployment);
        throw e;
      } catch (IOException e) {
        deployment.setError(e);
        deploymentsDao.save(deployment);
        throw e;
      } catch (InterruptedException e) {
        deployment.setError(e);
        deploymentsDao.save(deployment);
        throw e;
      } catch (YummerRuntimeException e) {
        deployment.setError(e);
        deploymentsDao.save(deployment);
        throw e;
      }
    }
    return "deployment";
  }

  private String getCommitter(String committer, long commitRevision) throws SVNException {
    if (committer == null || committer.isEmpty() || committer.equals("svnsync") ||
        committer.equals("Don't know")) {
      committer = svn.getCommitterForRevision(commitRevision);
    }
    return committer;
  }

  @RequestMapping("/buildanddeploy")
  public String buildAndDeploy(final DeploymentFormBean deployInfo, final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("form", deployInfo);
    model.put("teamcUrl", teamCityApi.getServerUrl());
    if (deployInfo.getServiceName() == null) {
      try {
        model.put("gluJsonStr", svnGluJson.fetchGluJson());
      } catch (SVNException e) {
        model.put("exception", e);
        return "error";
      }
    } else {
      try {
        teamCityApi.build(deployInfo.getServiceName(), deployInfo.getTags(),
            deployInfo.getCommitter(), deployInfo.getCommitMessage(), deployInfo.isPrepareOnly());
      } catch (NullPointerException e) {
        model.put("exception", e);
        return "error";
      } catch (HttpException e) {
        model.put("exception", e);
        return "error";
      } catch (IOException e) {
        model.put("exception", e);
        return "error";
      }
      return "successTeamC";
    }
    return "submitTeamC";
  }

  @RequestMapping("/status")
  public String status(StatusFormBean params, final Map<String, Object> model)
      throws SVNException, HttpException, IOException {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("sortedDeployments", deploymentsDao.listDeploymentsReverseChronological(false, params.getLimit()));
    model.put("limit", params.getLimit());
    return "status";
  }

  @RequestMapping("/modules")
  public String modules(ModulesFormBean params, final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("limit", params.getLimit());
    Collection<Module> modules;
    if (params.getModules() != null) {
      modules = new ArrayList<Module>();
      for (String m : params.getModules().split(",")) {
        modules.add(modulesDao.element(m));
      }
    } else {
      model.put("today", "deployed TODAY");
      modules = modulesDao.listToday();
    }
    model.put("modules", modules);
    return "modules";
  }

  @RequestMapping("/tags")
  public String tags(TagsFormBean params, final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("limit", params.getLimit());
    Collection<Tag> tags = new ArrayList<Tag>();
    if (params.getTags() != null) {
      for (String t : params.getTags().split(",")) {
        tags.add(tagsDao.element(t));
      }
    } else {
      model.put("today", "deployed TODAY");
      tags = tagsDao.listToday();
    }
    model.put("tags", tags);
    return "tags";
  }

  @RequestMapping("/clusters")
  public String clusters(ClustersFormBean params, final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("limit", params.getLimit());
    Collection<Cluster> clusters = new ArrayList<Cluster>();
    if (params.getClusters() != null) {
      for (String c : params.getClusters().split(",")) {
        clusters.add(clustersDao.element(c));
      }
    } else {
      model.put("today", "deployed TODAY");
      clusters = clustersDao.listToday();
    }
    model.put("clusters", clusters);
    return "clusters";
  }

  @RequestMapping("/committers")
  public String commiters(CommittersFormBean params, final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("limit", params.getLimit());
    List<Committer> committers = new ArrayList<Committer>();
    if (params.getCommitters() != null) {
      for (String t : params.getCommitters().split(",")) {
        committers.add(committersDao.element(t));
      }
    } else {
      committers = committersDao.listAll();
    }
    model.put("committers", committers);
    return "committers";
  }

  @RequestMapping("/histo")
  public String histo(final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    // Crate a date span of two weeks
    GregorianCalendar start = new GregorianCalendar();
    GregorianCalendar end = new GregorianCalendar();
    start.add(Calendar.DAY_OF_MONTH, -14);
    model.put("histo", deploymentsDao.getDeploymentsHistogramByDate(start.getTime(), end.getTime()));
    return "histo";
  }

  @RequestMapping("/timeline")
  public String timeline(final Map<String, Object> model) {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    List<Deployment> deployments = deploymentsDao.listDeployments(true);
    model.put("data", extractAnnotatedTimelineData(deployments));
    return "timeline";
  }

  private AnnotatedTimelineData extractAnnotatedTimelineData(List<Deployment> deployments) {
    return new AnnotatedTimelineData(deployments);
  }

  @RequestMapping("/versions")
  public String versions(final Map<String, Object> model) throws HttpException, IOException {
    model.put("gluServer", deploymentUpdater.getGluUrl());
    model.put("data", gluProxy.getLiveVersionsHisto());
    return "versions";
  }

  @RequestMapping("/cancel")
  public String cancel(final DeploymentIdFormBean params, final Map<String, Object> model, HttpServletRequest request)
      throws SVNException, HttpException, IOException {
    deploymentsDao.cancel(params.getId());
    return redirectToReferer(request);
  }

  @RequestMapping("/archive")
  public String archive(final DeploymentIdFormBean params, final Map<String, Object> model, HttpServletRequest request)
      throws SVNException, HttpException, IOException {
    deploymentsDao.archive(params.getId());
    return redirectToReferer(request);
  }

  private String redirectToReferer(HttpServletRequest request) {
    String referer = request.getHeader("Referer");
    return "redirect:" + referer;
  }

  @RequestMapping("/delete")
  public String delete(final DeploymentIdFormBean params, final Map<String, Object> model, HttpServletRequest request)
      throws SVNException, HttpException, IOException {
    deploymentsDao.delete(params.getId());
    return redirectToReferer(request);
  }
}
