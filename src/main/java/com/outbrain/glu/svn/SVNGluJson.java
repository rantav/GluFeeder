package com.outbrain.glu.svn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * This class used SVNUpdater to retrieve the glu.json file as string
 *
 * @author batels
 *
 */
public class SVNGluJson {

  private final String repoUrl;
  private final String username ;
  private final String pass;
  private final String path;
  private final SvnRepositoryFactory factory;
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(SVNGluJson.class);

  public SVNGluJson(String repoUrl, String username, String pass, String path, SvnRepositoryFactory factory) {
    Assert.notNull(repoUrl, "repoUrl is null");
    Assert.notNull(username, "username is null");
    Assert.notNull(pass, "pass is null");
    Assert.notNull(path, "path is null");
    Assert.notNull(factory, "factory is null");
    this.repoUrl = repoUrl;
    this.username = username;
    this.pass = pass;
    this.path = path;
    this.factory = factory;
  }

  public String fetchGluJson() throws SVNException {
    SVNRepository repository = factory.create(repoUrl, username, pass);
    SVNUpdaterFactory updaterFactory = new SVNUpdaterFactory(repository);
    SVNUpdater updater = updaterFactory.createUpdater(path);
    return updater.getFileContent();
  }

}
