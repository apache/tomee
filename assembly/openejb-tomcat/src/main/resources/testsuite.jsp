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
java.io.ByteArrayOutputStream,
java.io.PrintStream
"%>
<%@ page import="org.apache.openejb.test.TestRunner" %>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Tomcat Integration/1.0</title>
    <link href="default.css" rel="stylesheet">
    <!-- $Id: testsuite.jsp,v 1.1.2.1 2003/05/05 02:42:52 dblevins Exp $ -->
    <!-- Author: David Blevins (david.blevins@visi.com) -->
</head>
<body marginwidth="0" marginheight="0" leftmargin="0" bottommargin="0" topmargin="0" vlink="#6763a9" link="#6763a9" bgcolor="#ffffff">
<a name="top"></a>
<table width="712" cellspacing="0" cellpadding="0" border="0">
    <tr>
        <td bgcolor="#5A5CB8" align="left" valign="top" width="7"><img height="9" width="1" border="0" src="images/dotTrans.gif"></td>
        <td bgcolor="#5A5CB8" align="left" valign="top" width="40"><img border="0" height="6" width="40" src="images/dotTrans.gif"></td>
        <td bgcolor="#5A5CB8" align="left" valign="top" height="2" width="530"><img border="0" height="6" width="530" src="images/top_2.gif"></td>
        <td bgcolor="#E24717" align="left" valign="top" height="2" width="120"><img src="images/top_3.gif" width="120" height="6" border="0"></td>
    </tr>
    <tr>
        <td bgcolor="#5A5CB8" align="left" valign="top" bgcolor="#ffffff" width="13"><img border="0" height="15" width="13" src="images/dotTrans.gif"></td>
        <td align="left" valign="top" width="40"><img border="0" height="1" width="1" src="images/dotTrans.gif"></td>
        <td align="left" valign="middle" width="530"><a href="http://openejb.apache.org"><span class="menuTopOff">OpenEJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="index.jsp"><span class="menuTopOff">Index</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewjndi.jsp"><span class="menuTopOff">JNDI</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewejb.jsp"><span class="menuTopOff">EJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewclass.jsp"><span class="menuTopOff">Class</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="invokeobj.jsp"><span class="menuTopOff">Invoke</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"></td>
        <td align="left" valign="top" height="20" width="120"><img border="0" height="2" width="10" src="images/dotTrans.gif"></td>
    </tr>
    <tr>
        <td align="left" valign="top" bgcolor="#a9a5de" width="7"><img border="0" height="3" width="7" src="images/line_sm.gif"></td>
        <td align="left" valign="top" height="3" width="40"><img border="0" height="3" width="40" src="images/line_light.gif"></td>
        <td align="left" valign="top" height="3" width="530"><img border="0" height="3" width="530" src="images/line_light.gif"></td>
        <td align="left" valign="top" height="3" width="120"><img height="1" width="1" border="0" src="images/dotTrans.gif"></td>
    </tr>
    <tr>
        <td align="left" valign="top" bgcolor="#a9a5de" width="7">&nbsp;</td>
        <td align="left" valign="top" width="40">&nbsp;</td>
        <td valign="top" width="530" rowspan="4">
            <table width="530" cellspacing="0" cellpadding="0" border="0" rows="2" cols="1">
                <tr>
                    <td align="left" valign="top"><br>
                        <img width="200" vspace="0" src="./images/logo_ejb2.gif" hspace="0" height="55" border="0">
                        <br>
                        <img src="images/dotTrans.gif" hspace="0" height="7" border="0"><br>
                            <span class="pageTitle">
                            OpenEJB Test Suite
                            </span>
                        <br>
                        <img src="images/dotTrans.gif" hspace="0" height="1" border="0"></td>
                </tr>
            </table>
            <p>
            </p>
            <FONT SIZE="2">
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
<BR><BR>
<BR>
</FONT>

            </td>
            <td align="left" valign="top" height="5" width="120">


                &nbsp;</td>
        </tr>
    </table>
    </body>
</html>

<%!
    String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";

    static String invLock = "lock";
    static int invCount;

    HttpSession session;
    HttpServletRequest request;
    JspWriter out;

    /**
     * The main method of this JSP
     */
    public void main(HttpServletRequest request, HttpSession session, JspWriter out) throws Exception {
        this.request = request;
        this.session = session;
        this.out = out;

        String doInvoke = request.getParameter("invoke");
        if (doInvoke != null) {
            invoke();
        } else {
            out.print("<FORM NAME='invoke' METHOD='GET' ACTION='testsuite.jsp'>");
            out.print("<INPUT type='SUBMIT' NAME='invoke' value='Invoke'>");
            out.print("</FORM>");
        }
    }

    String pepperImg = "<img src='images/pepper.gif' border='0'>";

    public void invoke() throws Exception {

        try {
            System.setProperty("openejb.test.server", "org.apache.openejb.test.IvmTestServer");
            System.setProperty("openejb.test.database", "org.apache.openejb.test.InstantDbTestDatabase");
            //test.server.class=org.apache.openejb.test.IvmTestServer
            //out.print("B");
            //Object runner = this.getClass().forName("org.apache.openejb.test.TestRunner").newInstance();
            //out.print("C");

            //Method main = runner.getClass().getMethod("main",new Class[]{new String[]{}.getClass()});

            String[] args = new String[]{"org.apache.openejb.test.ClientTestSuite"};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.openejb.test.ResultPrinter printer = new org.apache.openejb.test.ResultPrinter(new PrintStream(baos));
            TestRunner runner = new TestRunner(printer);

            runner.start(args);
            byte[] bytes = baos.toByteArray();
            StringBuffer sb = new StringBuffer(bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                char c = (char) bytes[i];
                switch (c) {
                    case' ':
                        sb.append("&nbsp;");
                        break;
                    case'\n':
                        sb.append("<br>");
                        break;
                    case'\r':
                        break;
                    default:
                        sb.append(c);
                }
            }
            out.print(sb.toString());

            //main.invoke(runner, args);


        } catch (Throwable e) {
            out.print("<br><b>Bad Exception:</b><br><br>");
            out.print(formatThrowable(e));
        }
    }

    public String formatThrowable(Throwable err) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        err.printStackTrace(new PrintStream(baos));
        byte[] bytes = baos.toByteArray();
        StringBuffer sb = new StringBuffer(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            char c = (char) bytes[i];
            switch (c) {
                case' ':
                    sb.append("&nbsp;");
                    break;
                case'\n':
                    sb.append("<br>");
                    break;
                case'\r':
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
%>

