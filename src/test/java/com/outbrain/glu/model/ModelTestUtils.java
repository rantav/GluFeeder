package com.outbrain.glu.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.outbrain.glu.rpc.ModelType;

public class ModelTestUtils {

  /**
   * Reads a file from the classpath
   *
   * @param fileName
   * @return
   * @throws URISyntaxException
   * @throws IOException
   */
  public static String read(String fileName) throws IOException, URISyntaxException {
    return FileUtils.readFileToString(
        new File(ModelTestUtils.class.getClassLoader().getResource(fileName).toURI()), "UTF-8");
  }

  public static GluModel readModel(String fileName) throws JsonParseException,
      JsonMappingException, IOException, URISyntaxException {
    return new GluModel(read(fileName), ModelType.DESIRED);
  }
}
