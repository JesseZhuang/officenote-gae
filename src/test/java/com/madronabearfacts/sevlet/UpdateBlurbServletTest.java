package com.madronabearfacts.sevlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.helper.Constants;
import com.madronabearfacts.servlet.UpdateBlurbServlet;
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
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UpdateBlurbServletTest {
    // Set up a helper so that the ApiProxy returns a valid environment for local testing.
    private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

    @Mock private HttpServletRequest mockRequest;
    @Mock private HttpServletResponse mockResponse;
    private StringWriter responseWriter;
    private UpdateBlurbServlet servletUnderTest;
    private TestUtils testUtils;
    private List<Key> blurbKeys;

    private static final int BLURBS_COUNT = 5;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        helper.setUp();
        // Set up a fake HTTP response.
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
        testUtils = new TestUtils();
        servletUnderTest = new UpdateBlurbServlet();

        blurbKeys = testUtils.setUpEntities(createTestEntities());
    }

    @After
    public void tearDown() {
        testUtils.cleanUpKeys(blurbKeys);
        helper.tearDown();
    }

    private List<Entity> createTestEntities() {
        List<Entity> entities = new ArrayList<>();
        for(int i = 1; i <= BLURBS_COUNT; i++) {
            Entity e = new Entity(Constants.BLURB_ENTITY_KIND, i);
            e.setProperty(Blurb.TITLE, Constants.BLURB_ENTITY_KIND + i);
            e.setProperty(Blurb.CONTENT, new Text("content"));
            e.setProperty(Blurb.CUR_WEEK, (long) i);
            e.setProperty(Blurb.NUM_WEEKS, (long) BLURBS_COUNT);
            e.setProperty(Blurb.FETCH_DATE, TestUtils.START_DATE);
            e.setProperty(Blurb.START_DATE, TestUtils.START_DATE);
            entities.add(e);
        }
        return entities;
    }

    @Test
    public void testDoGet() throws Exception {
        servletUnderTest.doGet(mockRequest, mockResponse);

        assertThat(responseWriter.toString())
                .named("UpdateServlet response")
                .contains("Updated " + BLURBS_COUNT + " blurbs.\n" +
                        "Archived " + 1 + " blurbs.");
    }
}
