package com.outbrain.glu.model;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Implements a diff for two glu models. The diff is limited by the diff scope.
 * <p>
 * The diff is only calculated per the elements are that interesting for outbrain's deployment needs
 * it's not a generic diff algorithm.
 *
 * For a given list of entries the diff would look at all entries that are confined in the given
 * scope of product, tags and version at <code>desired</code> that are different from those at
 * <code>live</code>.
 * The result of a diff is a GluModelDiff which is essentially the list of entries from
 * <code>desired</code> that are different from the same list in <code>live</code> or that do not
 * exist in <code>live</code>.
 *
 * @author Ran Tavory
 */
public class GluModelDiffer {

  private static final Logger log = LoggerFactory.getLogger(GluModelDiffer.class);

  /**
   * Performs a diff between the desired model and the live model for all elements contained in the
   * given scope and returns a diff object containing all the elements that are in desired but not
   * in live, or that are different b/w desired and live.
   *
   * @param desired The desired model
   * @param live The live model
   * @param scope A scope to confine the diff
   *
   * @return A diff object that can be used to describe provide a glu DSL selector.
   */
  public GluModelDiff diff(GluModel desired, GluModel live, GluScope scope) {
    Assert.notNull(desired, "desired is null");
    Assert.notNull(live, "live is null");
    Assert.notNull(scope, "scope is null");
    Set<GluEntry> desiredEntries = scope.filter(desired.getEntries());
    Set<GluEntry> diff = new HashSet<GluEntry>(desiredEntries);
    diff.removeAll(live.getEntries());
    Set<GluEntry> liveDiff = retainOnlyLiveAgents(diff, live);
    return new GluModelDiff(liveDiff, scope);
  }

  /**
   * retains only the agents that exist in live, removing all the agents that are not in it.
   * Does not change diff or live, will return a new Set.
   * @param diff
   * @param live
   * @return
   */
  private Set<GluEntry> retainOnlyLiveAgents(Set<GluEntry> diff, GluModel live) {
    Set<GluEntry> ret = new HashSet<GluEntry>(diff.size());
    Set<String> liveAgentNames = extractAgentNames(live);
    for (GluEntry entry : diff) {
      String name = entry.getAgent().getName();
      if (liveAgentNames.contains(name)) {
        ret.add(entry);
      } else {
        log.info("The agent {} seems to be down!!! Will not deploy to it", name);
      }
    }
    return ret;
  }

  /**
   * Extracts just the agent names from the model.
   * @param model
   * @return
   */
  private Set<String> extractAgentNames(GluModel model) {
    Set<String> agents = new HashSet<String>();
    for (GluEntry entry : model.getEntries()) {
      log.debug("Adding agent {}", entry.getAgent().getName());
      agents.add(entry.getAgent().getName());
    }
    return agents;
  }
}
