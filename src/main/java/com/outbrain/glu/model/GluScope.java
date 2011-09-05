package com.outbrain.glu.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.outbrain.glu.data.Module;
import com.outbrain.glu.data.Tag;



/**
 * Defines a glu scope.
 * The scope is used when performing a diff and wanting to limit the diff's scope for comparison.
 * The scope is determined by the #deploy and #to tags in the commit.
 *
 * @author Ran Tavory
 *
 */
public class GluScope {

  private final Set<Module> modules;
  private final String version;
  private final Set<Tag> tags;

  public GluScope() {
    modules = new HashSet<Module>();
    version = null;
    tags = new HashSet<Tag>();
  }

  @SuppressWarnings("unchecked")
  public GluScope(Set<Module> modules, long version, Set<Tag> tags) {
    this.modules = modules == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(modules);
    this.version = String.valueOf(version);
    this.tags = tags == null ? Collections.EMPTY_SET: Collections.unmodifiableSet(tags);
  }

  /**
   * Filters the set of entries to be confined to this scope only.
   *
   * @param entries
   * @return
   */
  public Set<GluEntry> filter(Set<GluEntry> entries) {
    GluModelFilter filter = new GluModelFilter(modules, tags);
    Set<GluEntry> ret = new HashSet<GluEntry>();
    for (GluEntry e: entries) {
      if (filter.isIncluded(e)) {
        ret.add(e);
      }
    }
    return ret;
  }

  /**
   * Creates a system filter compatible to glu 2.0.
   * See "Filter Syntax" at https://github.com/linkedin/glu/wiki/Console
   * The filter is of the sorts:
   *
   *  <code>tags.hasAny('t1;t2');or{metadata.service='s1';metadata.service='s2'}</code>
   *
   * Where in this example tags was "t1,t2" and services was "s1,s2"
   *
   * @return A glu DSL string
   */
  public String getDsl() {
    StringBuffer sb = new StringBuffer();
    if (modules.size() > 1) {
      sb.append("or{");
    }
    for (Module module: modules) {
      sb.append("metadata.product='");
      sb.append(module.getName());
      sb.append("';");
    }
    if (modules.size() > 1) {
      sb.append("};");
    }

    sb.append("tags.hasAny('");
    for (Tag tag: tags) {
      sb.append(tag);
      sb.append(";");
    }
    sb.append("')");
    return sb.toString();
  }

  public Set<Module> getModules() {
    return modules;
  }

  public String getVersion() {
    return version;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  @Override
  public String toString() {
    return getDsl();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GluScope)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    GluScope other = (GluScope) obj;
    return toString().equals(other.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
