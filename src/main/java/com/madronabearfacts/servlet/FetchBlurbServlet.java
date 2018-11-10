package com.madronabearfacts.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Fetch blurbs from office notes gmail email messages.
 */
@WebServlet(name = "FetchBlurbServlet", value = "/admin/fetch")
public class FetchBlurbServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletHelper.prepLocalDatastore();
        response.setContentType("text/plain");
        ServletHelper.fetchBlurbs();
        response.getWriter().println(String.format("Fetched blurbs."));
    }
}
