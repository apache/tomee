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

<!-- $Rev$ $Date$ -->

<%@ page import="org.apache.openejb.tomcat.installer.Installer" %>
<%@ page import="java.io.File" %>
<%@ page import="org.apache.openejb.tomcat.installer.Paths" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
    Installer installer = (Installer) request.getAttribute("installer");
    Paths paths = (Paths) request.getAttribute("paths");
%>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Integration/1.0</title>
    <link href="default.css" rel="stylesheet">
</head>
<body marginwidth="0" marginheight="0" leftmargin="0" bottommargin="0" topmargin="0" vlink="#6763a9" link="#6763a9" bgcolor="#ffffff">
    <a name="top"></a>
    <table width="712" cellspacing="0" cellpadding="0" border="0">
        <tr>
            <td bgcolor="#5A5CB8" align="left" valign="top" width="7"><img height="9" width="1" border="0" src="images/dotTrans.gif"></td>
            <td bgcolor="#5A5CB8" align="left" valign="top" width="40"><img border="0" height="6" width="40" src="images/dotTrans.gif"></td>
            <td bgcolor="#5A5CB8" align="left" valign="top" height="2" width="430"><img border="0" height="6" width="430" src="images/top_2.gif"></td>
            <td bgcolor="#E24717" align="left" valign="top" height="2" width="120"><img src="images/top_3.gif" width="120" height="6" border="0"></td>
        </tr>
        <tr>
            <td bgcolor="#5A5CB8" align="left" valign="top" bgcolor="#ffffff" width="13"><img border="0" height="15" width="13" src="images/dotTrans.gif"></td>
            <td align="left" valign="top" width="40"><img border="0" height="1" width="1" src="images/dotTrans.gif"></td>
            <td align="left" valign="middle" width="430">
                <a href="http://openejb.apache.org"><span class="menuTopOff">OpenEJB</span></a>
                <img border="0" height="2" width="20" src="images/dotTrans.gif">
                <a href="index.jsp"><span class="menuTopOff">Index</span></a>
                <img border="0" height="2" width="20" src="images/dotTrans.gif">
                <a href="viewjndi.jsp"><span class="menuTopOff">JNDI</span></a>
                <img border="0" height="2" width="20" src="images/dotTrans.gif">
                <a href="viewejb.jsp"><span class="menuTopOff">EJB</span></a>
                <img border="0" height="2" width="20" src="images/dotTrans.gif">
                <a href="viewclass.jsp"><span class="menuTopOff">Class</span></a>
                <img border="0" height="2" width="20" src="images/dotTrans.gif">
                <a href="invokeobj.jsp"><span class="menuTopOff">Invoke</span></a>
                <img border="0" height="2" width="20" src="images/dotTrans.gif">
            </td>
            <td align="left" valign="top" height="20" width="120"><img border="0" height="2" width="10" src="images/dotTrans.gif"></td>
        </tr>
        <tr>
            <td align="left" valign="top" bgcolor="#a9a5de" width="7"><img border="0" height="3" width="7" src="images/line_sm.gif"></td>
            <td align="left" valign="top" height="3" width="40"><img border="0" height="3" width="40" src="images/line_light.gif"></td>
            <td align="left" valign="top" height="3" width="430"><img border="0" height="3" width="430" src="images/line_light.gif"></td>
            <td align="left" valign="top" height="3" width="120"><img height="1" width="1" border="0" src="images/dotTrans.gif"></td>
        </tr>
        <tr>
            <td align="left" valign="top" bgcolor="#a9a5de" width="7">&nbsp;</td>
            <td align="left" valign="top" width="40">&nbsp;</td>
            <td valign="top" width="430" rowspan="4">
                <table width="430" cellspacing="0" cellpadding="0" border="0" rows="2" cols="1">
                    <tr>
                        <td align="left" valign="top"><br>
                            <img width="200" vspace="0" src="./images/logo_ejb2.gif" hspace="0" height="55" border="0">
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="7" border="0"><br>
                            <span class="pageTitle">
                            OpenEJB Installer
                            </span>
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="1" border="0"></td>
                    </tr>
                </table>
                <p>
<%
    if (installer != null) {
        if (installer.getStatus() == Installer.Status.REBOOT_REQUIRED) {
%>

            <FONT SIZE='2'>
                <img border='0' height='3' width='360' src='images/line_light.gif'><br>
                <table width='360' cellspacing='4' cellpadding='4' border='0'>
<%
        for (String info : installer.getInfos()) {
%>
                    <tr>
                        <td><font size='2'><%= info %></font></td>
                        <td><font size='2' color='green'><b>DONE</b></font></td>
                    </tr>
<%
        }
%>
                </table>
                <img border='0' height='3' width='360' src='images/line_light.gif'>
                <br><br>

            The installer has completed successfully. <br> 
            Please, <b>restart Tomcat</b> and reload this page to verify installation.
<%
        } else {
            List<String> errors = new ArrayList<String>(paths.getErrors());
            errors.addAll(installer.getErrors());
            if (!errors.isEmpty()) {
%>          Installation Failed<br><br>
            <FONT SIZE='2'>
                <img border='0' height='3' width='360' src='images/line_light.gif'><br>
                <table width='300' cellspacing='4' cellpadding='4' border='0'>
<%
                for (String error : errors) {
%>
                    <tr>
                        <td><font size='2'><%= error %></font></td>
                        <!--<td><font size='2' color='green'><b>DONE</b></font></td>-->
                    </tr>
<%
                }
%>
                </table>
                <img border='0' height='3' width='360' src='images/line_light.gif'>
                <br><br>
<%
            } else {
%>
            <FONT SIZE='2'>
                <img border='0' height='3' width='360' src='images/line_light.gif'><br>
                <table width='300' cellspacing='4' cellpadding='4' border='0'>
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
                </table>
                <img border='0' height='3' width='360' src='images/line_light.gif'>
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
                <form action='/openejb/installer' method='post'>
                Catalina Home:
                <input type='text' size='100' name='catalinaHome' value='<%= safeGetAbsolutePath(paths.getCatalinaHomeDir()) %>'>
                <br>
                Catalina Base:
                <input type='text' size='100' name='catalinaBase' value='<%= safeGetAbsolutePath(paths.getCatalinaBaseDir()) %>'>
                <br>
                Catalina server.xml:
                <input type='text' size='100' name='serverXml' value='<%= safeGetAbsolutePath(paths.getServerXmlFile()) %>'>
                <br><br>
                <input type='submit' name='action' value='Install'>
                </form>
<%
            } else {
%>
                Installation Successful!
<%
            }
        }
%>
                </FONT>

                </p>
                <p>
                </p>
                <br>
                <br>
<% } %>
            </td>
            <td align="left" valign="top" height="5" width="120">


                &nbsp;</td>
        </tr>
    </table>
    </body>
</html>
<%!
    private String safeGetAbsolutePath(File file) {
        if (file == null) return "";
        return file.getAbsolutePath();
    }
%>