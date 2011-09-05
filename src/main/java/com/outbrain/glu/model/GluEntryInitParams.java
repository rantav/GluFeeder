package com.outbrain.glu.model;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * @author ran
 *
 */
public class GluEntryInitParams {

  JsonNode elem;

  public GluEntryInitParams(JsonNode jsonNode) {
    this.elem = jsonNode;
  }

  @Override
  public String toString() {
    return elem.toString();
  }

  public void setVersion(long version) {
    JsonNode value  = new LongNode(version);
    ((ObjectNode)elem).put("version", value);
  }
}
