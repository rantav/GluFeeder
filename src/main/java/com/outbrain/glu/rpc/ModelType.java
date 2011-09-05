package com.outbrain.glu.rpc;

/**
 * Model type to get when requesting glu
 * @author Ran Tavory
 *
 */
public enum ModelType {
  /** The actual live model right now */
  LIVE("live"),
  /** The desired model, defined by glu's model file */
  DESIRED("model");

  /**
   * The URL path part that identifies this type.
   * for /system/model it's "model"
   * fir /system/live it's "live"
   */
  private final String urlPart;

  private ModelType(String urlPart) {
    this.urlPart = urlPart;
  }
  public String getUrlPart() {
    return urlPart;
  }
}
