package com.outbrain.glu.data;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author ran
 *
 */
public class HibernateCommittersDao extends HibernateDaoSupport implements CommittersDao {

  @Override
  public Committer element(String name) {
    @SuppressWarnings("unchecked")
    List<Committer> tags = getHibernateTemplate().find("from Committer where name = ?", name);
    if (tags == null || tags.isEmpty()) {
      return null;
    }
    return tags.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Committer> listAll() {
    return getHibernateTemplate().find("from Committer");
  }

  @Override
  public Committer getOrCreate(String committerId) {
    Committer c = element(committerId);
    if (c == null) {
      c = new Committer(committerId);
      getHibernateTemplate().saveOrUpdate(c);
    }
    return c;
  }

}
