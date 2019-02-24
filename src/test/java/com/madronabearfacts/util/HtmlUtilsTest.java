package com.madronabearfacts.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HtmlUtilsTest {
    @Test
    public void testUrlWithParenthesis() {
        String url = "https://www.edmonds.wednet.edu/UserFiles/Servers/Server_306670/File/Community/Community%20eFliers/Community%20Activites/Sno-IsleLibrary(Edmonds)Naturescapes02212019.pdf";
        assertTrue(url.matches(HtmlUtils.URL_PATTERN));
    }
}
