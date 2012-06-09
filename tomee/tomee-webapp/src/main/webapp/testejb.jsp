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
<%@ page import="java.io.PrintWriter" %>
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
            <h2>Testing an Enterprise JavaBean</h2>
            <table class='table table-striped table-bordered table-condensed'><tbody>
<%
    try{
        synchronized (this) {
            main(request, session, out);
        }
    } catch (Exception e){
        out.println("<p>FAIL</p>");
        return;
    }
%>
            </tbody></table>
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

    String OK = "<span style='color: green'><b>OK</b></span>";
    String FAIL = "<span style='color: red'><b>FAIL</b></span>";

    private Object getEjbObj(InitialContext ctx, ClassLoader myLoader) {
        try {
            Class[] params = new Class[0];
            Class homeInterface = Class.forName("javax.management.j2ee.ManagementHome", true, myLoader);
            Method create = homeInterface.getDeclaredMethod("create", params);

            Object ejbHome = ctx.lookup("MEJB");
            Object ejbObject = create.invoke(ejbHome);

            return ejbObject;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * The main method of this JSP
     */
    public void main(final HttpServletRequest request, final HttpSession session, final JspWriter out) throws Exception {
        final ClassLoader myLoader = this.getClass().getClassLoader();
        final Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");

        final InitialContext ctx = new InitialContext(p);

        // ---------------------------------------------------
        //  Can I lookup a home interface from the testsuite?
        // ---------------------------------------------------

        printTest(out, "Looking up an ejb home", new TestAction() {
            @Override
            public String run() throws Exception {
                Object ejbHome = ctx.lookup("MEJB");
                if (ejbHome instanceof java.rmi.Remote) {
                    return OK;
                } else {
                    return FAIL;
                }
            }
        });

        // ---------------------------------------------------
        //  Is the home interface visible?
        // ---------------------------------------------------

        printTest(out, "Checking for the home interface class definition", new TestAction() {
            @Override
            public String run() throws Exception {
                Class.forName("javax.management.j2ee.ManagementHome", true, myLoader);
                return OK;
            }
        });

        // ---------------------------------------------------
        //  Can I invoke a create method on the ejb home?
        // ---------------------------------------------------

        printTest(out, "Invoking the create method on the ejb home", new TestAction() {
            @Override
            public String run() throws Exception {
                Object ejbObject = getEjbObj(ctx, myLoader);
                if (java.rmi.Remote.class.isInstance(ejbObject)) {
                    return OK;
                } else {
                    return FAIL;
                }
            }
        });

        // ---------------------------------------------------
        //  Is the remote interface visible?
        // ---------------------------------------------------

        printTest(out, "Checking for the remote interface class definition", new TestAction() {
            @Override
            public String run() throws Exception {
                Class.forName("javax.management.j2ee.Management", true, myLoader);
                return OK;
            }
        });

        // ---------------------------------------------------
        //  Can I invoke a business method on the ejb object?
        // ---------------------------------------------------

        printTest(out, "Invoking a business method on the ejb object", new TestAction() {
            @Override
            public String run() throws Exception {
                Class remoteInterface = Class.forName("javax.management.j2ee.Management", true, myLoader);
                Method businessMethod = remoteInterface.getDeclaredMethod("getMBeanCount");
                Object ejbObject = getEjbObj(ctx, myLoader);

                Object returnValue = null;
                if(ejbObject != null) {
                    returnValue = businessMethod.invoke(ejbObject);
                }

                if (java.lang.Integer.class.isInstance(returnValue)) {
                    return OK;
                } else {
                    return FAIL;
                }
            }
        });

    }

    private interface TestAction {
        String run() throws Exception;
    }

    protected void printTest(JspWriter out, String test, TestAction testAction) throws IOException {
        out.print("<tr><td>");
        out.print(test);
        out.print("</td><td>");
        try {
            out.print(testAction.run());
        } catch (Exception e) {
            out.print(FAIL + "<BR>" + formatThrowable(e));
        }
        out.print("</td></tr>");
    }

    public String formatThrowable(Throwable err) {
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

