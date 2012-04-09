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
    <meta charset="utf-8">
    <title>TomEE</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }
        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">

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
<script src="js/jquery/jquery-1.7.1.js"></script>
<script src="js/bootstrap/bootstrap.js"></script>

</body>
</html>

<%!
    static String invLock = "lock";
    static int invCount;

    String OK = "<span style='color: green'><b>OK</b></span>";
    String FAIL = "<span style='color: red'><b>FAIL</b></span>";

    /**
     * The main method of this JSP
     */
    public void main(final HttpServletRequest request, final HttpSession session, final JspWriter out) throws Exception {
        final InitialContext ctx;
        try {
            Properties p = new Properties();

            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
            p.put("openejb.loader", "embed");

            ctx = new InitialContext(p);

        } catch (Exception e) {
            formatThrowable(e);
            return;
        }

        // ---------------------------------------------------
        //  Were the OpenEJB classes installed?
        // ---------------------------------------------------

        printTest(out, "Were the OpenEJB classes installed", new TestAction() {
            @Override
            public String run() {
                ClassLoader myLoader = null;
                Class openejb = null;
                try {
                    myLoader = this.getClass().getClassLoader();
                    openejb = Class.forName("org.apache.openejb.OpenEJB", true, myLoader);
                    return OK;
                } catch (Exception e) {
                    return FAIL;
                }
            }
        });


        // ---------------------------------------------------
        //  Are the EJB libraries visible?
        // ---------------------------------------------------

        printTest(out, "Were the EJB classes installed", new TestAction() {
            @Override
            public String run() {

                try {
                    Class.forName("javax.ejb.EJBHome", true, this.getClass().getClassLoader());
                    return OK;
                } catch (Exception e) {
                    return FAIL;
                }
            }
        });


        // ---------------------------------------------------
        //  Was OpenEJB initialized (aka started)?
        // ---------------------------------------------------

        printTest(out, "Was OpenEJB initialized (aka started)", new TestAction() {
            @Override
            public String run() {
                try {
                    Class openejb = Class.forName("org.apache.openejb.OpenEJB", true, this.getClass().getClassLoader());
                    Method isInitialized = openejb.getDeclaredMethod("isInitialized");
                    Boolean running = (Boolean) isInitialized.invoke(openejb);

                    if (running) {
                        return OK;
                    } else {
                        return FAIL;
                    }
                } catch (Exception e) {
                    return FAIL;
                }
            }
        });

        // ---------------------------------------------------
        //  Can I lookup anything?
        // ---------------------------------------------------

        printTest(out, "Performing a test lookup", new TestAction() {
            @Override
            public String run() {
                try {
                    Object obj = ctx.lookup("");

                    if (obj.getClass().getName().equals("org.apache.openejb.core.ivm.naming.IvmContext")) {
                        return OK;
                    } else {
                        return FAIL;
                    }

                } catch (Exception e) {
                    return FAIL;
                }
            }
        });

    }

    private interface TestAction {
        String run();
    }

    protected void printTest(JspWriter out, String test, TestAction testAction) throws IOException {
        out.print("<tr><td>");
        out.print(test);
        out.print("</td><td>");
        out.print(testAction.run());
        out.print("</td></tr>");
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

