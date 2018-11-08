package com.madronabearfacts.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Updates existing blurbs (increment curWeek field by 1) and archive expired blurbs to a different Kind in
 * google cloud datastore.
 */
@WebServlet(name = "UpdateBlurbServlet", value = "/admin/update")
public class UpdateBlurbServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(UpdateBlurbServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletHelper.writeSchoolYearDates();
        response.setContentType("text/plain");
        Map<String, Integer> report = ServletHelper.updateArchiveDeleteBlurbs();
        response.getWriter().println("Updated " + report.get(ServletHelper.TOTAL_UPDATED_COUNT) + " blurbs.\n"
                + "Archived " + report.get(ServletHelper.TO_BE_ARCHIVED_COUNT) + " blurbs.");
    }
}
