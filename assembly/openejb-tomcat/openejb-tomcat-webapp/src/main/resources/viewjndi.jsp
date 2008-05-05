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
        <td bgcolor="#5A5CB8" align="left" valign="top" height="2" width="430"><img border="0" height="6" width="430" src="images/top_2.gif"></td>
        <td bgcolor="#E24717" align="left" valign="top" height="2" width="120"><img src="images/top_3.gif" width="120" height="6" border="0"></td>
    </tr>
    <tr>
        <td bgcolor="#5A5CB8" align="left" valign="top" bgcolor="#ffffff" width="13"><img border="0" height="15" width="13" src="images/dotTrans.gif"></td>
        <td align="left" valign="top" width="40"><img border="0" height="1" width="1" src="images/dotTrans.gif"></td>
        <td align="left" valign="middle" width="430"><a href="http://openejb.apache.org"><span class="menuTopOff">OpenEJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="index.jsp"><span class="menuTopOff">Index</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewjndi.jsp"><span class="menuTopOff">JNDI</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewejb.jsp"><span class="menuTopOff">EJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewclass.jsp"><span class="menuTopOff">Class</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="invokeobj.jsp"><span class="menuTopOff">Invoke</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"></td>
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
                            OpenEJB JNDI Namespace Browser
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
        String selected = request.getParameter("selected");
        if (selected == null) {
            selected = "";
        } 

        ctxID = request.getParameter("ctx");
        ctx = null;

        if (ctxID == null) {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put("openejb.loader", "embed");
            try {
                ctx = new InitialContext( p );
                ctxID = null;
                out.print("<b>OpenEJB Global JNDI Namespace</b><br><br>");
            } catch(Exception e) {
                out.print("<b>OpenEJB Not Installed</b><br><br>");                
            }
        } else {
            ctx = (Context)session.getAttribute(ctxID);
            if (ctxID.startsWith("enc")) {
%>
<b>JNDI Environment Naming Context (ENC)</b>
<a href="enc-help.html">[Info]</A>
<br><BR>
This is the private namespace of an Enterprise JavaBean. 
<BR><BR>
<%
            }
        }

        if (ctx != null) {
            Node root = new RootNode();
            buildNode(root,ctx);

            printNodes(root, out, "",selected);
        }
    } catch (Exception e) {
        out.println("FAIL");
        throw e;
        //return;
    }
%>
</FONT>

            </td>
            <td align="left" valign="top" height="5" width="120">


                &nbsp;</td>
        </tr>
    </table>
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
            } else if (obj instanceof java.rmi.Remote) {
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
                printBeanNode(node, out, tabs, selected);
                break;
            default:
                printOtherNode(node, out, tabs, selected);
                break;
        }

    }

    public void printContextNode(Node node, javax.servlet.jsp.JspWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
        if (selected.startsWith(id)) {
            if (ctxID != null) {
                out.print(tabs + "<a href='viewjndi.jsp?ctx=" + ctxID + "&selected=" + id + "'>" + openImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } else {
                out.print(tabs + "<a href='viewjndi.jsp?selected=" + id + "'>" + openImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            }
            for (int i = 0; i < node.children.length; i++) {
                Node child = node.children[i];
                printNodes(child, out, tabs + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", selected);
            }
        } else {
            if (ctxID != null) {
                out.print(tabs + "<a href='viewjndi.jsp?ctx=" + ctxID + "&selected=" + id + "'>" + closedImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } else {
                out.print(tabs + "<a href='viewjndi.jsp?selected=" + id + "'>" + closedImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            }
        }
    }

    public void printBeanNode(Node node, javax.servlet.jsp.JspWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
        if (ctxID != null && ctxID.startsWith("enc")) {
            // HACK!
            try {
                Object ejb = lookup(ctx, id);
                Object deploymentID = getDeploymentId(ejb);
                out.print(tabs + "<a href='viewejb.jsp?ejb=" + deploymentID + "'>" + ejbImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
            } catch (Exception e) {
                out.print(tabs + ejbImg + "&nbsp;&nbsp;" + node.getName() + "<br>");
            }
        } else {
            out.print(tabs + "<a href='viewejb.jsp?ejb=" + id + "'>" + ejbImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
        }
    }

    public void printOtherNode(Node node, javax.servlet.jsp.JspWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
        Object obj = lookup(ctx, id);
        String clazz = obj.getClass().getName();
        out.print(tabs + "<a href='viewclass.jsp?class=" + clazz + "'>" + javaImg + "&nbsp;&nbsp;" + node.getName() + "</a><br>");
    }

    private Object getDeploymentId(Object ejb) throws Exception {
        Class<?> proxyManagerClass = Class.forName("org.apache.openejb.util.proxy.ProxyManager");
        Method getInvocationHandlerMethod = proxyManagerClass.getMethod("getInvocationHandler", Object.class);
        Object handler = getInvocationHandlerMethod.invoke(null, ejb);

        Class<?> baseEjbProxyHandler = Class.forName("org.apache.openejb.core.ivm.BaseEjbProxyHandler");
        Field field = baseEjbProxyHandler.getField("deploymentID");
        Object deploymentID = field.get(handler);
        return deploymentID;
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

