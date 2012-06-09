<% out.print("<!DOCTYPE html>"); %>
<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->

<!-- $Rev: 597221 $ $Date: 2007-11-21 22:51:05 +0100 (Wed, 21 Nov 2007) $ -->

<%@ page import="
javax.servlet.http.HttpServletRequest,
javax.servlet.jsp.JspWriter,
java.io.File
"%>
<%@ page import="org.apache.tomee.common.TomcatVersion"%>
<html>

<head>
    <meta charset="utf-8">
    <title>TomEE</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="lib/bootstrap/2.0.4/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }
        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
    <link href="lib/bootstrap/2.0.4/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>

<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a class="brand" href="http://openejb.apache.org">TomEE</a>
            <div class="nav-collapse">
                <ul class="nav">
                    <li class="active"><a href="index.jsp">Index</a></li>
                    <li><a href="viewjndi.jsp">JNDI</a></li>
                    <li><a href="viewejb.jsp">EJB</a></li>
                    <li><a href="viewclass.jsp">Class</a></li>
                    <li><a href="invokeobj.jsp">Invoke</a></li>
                </ul>

            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <h2>Testing openejb.home validity</h2>
            <table class='table table-striped table-bordered table-condensed'><tbody>
<%
    try{
        synchronized (this) {
            main(request, session, out);
        }
    } catch (Exception e){
        out.println("FAIL");
        //throw e;
        return;
    }
%>
            </tbody></table>
            <a class='btn' href='testint.jsp'>Continue tests</a>
        </div>
    </div>
    <hr>

    <footer>
        <p>Copyright &copy; 2012  The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and the Apache feather logo are trademarks of The Apache Software Foundation.</p>
    </footer>
</div>


<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="lib/jquery/jquery-1.7.2.min.js"></script>
<script src="lib/bootstrap/2.0.4/js/bootstrap.js"></script>

</body>
</html>

<%!
    static String invLock = "lock";
    static int invCount;

    HttpSession session;
    HttpServletRequest request;
    JspWriter out;

    String OK = "<td><span style='color: green'><b>OK</b></span></td>";
    String FAIL = "<td><span style='color: red'><b>FAIL</b></span></td>";

    /**
     * The main method of this JSP
     */
    public void main(HttpServletRequest request, HttpSession session, JspWriter out) throws Exception {
        this.request = request;
        this.session = session;
        this.out = out;

        try {
            // The openejb.home must be set
            out.print("<tr><td>openejb.home is set</td> ");
            String homePath = System.getProperty("openejb.home");
            if (homePath == null) handleError(NO_HOME, INSTRUCTIONS);
            out.print(OK);
            out.print("</tr>");

            // The openejb.home must exist
            out.print("<tr><td>openejb.home exists</td> ");
            File openejbHome = new File(homePath);
            if (!openejbHome.exists()) handleError(BAD_HOME + homePath, NOT_THERE, INSTRUCTIONS);
            out.print(OK);
            out.print("</tr>");

            // The openejb.home must be a directory
            out.print("<tr><td>openejb.home is a directory</td> ");
            if (!openejbHome.isDirectory()) handleError(BAD_HOME + homePath, NOT_DIRECTORY, INSTRUCTIONS);
            out.print(OK);
            out.print("</tr>");

            // The openejb.home must contain a 'lib' directory
            out.print("<tr><td>has lib directory</td> ");

            File openejbHomeLib;
            if (TomcatVersion.v6.isTheVersion() || TomcatVersion.v7.isTheVersion()) {
                openejbHomeLib = new File(openejbHome, "lib");
            } else {
                File common = new File(openejbHome, "common");
                openejbHomeLib = new File(common, "lib");
            }
            if (!openejbHomeLib.exists()) handleError(BAD_HOME + homePath, NO_LIB, INSTRUCTIONS);
            out.print(OK);
            out.print("</tr>");

        } catch (Exception e) {
            out.print(FAIL);
            out.print("<p>" + e.getMessage() + "</p>");
        }
    }

    String NO_HOME = "The openejb.home is not set.";
    String BAD_HOME = "Invalid openejb.home: ";
    String NOT_THERE = "The path specified does not exist.";
    String NOT_DIRECTORY = "The path specified is not a directory.";
    String NO_LIB = "The path specified is not correct, it does not contain a 'lib' directory.";
    String INSTRUCTIONS = "Please edit the web.xml of the openejb_loader webapp and set the openejb.home init-param to the full path where OpenEJB is installed.";

    private void handleError(String m1, String m2, String m3) throws Exception {
        String msg = "<br><b>Please Fix:</b><br><br>";
        msg += m1 + "<br><br>";
        msg += m2 + "<br><br>";
        msg += m3 + "<br>";
        throw new Exception(msg);
    }

    private void handleError(String m1, String m2) throws Exception {
        String msg = "<br><b>Please Fix:</b><br><br>";
        msg += m1 + "<br><br>";
        msg += m2 + "<br>";
        throw new Exception(msg);
    }
%>

