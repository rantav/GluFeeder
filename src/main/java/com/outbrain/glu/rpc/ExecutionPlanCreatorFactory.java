package com.outbrain.glu.rpc;

/**
 * Creates new instances of {@link ExecutionPlanCreator}.
 * Uses the service locator pattern.
 * @author Ran Tavory
 *
 */
public interface ExecutionPlanCreatorFactory {

  public ExecutionPlanCreator getService();

}
