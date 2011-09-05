package com.outbrain.glu.data;

import java.util.List;

public interface CommittersDao {

  /**
   * Get a by ID (it's name ran, eran, erez)
   * @return The method may return null (if the user committer doesn't exist)
   */
  Committer element(String id);

  List<Committer> listAll();

  /**
   * Gets the committer from the DB if the committer exists.
   * If not, creates a new committer, saves it to the DB and returns it
   */
  Committer getOrCreate(String committerId);

}
