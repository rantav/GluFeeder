package com.outbrain.glu.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateTagsDao extends HibernateDaoSupport implements TagsDao {

  @Override
  public Tag element(String name) {
    @SuppressWarnings("unchecked")
    List<Tag> tags = getHibernateTemplate().find("from Tag where name = ?", name);
    if (tags == null || tags.isEmpty()) {
      return null;
    }
    return tags.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Tag> listAll() {
    return getHibernateTemplate().find("from Tag");
  }

  @Override
  public Set<Tag> getOrCreate(String tagsList) {
    Set<Tag> localTags = Tag.fromString(tagsList);
    @SuppressWarnings("unchecked")
    List<Tag> dbTagsList = getHibernateTemplate().findByNamedParam(
        "from Tag where name in (:tagsList)", "tagsList", tagsList.split(","));
    Set<Tag> dbTags = new HashSet<Tag>(dbTagsList);
    Set<Tag> missingTags = new HashSet<Tag>(localTags);
    missingTags.removeAll(dbTags);
    getHibernateTemplate().saveOrUpdateAll(missingTags);
    dbTags.addAll(missingTags);
    return dbTags;
  }

  @Override
  public Set<Tag> listToday() {
    @SuppressWarnings("unchecked")
    List<Deployment> deployments = getHibernateTemplate().find("from Deployment d where d.startTime >= TODAY");
    Map<String, Tag> tagsMap = new HashMap<String, Tag>();
    Set<Tag> ret = new HashSet<Tag>();
    for (Deployment deployment : deployments) {
      Set<Tag> tags = deployment.getTags();
      for (Tag tag : tags) {
        Tag minimalTag = tagsMap.get(tag.getName());
        if (minimalTag == null) {
          minimalTag = new Tag(tag.getName());
          tagsMap.put(minimalTag.getName(), minimalTag);
          ret.add(minimalTag);
        }
        minimalTag.getDeployments().add(deployment);
      }
    }
    return ret;
  }

}
