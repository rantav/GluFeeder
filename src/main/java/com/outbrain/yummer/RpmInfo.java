package com.outbrain.yummer;

/**
 * Details of an RPM file.
 *
 * @author Ran Tavory
 *
 */
public class RpmInfo {

  private final String module;
  private final String version;
  private final String repo;

  public RpmInfo(String module, String version, String repo) {
    this.module = module;
    this.version = version;
    this.repo = repo;
  }

  public String getModule() {
    return module;
  }

  public String getVersion() {
    return version;
  }

  public String getRepo() {
    return repo;
  }

  @Override
  public String toString() {
    return module + "-" + version + ":" + repo;
  }
}
