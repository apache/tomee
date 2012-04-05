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

<%@ page import="org.apache.tomee.installer.Installer" %>
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
            <div class="span9">
                <div class="hero-unit">
                    <h1>Welcome to Apache TomEE</h1>
                    <p>You can test your setup and play around with the tools provided below!</p>
                </div>
                <div class="row-fluid">
                    <div class="span4">
                        <h2>Setup</h2>
                        <p>Testing your setup</p>
                        <p><a class="btn" href="testhome.jsp">View details &raquo;</a></p>
                    </div><!--/span-->


                    <% if (!Installer.isListenerInstalled() && !Installer.isAgentInstalled()) { %>
                    <div class="span4">
                        <h2>Install</h2>
                        <p>[Optional] Install Listener and JavaAgent</p>
                        <p><a class="btn" href="installer">View details &raquo;</a></p>
                    </div><!--/span-->

                    <% } else if (!Installer.isListenerInstalled()) { %>
                    <div class="span4">
                        <h2>Install</h2>
                        <p>[Optional] Install Listener</p>
                        <p><a class="btn" href="installer">View details &raquo;</a></p>
                    </div><!--/span-->

                    <% } else if (!Installer.isAgentInstalled()) { %>
                    <div class="span4">
                        <h2>Install</h2>
                        <p>[Optional] JavaAgent</p>
                        <p><a class="btn" href="installer">View details &raquo;</a></p>
                    </div><!--/span-->

                    <% } %>






                    <div class="span4">
                        <h2>JNDI Browser</h2>
                        <p>Lookup for beans from your web browser</p>
                        <p><a class="btn" href="viewjndi.jsp">View details &raquo;</a></p>
                    </div><!--/span-->
                    <div class="span4">
                        <h2>Class Viewer</h2>
                        <p>See the classes your application have</p>
                        <p><a class="btn" href="viewclass.jsp">View details &raquo;</a></p>
                    </div><!--/span-->
                </div><!--/row-->
                <div class="row-fluid">
                    <div class="span4">
                        <h2>EJB Viewer</h2>
                        <p>See the EJBs you have in your context</p>
                        <p><a class="btn" href="viewejb.jsp">View details &raquo;</a></p>
                    </div><!--/span-->
                    <div class="span4">
                        <h2>Object Invoker</h2>
                        <p>Execute an EJB method from the browser</p>
                        <p><a class="btn" href="invokeobj.jsp">View details &raquo;</a></p>
                    </div><!--/span-->

                </div><!--/row-->
            </div><!--/span-->
        </div><!--/row-->

        <hr>

        <footer>
            <p>Copyright &copy; 2012  The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and the Apache feather logo are trademarks of The Apache Software Foundation.</p>
        </footer>

    </div><!--/.fluid-container-->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="js/jquery/jquery-1.7.1.js"></script>
    <script src="js/bootstrap/bootstrap.js"></script>

</body>
</html>
