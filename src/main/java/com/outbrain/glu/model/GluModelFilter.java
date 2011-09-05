package com.outbrain.glu.model;

import java.util.Collections;
import java.util.Set;

import com.outbrain.glu.data.Module;
import com.outbrain.glu.data.Tag;

public class GluModelFilter {

  final private Set<Module> modules;
  final private Set<Tag> tags;

  public GluModelFilter(Set<Module> modules, Set<Tag> tags) {
    this.modules = Collections.unmodifiableSet(modules);
    this.tags = Collections.unmodifiableSet(tags);
  }


  /**
   * Checks if the service should be included by this filter.
   * This means that its tags and its services name were provided.
   * @param service
   * @return True of the serivce is included in the service list and the tags match.
   */
  public boolean isIncluded(GluEntry service) {
    if (!modules.contains(new Module(service.getServiceName()))){
      return false;
    }
    if (tags.isEmpty()){
      return true;
    }
    Set<String> stringTagSet = service.getTags();
    Set<Tag> tagSet = Tag.fromStringSet(stringTagSet);
    tagSet.retainAll(tags);
    return !tagSet.isEmpty();
  }

  @Override
  public String toString() {
    return modules.toString() + tags.toString();
  }
}
