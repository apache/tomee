<!DOCTYPE html>
<!--

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

-->

<!-- $Rev: 597221 $ $Date: 2007-11-21 22:51:05 +0100 (Wed, 21 Nov 2007) $ -->

<%@ page import="org.apache.tomee.installer.Installer" %>
<%@ page import="java.io.File" %>
<%@ page import="org.apache.tomee.installer.Paths" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
    Installer installer = (Installer) request.getAttribute("installer");
    Paths paths = (Paths) request.getAttribute("paths");
%>
<html>

<head>
    <meta charset="utf-8">
    <title>TomEE</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="lib/bootstrap/2.1.1/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }

        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
    <link href="lib/bootstrap/2.1.1/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>

<body>

<div class="navbar navbar-inverse navbar-fixed-top">
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

            </div>
            <!--/.nav-collapse -->
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">

            <%
                if (installer != null) {
                    if (installer.getStatus() == Installer.Status.REBOOT_REQUIRED) {
            %>

            <table class='table table-striped table-bordered table-condensed'>
                <tbody>
                <%
                    for (String info : installer.getAlerts().getInfos()) {
                %>
                <tr>
                    <td><%= info %>
                    </td>
                    <td><span style="color:green;"><b>DONE</b></span></td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
            <br><br>

            The installer has completed successfully. <br>
            Please, <b>restart Tomcat</b> and reload this page to verify installation.
            <%
            } else {
                List<String> errors = new ArrayList<String>(paths.getErrors());
                errors.addAll(installer.getAlerts().getErrors());
                if (!errors.isEmpty()) {
            %> Installation Failed<br><br>
            <table>
                <%
                    for (String error : errors) {
                %>
                <tr>
                    <td><%= error %>
                    </td>
                    <td><span style="color:green;"><b>DONE</b></span></td>
                </tr>
                <%
                    }
                %>
            </table>
            <br><br>
            <%
            } else {
            %>
            <table class='table table-striped table-bordered table-condensed'>
                <tbody>
                <tr>
                    <td><font size='2'>Tomcat Listener Installed</font></td>
                    <% if (installer.isListenerInstalled()) { %>
                    <td><font size='2' color='green'><b>YES</b></font></td>
                    <% } else { %>
                    <td><font size='2' color='red'><b>NO</b></font></td>
                    <% } %>
                </tr>
                <tr>
                    <td><font size='2'>JavaAgent Installed</font></td>
                    <% if (installer.isAgentInstalled()) { %>
                    <td><font size='2' color='green'><b>YES</b></font></td>
                    <% } else { %>
                    <td><font size='2' color='red'><b>NO</b></font></td>
                    <% } %>
                </tr>
                </tbody>
            </table>
            <br><br>
            <%
                }
            %>
            <%
                if (installer.getStatus() == Installer.Status.NONE) {
                    if (errors.isEmpty()) {
            %>
            Please verify the file paths are correct and click the install button.
            <%
            } else {
            %>
            Try again?
            <%
                }
            %>

            <br><br>

            <form action='/tomee/installer' method='post'>
                <table>
                    <tbody>
                    <tr>
                        <td>Catalina Home:</td>
                        <td><input type='text' size='200' style="width:300px" name='catalinaHome'
                                   value='<%= safeGetAbsolutePath(paths.getCatalinaHomeDir()) %>'></td>
                    </tr>
                    <tr>
                        <td>Catalina Base:</td>
                        <td><input type='text' size='200' style="width:300px" name='catalinaBase'
                                   value='<%= safeGetAbsolutePath(paths.getCatalinaBaseDir()) %>'></td>
                    </tr>
                    <tr>
                        <td>Catalina server.xml:</td>
                        <td><input type='text' size='200' style="width:300px" name='serverXml'
                                   value='<%= safeGetAbsolutePath(paths.getServerXmlFile()) %>'></td>
                    </tr>
                    <tr>
                        <td><input type='submit' name='action' value='Install'></td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <%
            } else {
            %>
            Installation Successful!
            <%
                        }

                    }
                } %>
        </div>
    </div>
</div>
<hr>

<footer>
    <p>Copyright &copy; 2012 The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and
        the Apache feather logo are trademarks of The Apache Software Foundation.</p>
</footer>


<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="lib/jquery/jquery-1.7.2.min.js"></script>
<script src="lib/bootstrap/2.1.1/js/bootstrap.js"></script>

</body>
</html>

<%!
    private String safeGetAbsolutePath(File file) {
        if (file == null) return "";
        return file.getAbsolutePath();
    }
%>
