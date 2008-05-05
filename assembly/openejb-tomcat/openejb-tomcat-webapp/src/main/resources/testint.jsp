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
javax.naming.Context,
javax.naming.InitialContext,
javax.servlet.http.HttpServletRequest,
javax.servlet.jsp.JspWriter,
java.io.ByteArrayOutputStream,
java.io.IOException,
java.io.PrintStream,
java.lang.reflect.Method,
java.util.Properties
"%>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB/Tomcat</title>
    <link href="default.css" rel="stylesheet">
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
                            Testing integration
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

    String OK = "<td><font size='2' color='green'><b>OK</b></font></td></tr>";
    String FAIL = "<td><font size='2' color='red'><b>FAIL</b></font></td></tr>";
    String HR = "<img border='0' height='3' width='340' src='images/line_light.gif'><br>";
    String pepperImg = "<img src='images/pepper.gif' border='0'>";

    /**
     * The main method of this JSP
     */
    public void main(HttpServletRequest request, HttpSession session, JspWriter out) throws Exception {
        this.request = request;
        this.session = session;
        this.out = out;

        InitialContext ctx = null;
        try {
            Properties p = new Properties();

            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put("openejb.loader", "embed");

            ctx = new InitialContext(p);

        } catch (Exception e) {
            formatThrowable(e);
        }

        try {
            out.print(HR);
            out.print("<table width='300' cellspacing='4' cellpadding='4' border='0'>");

            // ---------------------------------------------------
            //  Were the OpenEJB classes installed?
            // ---------------------------------------------------

            printTest("Were the OpenEJB classes installed");
            ClassLoader myLoader = null;
            Class openejb = null;
            try {
                myLoader = this.getClass().getClassLoader();
                openejb = Class.forName("org.apache.openejb.OpenEJB", true, myLoader);
                out.print(OK);
            } catch (Exception e) {
                out.print(FAIL);
            }

            // ---------------------------------------------------
            //  Are the EJB libraries visible?
            // ---------------------------------------------------

            printTest("Were the EJB classes installed");
            try {
                Class.forName("javax.ejb.EJBHome", true, myLoader);
                out.print(OK);
            } catch (Exception e) {
                out.print(FAIL);
            }

            // ---------------------------------------------------
            //  Was OpenEJB initialized (aka started)?
            // ---------------------------------------------------

            printTest("Was OpenEJB initialized (aka started)");

            try {
                Method isInitialized = openejb.getDeclaredMethod("isInitialized");
                Boolean running = (Boolean) isInitialized.invoke(openejb);

                if (running) {
                    out.print(OK);
                } else {
                    out.print(FAIL);
                }
            } catch (Exception e) {
                out.print(FAIL);
            }

            // ---------------------------------------------------
            //  Can I lookup anything?
            // ---------------------------------------------------

            printTest("Performing a test lookup");

            try {
                Object obj = ctx.lookup("");

                if (obj.getClass().getName().equals("org.apache.openejb.core.ivm.naming.IvmContext")) {
                    out.print(OK);
                } else {
                    out.print(FAIL);
                }

            } catch (Exception e) {
                out.print(FAIL);
            }

            out.print("</table>");
            out.print(HR);
            try {
                Object obj = ctx.lookup("client");
                if (obj instanceof Context) {
                    out.print("<br><table><tr><td>" + pepperImg + "</td><td><font size='2'>");
                    out.print("<a href='testejb.jsp'>Continue tests</a>");
                    out.print("</font></td></tr></table>");
                }

            } catch (Exception e) {
            }

        } catch (Exception e) {
            out.print(FAIL);
            out.print("</table>");
            out.print(HR);

            out.print(e.getMessage());
        }
    }

    protected void printTest(String test) throws IOException {
        out.print("<tr><td><font size='2'>");
        out.print(test);
        out.print("</font></td>");
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

