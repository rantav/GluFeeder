package com.outbrain.glu.rpc;


/**
 * Creates instances of ExecutionStatusReader
 *
 * @author Ran Tavory
 *
 */
public interface ExecutionStatusReaderFactory {

  /** Gets an instance of ExecutionStatusReader as defined by Spring */
  public ExecutionStatusReader getService();
}
