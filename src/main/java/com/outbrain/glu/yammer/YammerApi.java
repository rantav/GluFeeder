package com.outbrain.glu.yammer;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.Assert;

import com.outbrain.glu.data.Committer;

/**
 * A simple "API" to yammer.
 * Really uses email, not real API, so functionality is pretty limited.
 * @author Ran Tavory
 *
 */
public class YammerApi {

  private final MailSender sender;
  private final String gluExecutionBaseUrl;
  private final String fromEmailAddress;

  public YammerApi(MailSender sender, String gluExecutionBaseUrl, String fromEmailAddress) {
    Assert.notNull(sender, "Sender is null");
    Assert.notNull(gluExecutionBaseUrl, "gluExecutionBaseUrl is null");
    Assert.notNull(fromEmailAddress, "fromEmailAddress is null");
    this.sender = sender;
    this.gluExecutionBaseUrl = gluExecutionBaseUrl;
    this.fromEmailAddress = fromEmailAddress;
  }

  public void postUpdate(String modules, String tags, long version, String gluExecutionId,
      Committer committer, String commitMessage) {
    if (System.getProperty("noMailServer") == null) {
      sender.send(createMessage(modules, tags, version, gluExecutionId, committer, commitMessage));
    }
  }

  private SimpleMailMessage createMessage(String modules, String tags, long version,
      String gluExecutionId, Committer committer, String commitMessage) {
    String group = "changelog";
    String subject = "#prodchange #glu";
    String body = String.format("Deploying %s (r %s) to %s. " +
    		"%s/%s?showErrorsOnly=false " +
    		"By @%s " +
    		" message: %s", modules, version, tags, gluExecutionBaseUrl, gluExecutionId, committer,
    		commitMessage);
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(group + "+yourcompanyxxx.com@yammer.com");
    msg.setFrom(fromEmailAddress);
    msg.setSubject(subject);
    msg.setText(body);
    return msg;
  }

  public String getGluExecutionBaseUrl() {
    return gluExecutionBaseUrl;
  }

  public String getFromEmailAddress() {
    return fromEmailAddress;
  }
}
