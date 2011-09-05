package com.outbrain.glu.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

/**
 *
 * @author Itai
 *
 */
public class SVNUpdater {

  private final SVNRepository repository;
  private final String path;
  private final String oldData;


  public SVNUpdater(SVNRepository repository, String path) throws SVNException {
    super();
    this.repository = repository;
    this.path = path;
    SVNProperties fileProperties = new SVNProperties();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    repository.getFile(path, -1, fileProperties, baos);
    oldData = new String(baos.toByteArray());
  }

  public String getFileContent(){
    return oldData;
  }

  /**
   *
   * @param newData
   * @param username name of the user committing
   * @param message message from this user.
   * @return the new revision number
   * @throws SVNException
   */
  public long commit(String newData, String username, String message) throws SVNException {
    // url escape the message (since the svn client doesn't).
    try {
      message = URLEncoder.encode(message, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    ISVNEditor editor = repository.getCommitEditor("Glu model update By " + username +
        " Message: " + message , null );
    SVNCommitInfo ci = modifyFile(editor, path.substring(0, path.lastIndexOf("/")), path,
        oldData.getBytes(), newData.getBytes());
    return ci.getNewRevision();
  }


  private SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath, String filePath,
      byte[] oldData, byte[] newData) throws SVNException {
    editor.openRoot(-1);

    editor.openDir(dirPath, -1);

    editor.openFile(filePath, -1);

    editor.applyTextDelta(filePath, null);

    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0,
        new ByteArrayInputStream(newData), editor, true);

    // Closes filePath.
    editor.closeFile(filePath, checksum);

    // Closes dirPath.
    editor.closeDir();

    // Closes the root directory.
    editor.closeDir();

    return editor.closeEdit();
  }

}
