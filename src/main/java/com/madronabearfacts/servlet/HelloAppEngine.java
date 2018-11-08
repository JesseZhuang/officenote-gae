package com.madronabearfacts.servlet;

import com.google.appengine.api.utils.SystemProperty;
import com.madronabearfacts.dao.SchoolYearDatesDAO;
import com.madronabearfacts.helper.GmailHelper;
import com.madronabearfacts.helper.GoogleAuthHelper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "HelloAppEngine", value = "/hello")
public class HelloAppEngine extends HttpServlet {

    private final Logger logger = Logger.getLogger(HelloAppEngine.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletHelper.writeSchoolYearDates();
        Properties properties = System.getProperties();

        logger.log(Level.INFO, String.format("remote address: %s, remote host: %s",
                request.getRemoteAddr(), request.getRemoteHost()));

        SchoolYearDatesDAO dao = new SchoolYearDatesDAO();
        logger.log(Level.INFO, "school year start date : " + dao.getStartDate().toString());

        response.setContentType("text/plain");
        response.getWriter().println("Hello App Engine - Standard using "
                + SystemProperty.version.get() + " Java " + properties.get("java.specification.version")
                + "\n" + "school year start date : " + dao.getStartDate().toString());
    }

    public static String getInfo() {
        return "Version: " + System.getProperty("java.version")
                + " OS: " + System.getProperty("os.name")
                + " User: " + System.getProperty("user.name");
    }
}
