package com.outbrain.glu.teamc;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Ran Tavory
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TeamCityApiTest {

  private TeamCityApi api;

  private final String host = "host";

  private final int port = 8081;

  private final String usernamePassword = "user:pass";

  private final String buildTriggerId = "bt3";

  @Mock
  private HttpClient client;

  @Mock
  private GetMethod method;

  @Before
  public void before() {
    HttpState httpState = Mockito.mock(HttpState.class);
    Mockito.when(client.getState()).thenReturn(httpState);
    HttpClientParams httpClientParams = Mockito.mock(HttpClientParams.class);
    Mockito.when(client.getParams()).thenReturn(httpClientParams);
    api = new TeamCityApi(host, port, usernamePassword, buildTriggerId, client, method);
  }

  @After
  public void after() {
    api = null;
  }

  @Test
  public void testBuild_ok() throws NullPointerException, HttpException, IOException {
    Mockito.when(client.executeMethod(method)).thenReturn(200);
    api.build("modules", "tags", "committer", "message", false);
    Mockito.verify(client).executeMethod(method);
  }

  @Test(expected = HttpException.class)
  public void testBuild_error() throws NullPointerException, HttpException, IOException {
    Mockito.when(client.executeMethod(method)).thenReturn(401);
    api.build("modules", "tags", "committer", "message", false);
  }
}
