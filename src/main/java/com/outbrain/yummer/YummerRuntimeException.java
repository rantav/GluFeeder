package com.outbrain.yummer;

@SuppressWarnings("serial")
public class YummerRuntimeException extends Exception {

  private final String cmd;
  private final int status;
  private final String stdout;
  private final String stderr;

  public YummerRuntimeException(String cmd, int status, String stdout, String stderr) {
    this.cmd = cmd;
    this.status = status;
    this.stdout = stdout;
    this.stderr = stderr;
  }

  @Override
  public String getMessage() {
    return String.format("Command %s exited with status %d.\n stdout: %s\n stderr: %s", cmd,
        status, stdout, stderr);
  }

  @Override
  public String getLocalizedMessage() {
    return getMessage();
  }


}
