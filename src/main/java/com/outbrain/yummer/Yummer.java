package com.outbrain.yummer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.outbrain.glu.data.Module;

/**
 * An API to the yum command line utility.
 * <p>
 * This class calls the operating system's yum command. So it needs to run on a station where yum
 * is installed.
 * <p>
 * The the most up to date response from yum we disable caching for the repositories which are
 * used by setting metadata_expire=1.
 <pre>
 cat /etc/yum.conf
[main]
...
metadata_expire=1
...
 </pre>
 *
 * @author Ran Tavory
 *
 */
public class Yummer {

  private static final Logger log = LoggerFactory.getLogger(Yummer.class);
  private final Runtime runtime;

  public Yummer(Runtime runtime) {
    Assert.notNull(runtime, "Runtime is null");
    this.runtime = runtime;
  }

  /**
   * Checks if the module exists in ANY of the repos.
   *
   * @param module
   * @param version
   * @return
   * @throws YummerRuntimeException
   * @throws InterruptedException
   * @throws IOException
   */
  public boolean checkModuleExistanceInRepos(Module module, String version, Set<String> repos)
      throws IOException, InterruptedException, YummerRuntimeException {
    Assert.notNull(module, "module is null");
    Assert.notNull(version, "version is null");
    Assert.notNull(repos, "repos is null");
    Assert.notEmpty(repos);

    Map<String, Boolean> repoExistance = new HashMap<String, Boolean>();
    List<RpmInfo> rpms = list(module);
    for (RpmInfo rpm : rpms) {
      if (module.getName().equalsIgnoreCase(rpm.getModule()) && version.equals(rpm.getVersion())) {
        repoExistance.put(rpm.getRepo(), true);
      }
    }

    for (String repo: repos) {
      if (repoExistance.containsKey(repo)) {
        return true; // at least one of the repos has the rpm
      }
    }
    return false;
  }

  /**
   * Lists the available yum versions of <code>moduleName</code>
   *
   * @param module
   * @return
   * @throws IOException
   * @throws InterruptedException
   * @throws YummerRuntimeException
   */
  public List<RpmInfo> list(Module module) throws IOException, InterruptedException,
      YummerRuntimeException {
    Assert.notNull(module, "module is null");
    String yumPath = System.getProperty("yum-path", "yum");
    List<String> lines = exec(yumPath + " list " + module.getName());
    List<RpmInfo> list = extractRpmDetails(lines);
    log.debug("Rpm details: {}", list);
    return list;
  }

  /**
   * Extract rpm details from
   *
   * @param r
   * @return
   */
  private List<RpmInfo> extractRpmDetails(List<String> lines) {
    List<RpmInfo> list = new ArrayList<RpmInfo>();
    for (String line: lines) {
        RpmInfo d = extractRpmDetail(line);
        if (d != null) {
          list.add(d);
        }
      }
    return list;
  }

  /**
   * Extracts details from lines such as:
   *
   * ImageServer.noarch 31218-1 installed
   *
   * @param line
   * @return
   */
  private RpmInfo extractRpmDetail(String line) {
    Assert.notNull(line);
    Pattern linePattern = Pattern.compile("([^\\s]+)\\.noarch\\s+([^\\s]+)-1\\s+([^\\s]+)");
    Matcher m = linePattern.matcher(line);
    if (!m.find() || m.groupCount() < 3) {
      log.debug("Not found in this line: {}", line);
      return null;
    }
    String module = m.group(1);
    String version = m.group(2);
    String repo = m.group(3);
    return new RpmInfo(module, version, repo);
  }

  /**
   * Executes the command and returns a list of strings read from its output, one string per line.
   * @param cmd
   * @return
   * @throws IOException
   * @throws InterruptedException
   * @throws YummerRuntimeException
   */
  @SuppressWarnings("unchecked")
  private List<String> exec(String cmd) throws IOException, InterruptedException,
      YummerRuntimeException {
    Process pr = runtime.exec(cmd);
    pr.waitFor();
    if (pr.exitValue() != 0 || pr.getInputStream() == null) {
      throw new YummerRuntimeException(cmd, pr.exitValue(), streamToString(pr.getInputStream()),
          streamToString(pr.getErrorStream()));
    }
    return IOUtils.readLines(pr.getInputStream());
  }

  /**
   * Converts an input stream to a string, preserving newlines and all.
   *
   * @param stream
   * @return
   */
  private String streamToString(InputStream stream) {
    if (stream == null) {
      return null;
    }
    try {
      return IOUtils.toString(stream);
    } catch (IOException e) {
      log.error("Upable to read the stream " + stream, e);
      return null;
    }
  }
}
