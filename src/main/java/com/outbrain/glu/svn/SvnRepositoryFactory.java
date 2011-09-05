package com.outbrain.glu.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 *
 * @author Itai
 *
 */
public class SvnRepositoryFactory {
  static {
    DAVRepositoryFactory.setup();
  }

  /**
   * Creates a repository connection with the given credentials.
   * <p>
   * The credentials are not validated during constructions, they will only be validated on first
   * usage after this method exist.
   * @param url
   * @param user
   * @param pass
   * @return
   * @throws SVNException
   */
  public SVNRepository create(String url, String user, String pass) throws SVNException {
    SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
    ISVNAuthenticationManager authManager = new BasicAuthenticationManager(user, pass);
    repository.setAuthenticationManager(authManager);
    return repository;
  }
}
