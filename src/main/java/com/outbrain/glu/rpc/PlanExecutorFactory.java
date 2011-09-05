package com.outbrain.glu.rpc;


/**
 * Creates instances of PlanExecutor
 * Uses Sprin'g service locator pattern.
 *
 * @author Ran Tavory
 *
 */
public interface PlanExecutorFactory {

  /** Gets an instance of PlanExecutor as defined by Spring */
  public PlanExecutor getService();
}
