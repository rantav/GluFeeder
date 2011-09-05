package com.outbrain.glu.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

public class SVNUpdaterFactory {

  private SVNRepository svnRepository;
  
  public SVNUpdaterFactory(SVNRepository svnRepository) {
    super();
    this.svnRepository = svnRepository;
  }
  
  public SVNUpdater createUpdater(String path) throws SVNException{
    return new SVNUpdater(svnRepository, path);
    
  }

}
