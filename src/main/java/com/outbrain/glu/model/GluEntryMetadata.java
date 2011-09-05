package com.outbrain.glu.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * @author ran
 *
 */
public class GluEntryMetadata {
  private final JsonNode elem;

  public GluEntryMetadata(JsonNode jsonNode) {
    this.elem = jsonNode;
  }

  @Override
  public String toString() {
    return elem.toString();
  }

  public String getProduct(){
    return getValue("product");
  }

  public int getVersion(){
    JsonNode node = elem.get("version");
    return node == null ? -1 : node.getIntValue();
  }

  public void setVersion(long version){
    JsonNode value  = new LongNode(version);
    ((ObjectNode)elem).put("version", value);
  }


  private String getValue(String name){
    JsonNode strElem = elem.get(name);
    return strElem == null ? "" : strElem.getTextValue();
  }

  public String getCluster() {
    return getValue("cluster");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GluEntryMetadata)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    GluEntryMetadata rhs = (GluEntryMetadata) obj;
    return new EqualsBuilder().append(getCluster(), rhs.getCluster()).
        append(getVersion(), rhs.getVersion()).
        append(getProduct(), rhs.getProduct()).isEquals();
    }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getCluster()).append(getVersion()).append(getProduct())
                                .toHashCode();
  }
}
