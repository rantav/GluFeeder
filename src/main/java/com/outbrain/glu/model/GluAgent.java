package com.outbrain.glu.model;

import org.codehaus.jackson.JsonNode;

/**
 * 
 * @author ran
 *
 */
public class GluAgent {

  private final JsonNode elem;

  public GluAgent(JsonNode jsonNode) {
    this.elem = jsonNode;
  }

  @Override
  public String toString() {
    return elem.toString();
  }

  public String getName() {
    return elem.getTextValue();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GluAgent)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    return ((GluAgent)obj).getName().equals(getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

}
