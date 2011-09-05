package com.outbrain.glu.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonNode;

import com.outbrain.glu.data.Cluster;

/**
 * 
 * @author ran
 *
 */
public class GluEntry {

  private static final String DEFAULT_STATE_ASSUMED = "running";
  private final JsonNode elem;
  private final GluAgent agent;
  private final GluEntryMetadata metadata;
  private final GluEntryInitParams initParameters;
  private final GluEntryTags tags;
  private final String entryState;

  public GluEntry(JsonNode elem) {
    this.elem = elem;
    agent = new GluAgent(elem.get("agent"));
    metadata = new GluEntryMetadata(elem.get("metadata"));
    initParameters = new GluEntryInitParams(elem.get("initParameters"));
    tags = new GluEntryTags(elem.get("tags"));
    JsonNode entryStateNode = elem.get("entryState");
    entryState = entryStateNode == null ? DEFAULT_STATE_ASSUMED : entryStateNode.getTextValue();
  }

  @Override
  public String toString() {
    return elem.toString();
  }

  public Set<String> getTags() {
    Set<String> tagSet = new HashSet<String>();
    // And from the new location
    tagSet.addAll(tags.getTags());
    return tagSet;
  }

  public String getServiceName() {
    return metadata.getProduct();
  }

  public GluEntryMetadata getMetadata() {
    return metadata;
  }

  public boolean updateVersion(long version) {
    if (version == metadata.getVersion()) {
      return false;
    }
    metadata.setVersion(version);
    initParameters.setVersion(version);
    return true;
  }

  public JsonNode getJsonNode() {
    return elem;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GluEntry)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    GluEntry rhs = (GluEntry) obj;
    return new EqualsBuilder().append(agent, rhs.agent)
                              .append(metadata, rhs.metadata)
                              .append(tags, rhs.tags)
                              .append(entryState, rhs.entryState)
                              .isEquals();

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(agent).append(metadata).append(tags).toHashCode();
  }

  public GluAgent getAgent() {
    return agent;
  }

  public GluEntryInitParams getInitParameters() {
    return initParameters;
  }

  public String getCluster() {
    return metadata.getCluster();
  }

  public boolean isProduction() {
    String cluster = getCluster();
    if (cluster == null || "".equals(cluster)) {
      return false;
    }
    return Cluster.isProduction(cluster);
  }
}
