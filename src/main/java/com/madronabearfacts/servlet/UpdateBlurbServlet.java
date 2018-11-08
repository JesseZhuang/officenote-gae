package com.madronabearfacts.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Updates existing blurbs (increment curWeek field by 1) and archive expired blurbs to a different Kind in
 * google cloud datastore.
 */
@WebServlet(name = "UpdateBlurbServlet", value = "/admin/update")
public class UpdateBlurbServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletHelper.prepLocalDatastore();
        response.setContentType("text/plain");
        ServletHelper.updateArchiveDeleteBlurbs();
        response.getWriter().println("Updated, archived, and deleted blurbs.");
    }
}
