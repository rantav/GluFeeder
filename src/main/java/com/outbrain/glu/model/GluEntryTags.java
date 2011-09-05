package com.outbrain.glu.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.jackson.JsonNode;

/**
 * 
 * @author ran
 *
 */
public class GluEntryTags {

  private final JsonNode elem;

  public GluEntryTags(JsonNode n) {
    elem = n;
  }

  @Override
  public String toString() {
    return elem == null ? null : elem.toString();
  }

  public Set<String> getTags() {
    if (elem == null) {
      return Collections.emptySet();
    }
    Set<String> tags = new HashSet<String>();
    JsonNode n;
    for (Iterator<JsonNode> it = elem.getElements(); it.hasNext(); ) {
      n = it.next();
      tags.add(n.getTextValue());
    }
    return tags;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    GluEntryTags rhs = (GluEntryTags) obj;
    return rhs.getTags().equals(getTags());
   }

  @Override
  public int hashCode() {
    return getTags().hashCode();
  }
}
