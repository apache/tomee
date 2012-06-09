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
org.apache.openejb.BeanType,
org.apache.openejb.BeanContext,
org.apache.openejb.loader.SystemInstance,
org.apache.openejb.spi.ContainerSystem,
javax.naming.Context,
javax.naming.InitialContext
"%>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.lang.reflect.Field" %>
<%@ page import="java.lang.reflect.Method" %>
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
                    <li><a href="viewjndi.jsp">JNDI</a></li>
                    <li class="active"><a href="viewejb.jsp">EJB</a></li>
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
    <%
        try{
            String ejb = request.getParameter("ejb");
            String jndiName = request.getParameter("jndiName");
            String contextID = request.getParameter("ctxID");
            if (ejb == null) {
                out.print("<p>No EJB specified</p>");
            } else {
                printEjb(ejb,jndiName,contextID,out, session);

            }
        } catch (Exception e){

            out.println("<p>FAIL: <br>");
            out.print(e.getMessage() + "</p>");
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
    private BeanContext getDeployment(String deploymentID) {
        try {
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            BeanContext ejb = containerSystem.getBeanContext(deploymentID);
            return ejb;
        } catch (Exception e) {
            return null;
        }
    }

    public void printEjb(String name,String jndiName, String contextID, javax.servlet.jsp.JspWriter out, HttpSession session) throws Exception {
        String id = (name.startsWith("/")) ? name.substring(1, name.length()) : name;
        BeanContext ejb = getDeployment(id);

        if (ejb == null) {
            out.print("<p>No such EJB: " + id + "</p>");
            return;
        }
        String type = null;

        switch (ejb.getComponentType()) {
            case CMP_ENTITY:
                type = "EntityBean with Container-Managed Persistence";
                break;
            case BMP_ENTITY:
                type = "EntityBean with Bean-Managed Persistence";
                break;
            case STATEFUL:
                type = "Stateful SessionBean";
                break;
            case STATELESS:
                type = "Stateless SessionBean";
                break;
            case SINGLETON:
                type = "Singleton SessionBean";
                break;
            case MANAGED:
                type = "Managed SessionBean";
                break;
            default:
                type = "Unkown Bean Type";
                break;
        }
        out.print("<h2>" + type + "</h2>");
        out.print("<table class=\"table table-striped table-bordered\"><tbody>");
        printRow("JNDI Name", jndiName, out);
        if(ejb.getRemoteInterface() != null)
        printRow("Remote Interface", getClassRef(ejb.getRemoteInterface(),session), out);
        if(ejb.getHomeInterface() != null)
        printRow("Home Interface", getClassRef(ejb.getHomeInterface(),session), out);
        if(ejb.getBeanClass() != null)
        printRow("Bean Class", getClassRef(ejb.getBeanClass(),session), out);
        if(ejb.getBusinessLocalInterfaces().size() > 0)
        printRow("Business Local Interfaces", getClassRefs(ejb.getBusinessLocalInterfaces(),session), out);
        if(ejb.getBusinessRemoteInterfaces().size() > 0)
        printRow("Business Remote Interfaces", getClassRefs(ejb.getBusinessRemoteInterfaces(),session), out);        
        if (ejb.getComponentType() == BeanType.BMP_ENTITY || ejb.getComponentType() == BeanType.CMP_ENTITY) {
            printRow("Primary Key", getClassRef(ejb.getPrimaryKeyClass(),session), out);
        }
        out.print("</tbody></table>");

        // Browse JNDI with this ejb
        //javax.servlet.http.HttpSession session = this.session;
        //noinspection unchecked
        Map<String, Object> objects = (Map<String, Object>) session.getAttribute("objects");

        Context ctx;
        if(contextID == null){
        Properties p = new Properties();

        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");

        ctx = new InitialContext(p);
        }else{
          ctx = (Context)session.getAttribute(contextID);
        }
        Object obj = ctx.lookup(jndiName);
 //       String objID = ejb.getHomeInterface().getName() + "@" + obj.hashCode(); 
        String objID = ""+obj.hashCode(); //TODO: Not the best of the ID's, more meaningful ID would be better. Right now hashcode would suffice
        objects.put(objID, obj);
        String invokeURL = "<a class='btn' href='invokeobj.jsp?obj=" + objID + "'>Invoke this EJB</a>";

        Context enc = ejb.getJndiEnc();
        String ctxID = "enc" + enc.hashCode();
        session.setAttribute(ctxID, enc);
        String jndiURL = "<a class='btn' href='viewjndi.jsp?ctxID=" + ctxID + "'>Browse this EJB's private JNDI namespace</a>";

        out.print("<div class='btn-group'>" + invokeURL + jndiURL + "</div>");
    }

    protected void printRow(String col1, String col2, javax.servlet.jsp.JspWriter out) throws IOException {
        out.print("<tr>");
        out.print("<td>" + col1 + "</td>");
        out.print("<td>" + col2 + "</td>");
        out.print("</tr>");
    }

    public String getClassRef(Class clazz, HttpSession session) throws Exception {
        String name = clazz.getName();
        session.setAttribute(name,clazz);
        return "<a href='viewclass.jsp?class=" + name + "'>" + name + "</a>";
    }

    public String getClassRefs(List<Class> classes, HttpSession session) throws Exception{
        String refs = "";
        for(Class clazz: classes){
           refs += getClassRef(clazz,session)+"<br/>";
        }
        return refs;
    }
%>

