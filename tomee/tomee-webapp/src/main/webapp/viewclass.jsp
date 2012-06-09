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
java.lang.reflect.Method,
java.lang.reflect.Modifier,
java.util.Map
"%>
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
                    <li><a href="viewejb.jsp">EJB</a></li>
                    <li class="active"><a href="viewclass.jsp">Class</a></li>
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
               
     String className = request.getParameter("class");
    try{
       
        if (className == null || className.trim().length() == 0) {
%>
<form class="well form-search" NAME='view' METHOD='GET' ACTION='viewclass.jsp'>
    <input type="text" class="input-xlarge search-query" NAME='class' placeholder="Class name">
    <button class="btn" type="submit"><i class="icon-search"></i> Search</button>
</form>
<%
            out.print("<table class='table table-striped table-bordered table-condensed'><tbody>");
            out.print("<tr><td>" + getClassRef("javax.ejb.EJBHome") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.ejb.EJBObject") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.ejb.EnterpriseBean") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.ejb.SessionBean") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.ejb.EntityBean") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.servlet.http.HttpServlet") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.servlet.http.HttpServletRequest") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.servlet.http.HttpServletResponse") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.servlet.http.HttpSession") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.naming.InitialContext") + "</td></tr>");
            out.print("<tr><td>" + getClassRef("javax.naming.Context") + "</td></tr>");
            out.print("</tbody></table>");

        } else {
            Class clazz = (Class)session.getAttribute(className); 
            if(clazz == null)
            clazz = Class.forName(className);
            printClass(clazz,out,session);
        }
    } catch (Exception e){
        out.println("Could not find class "+className+" <br/>");
        out.println("<a href='viewclass.jsp'>Back</a>");
      //  throw e;
        //return;
    }
%>


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

    public void printClass(Class clazz, javax.servlet.jsp.JspWriter out, HttpSession session) throws Exception {
        out.print("<h2>"+clazz.getName()+"</h2>");
        Method[] methods = clazz.getDeclaredMethods();
        out.print("<table class='table table-striped table-bordered table-condensed'><tbody>");
        for (int i=0; i < methods.length; i++){
            printMethod( methods[i], out );
        }
        out.print("</tbody></table>");

        Class sup = clazz.getSuperclass();
        if (sup != null) {
            out.print("<h3>Extends:</h3>");
            out.print("<p>" + getClassRef(sup,session) + "</p>");
        }

        Class[] intf = clazz.getInterfaces();
        if (intf.length > 0) {
            out.print("<h3>Implements:</h3>");
            out.print("<table class='table table-striped table-bordered table-condensed'><tbody>");
            for (int i=0; i < intf.length; i++){
                out.print("<tr><td>" + getClassRef(intf[i],session) + "</td></tr>");
            }
            out.print("</tbody></table>");
        }
    }

    public void printMethod(Method m, javax.servlet.jsp.JspWriter out) throws Exception {
        out.print("<tr><td>");
        out.print(" "+getModifier(m));
        
        out.print(" "+getShortClassRef(m.getReturnType())+"&nbsp;&nbsp;");

        out.print(""+m.getName()+"&nbsp;");
        Class[] params = m.getParameterTypes();
        out.print("<font color='gray'>(</font>");
        for (int j=0; j < params.length; j++){
            out.print(getShortClassRef(params[j]));
            if (j != params.length-1) {
                out.print(",&nbsp;");
            }
        }
        out.print("<font color='gray'>)</font>");

        Class[] excp = m.getExceptionTypes();
        if (excp.length > 0) {
            out.print(" <font color='gray'>throws</font>&nbsp;&nbsp;");
            for (int j=0; j < excp.length; j++){
                out.print(getShortClassRef(excp[j]));
                if (j != excp.length-1) {
                    out.print(",&nbsp;");
                }
            }
        }
        out.print("</td></tr>");
    }
    public String getModifier(Method m) throws Exception {
        int mod = m.getModifiers();

        if (Modifier.isPublic(mod)) {
            return "public";

        } else if (Modifier.isPrivate(mod)) {
            return "private";

        } else if (Modifier.isProtected(mod)) {
            return "protected";

        } else {
            return "";
        }
    }

/*    public String getClassRef(Class clazz) throws Exception {
            String name = clazz.getName();
            return "<a href='viewclass.jsp?class="+name+"'>"+name+"</a>";
    }
  */    
    public String getClassRef(String name) throws Exception {
            return "<a href='viewclass.jsp?class=" +name + "'>" + name + "</a>";
    }

   public String getClassRef(Class clazz, HttpSession session) throws Exception {
        String name = clazz.getName();
        session.setAttribute(name,clazz);
        return getClassRef(name);
    }

    public String getShortClassRef(Class clazz) throws Exception {
        if (clazz.isPrimitive()) {
            return "<font color='gray'>"+clazz.getName()+"</font>";
        } else if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
            return "<font color='gray'>"+clazz.getComponentType()+"[]</font>";
        } else if (clazz.isArray()) {
            String name = clazz.getComponentType().getName();
            int dot = name.lastIndexOf(".")+1;
            String shortName = name.substring(dot,name.length());
            return "<a href='viewclass.jsp?class="+name+"'>"+shortName+"[]</a>";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".")+1;
            String shortName = name.substring(dot,name.length());
            return "<a href='viewclass.jsp?class="+name+"'>"+shortName+"</a>";
        }
    }


%>

