package com.outbrain.glu.data;

import java.util.List;
import java.util.Set;

/**
 *  
 * @author ran
 *
 */
public interface TagsDao {

  /** Get a by ID (it's name, e.g. la or ny)*/
  Tag element(String id);

  List<Tag> listAll();

  /**
   * Gets the set of tags from the DB and creates new tags for any tag that doesn't exist yet.
   * @param string a com  ma delimited list of tag names
   * @return
   */
  Set<Tag> getOrCreate(String tagsList);

  Set<Tag> listToday();
}
