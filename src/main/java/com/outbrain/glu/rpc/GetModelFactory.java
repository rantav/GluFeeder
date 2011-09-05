package com.outbrain.glu.rpc;

/**
 * Creates new instances of {@link GetModel}.
 * Uses the service locator pattern.
 * @author Ran Tavory
 *
 */
public interface GetModelFactory {
  GetModel getService();
}
