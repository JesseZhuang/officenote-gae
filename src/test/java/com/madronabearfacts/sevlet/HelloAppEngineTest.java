package com.madronabearfacts.sevlet;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.madronabearfacts.servlet.HelloAppEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HelloAppEngine}.
 */
@RunWith(JUnit4.class)
public class HelloAppEngineTest {
  private static final String FAKE_URL = "fake.fk/hello";
  // Set up a helper so that the ApiProxy returns a valid environment for local testing.
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private HelloAppEngine servletUnderTest;
  private TestUtils testUtils;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    //  Set up some fake HTTP requests
    when(mockRequest.getRequestURI()).thenReturn(FAKE_URL);
    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new HelloAppEngine();
    testUtils = new TestUtils();

    testUtils.setUp();
  }

  @After public void tearDown() {
    testUtils.cleanUp();
    helper.tearDown();
  }

  @Test
  public void doGetWritesResponse() throws Exception {

    servletUnderTest.doGet(mockRequest, mockResponse);

    String response = responseWriter.toString();
    // We expect our hello world response.
    assertThat(response)
            .named("HelloAppEngine response")
            .contains("Hello App Engine - Standard ");

    assertThat(response)
            .contains("school year start date : Mon Jan 01 00:00:00 UTC 2018");
  }

  @Test
  public void helloInfoTest() {
    String result = HelloAppEngine.getInfo();
    assertThat(result)
            .named("HelloInfo.getInfo")
            .containsMatch("^Version:\\s+.+OS:\\s+.+User:\\s");
  }
}
