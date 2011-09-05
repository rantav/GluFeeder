package com.outbrain.glu.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import com.outbrain.glu.data.Module;
import com.outbrain.glu.data.Tag;

public class GluScopeTest {

  @SuppressWarnings("serial")
  @Test
  public void testGluScopeStringStringString() {
    GluScope scope = new GluScope(Module.fromString(" p1   ,p2 "), 123,
        Tag.fromString(" t1  ,  t2"));
    assertEquals("123", scope.getVersion());
    assertEquals(new HashSet<Tag>() {
      {
        add(new Tag("t1"));
        add(new Tag("t2"));
      }
    }, scope.getTags());
    assertEquals(new HashSet<Module>() {
      {
        add(new Module("p1"));
        add(new Module("p2"));
      }
    }, scope.getModules());
  }

  @Test
  public void testFilter_noneMatch() throws JsonParseException, JsonMappingException, IOException,
      URISyntaxException {
    // top secret :)
  }

  @Test
  public void testFilter_someMatch() throws JsonParseException, JsonMappingException, IOException,
      URISyntaxException {
    // top secret :)
  }

  @Test
  public void testGetDsl() throws JsonParseException, JsonMappingException, IOException,
      URISyntaxException {
    // top secret :)
  }
}
