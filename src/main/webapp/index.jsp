<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.madronabearfacts.servlet.HelloAppEngine" %>
<%@ page import="com.madronabearfacts.servlet.FetchBlurbServlet" %>
<%@ page import="com.madronabearfacts.servlet.UpdateBlurbServlet" %>
<%@ page import="com.madronabearfacts.servlet.MailchimpOfficeNote" %>
<%@ page import="com.madronabearfacts.servlet.MailchimpSingleBlast" %>
<%@ page import="com.madronabearfacts.servlet.WeeklyCronServlet" %>
<html>
<head>
  <link href='//fonts.googleapis.com/css?family=Marmelad' rel='stylesheet' type='text/css'>
  <title>Hello App Engine Standard Java 8</title>
</head>
<body>
    <h1>Hello App Engine -- Java 8!</h1>

  <p>This is <%= HelloAppEngine.getInfo() %>.</p>
  <table>
    <tr>
      <td colspan="2" style="font-weight:bold;">Available Servlets:</td>
    </tr>
    <tr>
      <td><a href='/hello'>Hello App Engine</a></td>
    </tr>
    <tr>
      <td>The ones below are limited to admin only:</td>
    </tr>
    <tr>
      <td><a href='/admin/fetch'>Fetch Blurbs</a></td>
    </tr>
    <tr>
      <td><a href='/admin/update'>Update and Archive Blurbs</a></td>
    </tr>
    <tr>
      <td><a href='/admin/MailchimpOfficeNote'>Put together mailchimp campaign</a></td>
    </tr>
    <tr>
      <td><a href='/admin/MailchimpSingleBlast'>Put together mailchimp campaign for single blast</a></td>
    </tr>
    <tr>
      <td><a href='/admin/cron/weekly'>Put together the mailchimp campaign, update blurbs, email confirmation</a></td>
    </tr>
  </table>

</body>
</html>
