package com.outbrain.glu.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class GluStringUtils {

  /**
   * Returns a list of strings after splitting and trimming the commaDelimited input.
   * If the input is empty or null returns an empty list
   * @param comaDelimited
   * @return
   */
  public static Set<String> splitTrim(String commaDelimited) {
    if (StringUtils.isEmpty(commaDelimited)) {
      return Collections.emptySet();
    }
    String[] split = commaDelimited.split(",");
    return new HashSet<String>(Arrays.asList(StringUtils.stripAll(split)));
  }
}
