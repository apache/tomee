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

<!-- $Rev: 546344 $ $Date: 2007-06-11 18:10:15 -0700 (Mon, 11 Jun 2007) $ -->

<%@ page import="
javax.naming.InitialContext,
javax.naming.Context,
javax.naming.*,
java.util.Properties,
javax.naming.Context,
javax.naming.InitialContext,
javax.servlet.ServletConfig,
javax.servlet.ServletException,
javax.servlet.http.HttpServlet,
javax.servlet.http.HttpServletRequest,
javax.servlet.http.HttpServletResponse,
java.io.PrintWriter,
java.io.*,
java.lang.reflect.Method,
java.lang.reflect.Modifier
"%>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Tomcat Integration/1.0</title>
    <link href="default.css" rel="stylesheet">
    <!-- $Id: viewclass.jsp 445516 2005-07-04 08:10:54Z dblevins $ -->
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
            <td align="left" valign="middle" width="530"><a href="http://www.openejb.org"><span class="menuTopOff">OpenEJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="index.html"><span class="menuTopOff">Index</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewjndi.jsp"><span class="menuTopOff">JNDI</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewejb.jsp"><span class="menuTopOff">EJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewclass.jsp"><span class="menuTopOff">Class</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="invokeobj.jsp"><span class="menuTopOff">Invoke</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"></td>
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
                            OpenEJB Class Viewer
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
        String className = request.getParameter("class");
        if (className == null) {
            out.print("<b>Enter a class name to browse:</b>");
            out.print("<FORM NAME='view' METHOD='GET' ACTION='viewclass.jsp'>");
            out.print("<INPUT type='text' NAME='class' size='40' VALUE=''>");
            out.print("<INPUT type='SUBMIT' NAME='view' value='View'>");
            out.print("</form>");
            out.print("<b>Or browse one of these fun classes:</b><br><br>");
            out.print(tab+getClassRef("javax.ejb.EJBHome")+"<br>");
            out.print(tab+getClassRef("javax.ejb.EJBObject")+"<br>");
            out.print(tab+getClassRef("javax.ejb.EnterpriseBean")+"<br>");
            out.print(tab+getClassRef("javax.ejb.SessionBean")+"<br>");
            out.print(tab+getClassRef("javax.ejb.EntityBean")+"<br>");
            out.print(tab+getClassRef("javax.servlet.http.HttpServlet")+"<br>");
            out.print(tab+getClassRef("javax.servlet.http.HttpServletRequest")+"<br>");
            out.print(tab+getClassRef("javax.servlet.http.HttpServletResponse")+"<br>");
            out.print(tab+getClassRef("javax.servlet.http.HttpSession")+"<br>");
            out.print(tab+getClassRef("javax.naming.InitialContext")+"<br>");
            out.print(tab+getClassRef("javax.naming.Context")+"<br>");

        } else {
            Class clazz = this.getClass().forName(className);
            printClass(clazz,out);
        }
    } catch (Exception e){
        out.println("FAIL");
        return;
    }
%>
<BR><BR>
<% if (hasMethods) { %>
<font color='green'>*</font>&nbsp;Public &nbsp; 
<font color='red'>*</font>&nbsp;Private &nbsp;
<font color='blue'>*</font>&nbsp;Protected &nbsp;
<font color='yellow'>*</font>&nbsp;Default 
<BR>
<%}%>
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
    boolean hasMethods;

    public void printClass(Class clazz, javax.servlet.jsp.JspWriter out) throws Exception {
        out.print("<b>"+clazz.getName()+"</b><br>");
        Method[] methods = clazz.getDeclaredMethods();
        hasMethods = (methods.length > 0);
        for (int i=0; i < methods.length; i++){
            printMethod( methods[i], out );
        }

/*        //out.print("&nbsp;&nbsp;<font color='gray'><u>Public Methods:</u></font><br>");
        for (int i=0; i < methods.length; i++){
            if (Modifier.isPublic(methods[i].getModifiers())){
                printMethod( methods[i], out );
            }
        }
        //out.print("&nbsp;&nbsp;<font color='gray'><u>Private Methods:</u></font><br>");
        for (int i=0; i < methods.length; i++){
            if (Modifier.isPrivate(methods[i].getModifiers())){
                printMethod( methods[i], out );
            }
        }
        for (int i=0; i < methods.length; i++){
            if (Modifier.isProtected(methods[i].getModifiers())){
                printMethod( methods[i], out );
            }
        }
        for (int i=0; i < methods.length; i++){
            if (!Modifier.isSrict(methods[i].getModifiers())){
                printMethod( methods[i], out );
            }
        }
*/         
        Class sup = clazz.getSuperclass();
        if (sup != null) {
            out.print("<br><b>Extends:</b><br>");
            out.print(tab+getClassRef(sup)+"<br>");
        }

        Class[] intf = clazz.getInterfaces();
        if (intf.length > 0) {
            out.print("<br><b>Implements:</b><br>");
            for (int i=0; i < intf.length; i++){
                out.print(tab+getClassRef(intf[i])+"<br>");
            }
        }
    }

    public void printMethod(Method m, javax.servlet.jsp.JspWriter out) throws Exception {
        out.print(tab);
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
        out.print("<br>");
    }
    public String getModifier(Method m) throws Exception {
        int mod = m.getModifiers();
        String color = "";

        if (Modifier.isPublic(mod)) {
            color = "green";
        } else if (Modifier.isPrivate(mod)) {
            color = "red";
        } else if (Modifier.isProtected(mod)) {
            color = "blue";
        } else {
            color = "yellow";
        }
        return "<font color='"+color+"'>*</font>";
    }

    public String getClassRef(Class clazz) throws Exception {
            String name = clazz.getName();
            return "<a href='viewclass.jsp?class="+name+"'>"+name+"</a>";
    }
    
    public String getClassRef(String name) throws Exception {
            return "<a href='viewclass.jsp?class="+name+"'>"+name+"</a>";
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

