<%@ page import="org.apache.openejb.tomcat.installer.Installer" %>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Integration/1.0</title>
    <link href="default.css" rel="stylesheet">
    <!-- $Id: index.html,v 1.1.2.2 2003/05/05 02:27:04 dblevins Exp $ -->
    <!-- Author: David Blevins (david.blevins@visi.com) -->
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
                            OpenEJB and Tomcat Integration Page
                            </span>
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="1" border="0"></td>
                    </tr>
                </table>
                <p>
                <FONT SIZE='2'>
                <B>Welcome to the OpenEJB/Tomcat integration!</B><br><BR>
                Now that OpenEJB has been installed, click on
                the "Testing your setup" link below to verify it.  When everything
                is setup well, feel free to play around with the tools provided below!
                <BR><BR>
                <B>Setup</B><BR>
                <A HREF="testhome.jsp">Testing your setup</A><BR>
                <BR>
<% if (!Installer.isListenerInstalled() && !Installer.isAgentInstalled()) { %>
                <B>Install</B><BR>
                <A HREF="installer">[Optional] Install Listener and JavaAgent</A><BR>
                <BR>
<% } else if (!Installer.isListenerInstalled()) { %>
                <B>Install</B><BR>
                <A HREF="installer">[Optional] Install Listener</A><BR>
                <BR>
<% } else if (!Installer.isAgentInstalled()) { %>
                <B>Install</B><BR>
                <A HREF="installer">[Optional] JavaAgent</A><BR>
                <BR>
<% } %>
                <B>Tools</B><BR>
                <A HREF="viewjndi.jsp">OpenEJB JNDI Browser</A><BR>
                <A HREF="viewclass.jsp">OpenEJB Class Viewer</A><BR>
                <A HREF="viewejb.jsp">OpenEJB EJB Viewer</A><BR>
                <A HREF="invokeobj.jsp">OpenEJB Object Invoker</A><BR>
                <BR>
<%--
                <B>FAQs</B><BR>
                <A HREF="howitworks.html">How does the integration work</A><BR>
                <A HREF="ejbclasses.html">Where to put your bean classes</A><BR>
                <A HREF="ejbref.html">How to configure java:comp/env lookups</A><BR>
                <BR>
--%>                
                </FONT>
                </p>
                <p>
                </p>
                <br>
                <br>
            </td>
            <td align="left" valign="top" height="5" width="120">


                &nbsp;</td>
        </tr>
    </table>
    </body>
</html>