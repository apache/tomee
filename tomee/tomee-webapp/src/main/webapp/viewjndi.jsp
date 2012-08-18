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
javax.naming.NameClassPair,
javax.naming.NamingEnumeration,
java.util.Properties
"%>
<%@ page import="javax.naming.NamingException" %>
<%@ page import="java.lang.reflect.Method" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="org.apache.openejb.util.proxy.LocalBeanProxyFactory" %>
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
                    <li><a href="index.jsp">Index</a></li>
                    <li class="active"><a href="viewjndi.jsp">JNDI</a></li>
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
            try{
                String selected = request.getParameter("selected");
                if (selected == null) {
                    selected = "";
                }

                ctxID = request.getParameter("ctxID");
                ctx = null;

                if (ctxID == null) {
                    Properties p = new Properties();
                    p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
                    p.put("openejb.loader", "embed");
                    try {
                        ctx = new InitialContext( p );
                        out.print("<h2>OpenEJB Global JNDI Namespace</h2>");
                    } catch(Exception e) {
                        out.print("<h2>OpenEJB Not Installed</h2>");
                    }
                } else {
                    ctx = (Context)session.getAttribute(ctxID);
                    if (ctxID.startsWith("enc")) {
        %>
        <h2>JNDI Environment Naming Context <a href="#" rel="tooltip" title="This is the private namespace of an Enterprise JavaBean">(ENC)</a></h2>
        <%
                    }
                }

                if (ctx != null) {
                    Node root = new RootNode();
                    buildNode(root,ctx);
                    out.println("<p>");
                    printNodes(root, out, "",selected);
                    out.println("</p>");
                }
            } catch (Exception e) {
                out.println("<p>FAIL</p>");
                throw e;
                //return;
            }
        %>
            </div>
        </div>

        <hr>

        <footer>
            <p>Copyright &copy; 2012  The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and the Apache feather logo are trademarks of The Apache Software Foundation.</p>
        </footer>
    </div> <!-- /container -->


<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="lib/jquery/jquery-1.7.2.min.js"></script>
<script src="lib/bootstrap/2.0.4/js/bootstrap.js"></script>

</body>
</html>


<%!
    String ctxID;
    Context ctx;

    class Node {
        static final int CONTEXT = 1;
        static final int BEAN = 2;
        static final int OTHER = 3;
        Node parent;
        Node[] children = new Node[0];
        String name;
        int type = 0;
		// returns the JNDI name
        public String getID() {
            if (parent instanceof RootNode) {
                return name;
            } else {
                return parent.getID() + "/" + name;
            }
        }

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }

        public void addChild(Node child) {
            int len = children.length;
            Node[] newChildren = new Node[len + 1];
            System.arraycopy(children, 0, newChildren, 0, len);
            newChildren[len] = child;
            children = newChildren;
            child.parent = this;
        }
    }

    class RootNode extends Node {
        public String getID() {
            return "";
        }

        public String getName() {
            return "";
        }

        public int getType() {
            return Node.CONTEXT;
        }
    }

    public void buildNode(Node parent, Context ctx) throws Exception {
        NamingEnumeration namingEnumeration = ctx.list("");
        while (namingEnumeration.hasMoreElements()) {
            NameClassPair pair = (NameClassPair) namingEnumeration.next();
            Node node = new Node();
            parent.addChild(node);
            node.name = pair.getName();

            Object obj = lookup(ctx, node.getName());
            if (obj instanceof Context) {
                node.type = Node.CONTEXT;
                buildNode(node, (Context) obj);
            } else if (obj instanceof java.rmi.Remote
                || obj instanceof org.apache.openejb.core.ivm.IntraVmProxy
                || (obj != null && LocalBeanProxyFactory.isProxy(obj.getClass()))) {
                node.type = Node.BEAN;
            } else {
                node.type = Node.OTHER;
            }
        }
    }

    String openImg = "<img src='images/TreeOpen.gif' border='0'>";
    String closedImg = "<img src='images/TreeClosed.gif' border='0'>";
    String ejbImg = "<img src='images/ejb.gif' border='0'>";
    String javaImg = "<img src='images/JavaCup.gif' border='0'>";


    public void printNodes(Node node, javax.servlet.jsp.JspWriter out, String tabs, String selected) throws Exception {
        switch (node.getType()) {
            case Node.CONTEXT:
                printContextNode(node, out, tabs, selected);
                break;
            case Node.BEAN:
                printBeanNode(node, out, tabs);
                break;
            default:
                printOtherNode(node, out, tabs);
                break;
        }

    }

    public void printContextNode(Node node, javax.servlet.jsp.JspWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
        if (selected.startsWith(id)) {
            if (ctxID != null) {
                out.print(tabs + "<a href='viewjndi.jsp?ctxID=" + ctxID + "&selected=" + id + "'>" + openImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } else {
                out.print(tabs + "<a href='viewjndi.jsp?selected=" + id + "'>" + openImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            }
            for (int i = 0; i < node.children.length; i++) {
                Node child = node.children[i];
                printNodes(child, out, tabs + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", selected);
            }
        } else {
            if (ctxID != null) {
                out.print(tabs + "<a href='viewjndi.jsp?ctxID=" + ctxID + "&selected=" + id + "'>" + closedImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } else {
                out.print(tabs + "<a href='viewjndi.jsp?selected=" + id + "'>" + closedImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            }
        }
    }

    public void printBeanNode(Node node, javax.servlet.jsp.JspWriter out, String tabs) throws Exception {
        String id = node.getID();
        if (ctxID != null && ctxID.startsWith("enc")) {
            // HACK!
            try {
                Object ejb = lookup(ctx, id);
                Object deploymentID = getDeploymentId(ejb);
                out.print(tabs + "<a href='viewejb.jsp?ejb=" + deploymentID +"&jndiName="+id +"&ctxID="+ctxID+"'>" + ejbImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } catch (Exception e) {
                out.print(tabs + ejbImg + "&nbsp;&nbsp;" + node.getName() + "<br>");
            }
        } else {
            try {
                Object ejb = lookup(ctx, id);
                Object deploymentID = getDeploymentId(ejb);
                out.print(tabs + "<a href='viewejb.jsp?ejb=" + deploymentID +"&jndiName="+id +"'>" + ejbImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } catch (Exception e) {
                out.print(tabs + ejbImg + "&nbsp;&nbsp;" + node.getName() + "<br>");
            }
        }
    }

    public void printOtherNode(Node node, javax.servlet.jsp.JspWriter out, String tabs) throws Exception {
        String id = node.getID();
        Object obj = lookup(ctx, id);
        String clazz = obj.getClass().getName();
        out.print(tabs + "<a href='viewclass.jsp?class=" + clazz + "'>" + javaImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
    }

    private Object getDeploymentId(Object ejb) throws Exception {
        org.apache.openejb.core.ivm.BaseEjbProxyHandler handler = (org.apache.openejb.core.ivm.BaseEjbProxyHandler)org.apache.openejb.util.proxy.ProxyManager.getInvocationHandler(ejb);
        return handler.deploymentID;
        
    }

    private Object lookup(Context ctx, String name) throws NamingException {
        try {
            Object obj = ctx.lookup(name);
            return obj;
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

%>

