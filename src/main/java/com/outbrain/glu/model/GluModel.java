package com.outbrain.glu.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.outbrain.glu.rpc.ModelType;


/**
 * Abstracts glu's model tree.
 * @author Ran Tavory
 *
 */
public class GluModel {

  private final Logger log = LoggerFactory.getLogger(GluModel.class);

  private final JsonNode jsonModel;
  private final Set<GluEntry> entries = new HashSet<GluEntry>();
  private final ModelType type;

  public GluModel(String jsonStr, ModelType modelType) throws JsonParseException, JsonMappingException, IOException {
    Assert.notNull(jsonStr, "jsonStr is null");
    Assert.notNull(modelType, "modelType is null");
    this.type = modelType;
    ObjectMapper m = new ObjectMapper();
    jsonModel = m.readValue(jsonStr, JsonNode.class);
    JsonNode servicesArray = jsonModel.get("entries");
    for (int i = 0; i < servicesArray.size(); ++i){
      JsonNode elem = servicesArray.get(i);
      entries.add(new GluEntry(elem));
    }
  }

  public Set<GluEntry> update(GluModelFilter filter,long version){
    Set<GluEntry> changedServices = new HashSet<GluEntry>();
    for (GluEntry service: entries){
      if (filter.isIncluded(service)){
        boolean changed = service.updateVersion(version);
        if (changed){
          changedServices.add(service);
        }
      }
    }
    return changedServices;
  }

  /*package*/ Set<GluEntry> getEntries() {
    return Collections.unmodifiableSet(entries);
  }

  public String prettyPrint() {
    ObjectMapper m = new ObjectMapper();
    m.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      m.writeValue(stream, jsonModel);
      return new String(stream.toByteArray());
    } catch (JsonGenerationException e) {
      log.error("Can't pretty print. ", e);

    } catch (JsonMappingException e) {
      log.error("Can't pretty print. ", e);
    } catch (IOException e) {
      log.error("Can't pretty print. ", e);
    }
    return null;
  }

  @Override
  public String toString() {
    return type + ": " + prettyPrint();
  }

  public String toJsonString() {
    return prettyPrint();
  }

  /**
   * Counts the number of instances each service has in each DC.
   * The returned map is of DC-SERVICE -> Integer.
   * For example: ny-ImageServer -> 25
   *
   * @param jsonModel The model from which the number of instances are being counted.
   *
   * @return A map from DC-SERVICE to the number of occurrences of this service in the DC.
   */
  public Map<String, Integer> getDcServiceCount() {
    Map<String, Integer> map = new HashMap<String, Integer>();
    for (GluEntry entry: entries) {
      String cluster = entry.getCluster();
      String service = entry.getServiceName();
      String key = cluster + "-" + service;
      if (map.get(key) == null) {
        map.put(key, 1);
      } else {
        map.put(key, map.get(key) + 1);
      }
    }
    return map;
  }

  public List<MaxMinVersion> getMinMaxVersions(String gluUrl) {
    MaxMinVersion.GLU_SERVER = gluUrl;
    Map<String, MaxMinVersion> moduleVersions = new HashMap<String, MaxMinVersion>();
    for (GluEntry entry : getEntries()) {
      // exclude non-production entries
      if (!entry.isProduction()) {
        continue;
      }
      String module = entry.getServiceName();
      int version = entry.getMetadata().getVersion();
      if (version <= 0) {
        continue;
      }
      String agent = entry.getAgent().getName();
      MaxMinVersion maxMin = moduleVersions.get(module);
      if (maxMin == null) {
        Set<String> agents = new HashSet<String>();
        agents.add(agent);
        maxMin = new MaxMinVersion(module, version, version, agents, agents);
        moduleVersions.put(module, maxMin);
      }
      maxMin.addVersion(version, agent);
    }
    List<MaxMinVersion> ret = new ArrayList<MaxMinVersion>(moduleVersions.values());
    return ret;
  }
}
