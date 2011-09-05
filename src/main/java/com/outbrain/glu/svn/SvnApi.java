package com.outbrain.glu.svn;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 *
 * @author Ran Tavory
 *
 */
public class SvnApi {

  private final String repoUrl;
  private final String username ;
  private final String pass;
  private final SvnRepositoryFactory factory;
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(SVNGluJson.class);

  public SvnApi(String repoUrl, String username, String pass, SvnRepositoryFactory factory) {
    Assert.notNull(repoUrl, "repoUrl is null");
    Assert.notNull(username, "username is null");
    Assert.notNull(pass, "pass is null");
    Assert.notNull(factory, "factory is null");
    this.repoUrl = repoUrl;
    this.username = username;
    this.pass = pass;
    this.factory = factory;
  }

  public String getCommitterForRevision(long commitRevision) throws SVNException {
    SVNRepository repository = factory.create(repoUrl, username, pass);
    @SuppressWarnings("rawtypes")
    Collection logEntries = repository.log(new String[] {""}, null, commitRevision,
        commitRevision, true, true);
    Assert.notNull(logEntries, "logEntries are null for revision " + commitRevision);
    SVNLogEntry entry = (SVNLogEntry) logEntries.iterator().next();
    return entry.getAuthor();
  }
}
