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
javax.servlet.http.HttpServletRequest,
javax.servlet.jsp.JspWriter,
java.io.ByteArrayOutputStream,
java.io.PrintStream,
java.lang.reflect.InvocationTargetException,
java.lang.reflect.Method,
java.lang.reflect.Modifier,
java.util.HashMap,
java.util.Iterator,
java.util.Set
"%>
<%@ page import="java.util.Map" %>
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
                    <li><a href="viewclass.jsp">Class</a></li>
                    <li class="active"><a href="invokeobj.jsp">Invoke</a></li>
                </ul>

            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <h2>OpenEJB Object Invoker</h2>
<%
    try{
        synchronized (this) {
            main(request, session, out);
        }
    } catch (Exception e){
        out.println("<p>FAIL</p>");
        //throw e;
        return;
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
    String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";

    static final String invLock = "lock";
    static int invCount;

    HttpSession session;
    HttpServletRequest request;
    JspWriter out;

    class Invocation {

        String id = "inv";
        String objID;
        Class clazz;
        Object target;
        Method method;
        Object[] args;
        Object result;

        Invocation() {
            synchronized (invLock) {
                id += ++invCount;
            }
        }

        public Object invoke() throws Exception {
            if (target == null || method == null || args == null) {
                throw new Exception("This invocation contains null objects.");
            }
            return method.invoke(target, args);
        }
    }

    /**
     * The main method of this JSP
     */
    public void main(HttpServletRequest request, HttpSession session, JspWriter out) throws Exception {
        this.request = request;
        this.session = session;
        this.out = out;

        printObjectSection();
    }

    /**
     * Print the list of objects with the focused object as
     * selected in the box.
     * If no object is selected, make an entry called "Pick an Object"
     */
    public void printObjectSection() throws Exception {
        String removeID = request.getParameter("remove");
        if (removeID != null) {
            removeObject(removeID);
        }

        Invocation inv = null;
        String invID = request.getParameter("inv");

        if (invID == null) {
            String objID = request.getParameter("obj");
            if (objID != null) {
                inv = new Invocation();
                inv.target = getObject(objID);
                inv.objID = objID;
                setInvocation(inv.id, inv);
            }
        } else {
            inv = getInvocation(invID);
        }

        if (inv == null || inv.target == null) {
            // Pick from the list
            printObjectList();

        } else {
            out.print("<h3>Object</h3>");
            out.print("<p>" + inv.objID + " <a href='invokeobj.jsp'>[change]</a></p>");

            // Show the selected item and continue
            printMethodSection(inv);
        }
    }

    /**
     * Prints the list of objects that can be invoked
     */
    public void printObjectList() throws Exception {

        Map<String, Object> objects = getObjectMap();
        if (objects.size() == 0) {
            out.print("<p>No object have been created. <A HREF='viewjndi.jsp'>Browse for an EJB</A></p>");

        } else {
            out.print("<h3>Pick and object to invoke</h3>");

            Set keys = objects.keySet();
            Iterator iterator = keys.iterator();
            out.print("<ul>");
            while (iterator.hasNext()) {
                String entry = (String) iterator.next();
                out.print("<li><a href='invokeobj.jsp?obj=" + entry + "'>" + entry + "</a><a href='invokeobj.jsp?remove=" + entry + "'> [remove]</a></li>");
            }
            out.print("</ul>");
        }
    }

    /**
     * Print the list of methods with the focused method as
     * selected in the box.
     * If no method is selected, make an entry called "Pick a Method"
     */
    public void printMethodSection(Invocation inv) throws Exception {
        String methodID = request.getParameter("m");

        if (methodID != null) {
            int method = Integer.parseInt(methodID);
            Method[] methods = inv.clazz.getMethods();
            if (method > -1 && method < methods.length) {
                inv.method = methods[method];
            } else {
                inv.method = null;
                inv.args = null;
            }
        }

        if (inv.method == null) {
            // Pick from the list
            printMethodList(inv);

        } else {
            out.print("<h3>Method</h3>");
            out.print("<p>" + formatMethod(inv.method) + " <a href='invokeobj.jsp?m=-1&inv=" + inv.id + "'>[change]</a></<p>");

            // Show the selected item and continue
            printArgumentSection(inv);
        }

    }

    /**
     * Prints the list of methods that can be invoked
     */
    public void printMethodList(Invocation inv) throws Exception {
        out.print("<b>Pick a method to invoke</b><br>");
        //out.print("<b>Methods:</b><br>");

        Object obj = inv.target;
        Class clazz = inv.target.getClass();
        if (obj instanceof javax.ejb.EJBHome) {
            clazz = obj.getClass().getInterfaces()[0];
        } else if (obj instanceof javax.ejb.EJBObject) {
            clazz = obj.getClass().getInterfaces()[0];
        } else {
            clazz = obj.getClass();
        }
        inv.clazz = clazz;

        out.print("<ul>");
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (Modifier.isPublic(m.getModifiers())) {
                out.print("<li><a href='invokeobj.jsp?inv=" + inv.id + "&m=" + i + "'>" + formatMethod(m) + "</a></li>");
            }
        }
        out.print("</ul>");
    }

    /**
     * Print the list of arguments.
     * If no arguments have been selected, 
     * show the argument entry form.
     */
    public void printArgumentSection(Invocation inv) throws Exception {
        String args = request.getParameter("args");

        if (args != null) {
            parseArgs(inv);
        }

        if (inv.method.getParameterTypes().length == 0) {
            inv.args = new Object[]{};
        }

        if (inv.args == null) {
            printArgumentList(inv);
        } else {
            out.print("<h3>Arguments</h3>");

            if (inv.args.length == 0) {
                out.print("<p>none</p>");
            }

            out.print("<ol>");

            for (int i = 0; i < inv.args.length; i++) {
                String val = formatObject(inv.args[i]);
                out.print("<li><i>" + val + "</i>");
            }

            out.print("</ol>");

            printInvokeSection(inv);
        }
    }

    public void parseArgs(Invocation inv) throws Exception {
        Class[] pTypes = inv.method.getParameterTypes();
        inv.args = new Object[pTypes.length];

        for (int i = 0; i < pTypes.length; i++) {
            Class type = pTypes[i];
            String unparsedArg = request.getParameter("arg" + i);
            inv.args[i] = getConverter(type).convert(type, unparsedArg);
        }
    }

    public void printArgumentList(Invocation inv) throws Exception {
        out.print("<b>Fill in the arguments</b><br>");
        Class[] pTypes = inv.method.getParameterTypes();
        out.print("<FORM NAME='args' METHOD='GET' ACTION='invokeobj.jsp'>");
        out.print("<INPUT type='HIDDEN' NAME='inv' VALUE='" + inv.id + "'>");
        out.print("<table>");
        for (int i = 0; i < pTypes.length; i++) {
            Converter con = getConverter(pTypes[i]);
            out.print("<tr>");
            out.print("<td align='right'><font size='2'>");
            out.print(tab + getShortClassRef(pTypes[i]));
            out.print("</font></td>");
            out.print("<td><font size='2'>");
            out.print("&nbsp;&nbsp;arg" + i);
            out.print("</font></td>");
            out.print("<td><font size='2'>");
            out.print("&nbsp;&nbsp;" + con.getInputControl(i, pTypes[i]));
            out.print("</font></td>");
        }
        out.print("</table>");

        out.print("<br><br>");
        out.print("<INPUT type='SUBMIT' NAME='args' value='Continue'>");
        out.print("</form>");

    }

    /**
     * Print the list of arguments.
     * If no arguments have been selected, 
     * show the argument entry form.
     */
    public void printInvokeSection(Invocation inv) throws Exception {
        String doInvoke = request.getParameter("invoke");
        if (doInvoke != null) {
            invoke(inv);
        } else {
            out.print("<FORM NAME='invoke' METHOD='GET' ACTION='invokeobj.jsp'>");
            out.print("<INPUT type='HIDDEN' NAME='inv' VALUE='" + inv.id + "'>");
            out.print("<INPUT type='SUBMIT' NAME='invoke' value='Invoke'>");
            out.print("</FORM>");
        }

    }

    String pepperImg = "<img src='images/pepper.gif' border='0'>";

    public void invoke(Invocation inv) throws Exception {

        try {
            inv.result = inv.invoke();

            out.print("<h3>Result:</h3>");
            if (inv.method.getReturnType() == java.lang.Void.TYPE) {
                out.print("<p>Done</p>");
            } else if (inv.result == null) {
                out.print("<p><i>null</i></p>");
            } else {
                String clazz = inv.result.getClass().getName();
                String objID = getObjectID(inv.result);
                setObject(objID, inv.result);

                out.print("<table class='table table-bordered table-striped'><colgroup><col class='span2'><col class='span10'></colgroup><tbody>");
                printRow("<i>id</i>", objID);
                printRow("<i>class</i>", "<a href='viewclass.jsp?class=" + clazz + "'>" + clazz + "</a>");
                printRow("<i>toString</i>", formatObject(inv.result));
                out.print("</tbody></table>");

                out.print("<h3>Actions:</h3>");
                out.print("<a href='invokeobj.jsp?obj=" + objID + "'>Invoke a method on the object</a> or <a href='invokeobj.jsp?remove="  + objID + "'>Discard the object</a>");
            }
        } catch (InvocationTargetException e) {
            out.print("<h3>Exception:</h3>");
            Throwable t = e.getTargetException();

            out.print("<p>");
            out.print("Received a " + t.getClass().getName());
            //out.print(inv.method+"<br><br>");
            if (t instanceof java.rmi.RemoteException) {
                out.print("<br><br>");
                out.print("<i>RemoteException message:</i><br>");
                out.print(t.getMessage());
                out.print("<br><br>");
                out.print("<i>Nested exception's stack trace:</i><br>");

                while (t instanceof java.rmi.RemoteException) {
                    t = ((java.rmi.RemoteException) t).detail;
                }
                out.print(formatThrowable(t));
            } else {
                out.print("<br><br>" + formatThrowable(t));
            }

            out.print("</p>");

        } catch (Throwable e) {
            out.print("<h3>Exception:</h3>");

            out.print("<p>");
            out.print(formatObject(e));
            out.print("</p>");
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


    public String formatObject(Object obj) throws Exception {
        int max = 75;
        String val = obj.toString();
        val = (val.length() > max) ? val.substring(0, max - 3) + "..." : val;
        char[] chars = new char[val.length()];
        val.getChars(0, chars.length, chars, 0);

        StringBuffer sb = new StringBuffer(chars.length);
        for (int j = 0; j < chars.length; j++) {
            char c = chars[j];
            switch (c) {
                case'<':
                    sb.append("&lt;");
                    break;
                case'>':
                    sb.append("&gt;");
                    break;
                case'&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /*-----------------------------------------------------------*/
    // Method name formatting
    /*-----------------------------------------------------------*/
    public String formatMethod(Method m) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append(getShortClassName(m.getReturnType())).append("&nbsp;&nbsp;");
        sb.append(m.getName());

        Class[] params = m.getParameterTypes();
        sb.append("(");
        for (int j = 0; j < params.length; j++) {
            sb.append(getShortClassName(params[j]));
            if (j != params.length - 1) {
                sb.append(",&nbsp;");
            }
        }
        sb.append(")");

        Class[] excp = m.getExceptionTypes();
        if (excp.length > 0) {
            sb.append(" throws&nbsp;");
            for (int j = 0; j < excp.length; j++) {
                sb.append(getShortClassName(excp[j]));
                if (j != excp.length - 1) {
                    sb.append(",&nbsp;");
                }
            }
        }
        return sb.toString();
    }

    /*-----------------------------------------------------------*/
    // Class name formatting
    /*-----------------------------------------------------------*/
    public String getShortClassName(Class clazz) throws Exception {
        if (clazz.isPrimitive()) {
            return clazz.getName();
        } else if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
            return clazz.getComponentType() + "[]";
        } else if (clazz.isArray()) {
            String name = clazz.getComponentType().getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return shortName + "[]";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return shortName;
        }
    }

    public String getShortClassRef(Class clazz) throws Exception {
        if (clazz.isPrimitive()) {
            return "<font color='gray'>" + clazz.getName() + "</font>";
        } else if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
            return "<font color='gray'>" + clazz.getComponentType() + "[]</font>";
        } else if (clazz.isArray()) {
            String name = clazz.getComponentType().getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='viewclass.jsp?class=" + name + "'>" + shortName + "[]</a>";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='viewclass.jsp?class=" + name + "'>" + shortName + "</a>";
        }
    }

    protected void printRow(String col1, String col2) throws Exception {
        out.print("<tr><td><font size='2'>");
        out.print(col1);
        out.print("</font></td><td><font size='2'>");
        out.print(col2);
        out.print("</font></td></tr>");
    }

    /*-----------------------------------------------------------*/
    // Object list support
    /*-----------------------------------------------------------*/
    public String getObjectID(Object obj) {
        Class clazz = obj.getClass();
        if (obj instanceof javax.ejb.EJBHome) {
            clazz = obj.getClass().getInterfaces()[0];
        } else if (obj instanceof javax.ejb.EJBObject) {
            clazz = obj.getClass().getInterfaces()[0];
        }
        return clazz.getName() + "@" + obj.hashCode();
    }

    public Object getObject(String objID) {
        return getObjectMap().get(objID);
    }

    public void setObject(String objID, Object obj) {
        getObjectMap().put(objID, obj);
    }

    public void removeObject(String objID) {
        getObjectMap().remove(objID);
    }

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> getObjectMap() {
        Map<String, Object> objects = (Map<String, Object>) session.getAttribute("objects");
        return objects;
    }

    /*-----------------------------------------------------------*/
    // Invocation list support
    /*-----------------------------------------------------------*/
    public Invocation getInvocation(String invID) {
        return getInvocationMap().get(invID);
    }

    public void setInvocation(String invID, Invocation obj) {
        getInvocationMap().put(invID, obj);
    }

    @SuppressWarnings({"unchecked"})
    public Map<String, Invocation> getInvocationMap() {
        Map<String, Invocation> invocations = (Map<String, Invocation>) session.getAttribute("invocations");
        if (invocations == null) {
            invocations = new HashMap<String, Invocation>();
            session.setAttribute("invocations", invocations);
        }
        return invocations;
    }

    /*-----------------------------------------------------------*/
    // String conversion support
    /*-----------------------------------------------------------*/
    final Map<Class, Converter> converters = initConverters();

    public Converter getConverter(Class type) {
        Converter con = converters.get(type);
        if (con == null) {
            con = defaultConverter;
        }
        return con;
    }

    final Converter defaultConverter = new ObjectConverter();

    private Map<Class, Converter> initConverters() {
        Map<Class, Converter> map = new HashMap<Class, Converter>();

        map.put(String.class, new StringConverter());
        map.put(Character.class, new CharacterConverter());
        map.put(Boolean.class, new BooleanConverter());
        map.put(Byte.class, new ByteConverter());
        map.put(Short.class, new ShortConverter());
        map.put(Integer.class, new IntegerConverter());
        map.put(Long.class, new LongConverter());
        map.put(Float.class, new FloatConverter());
        map.put(Double.class, new DoubleConverter());
        map.put(Object.class, new ObjectConverter());
        map.put(Character.TYPE, map.get(Character.class));
        map.put(Boolean.TYPE, map.get(Boolean.class));
        map.put(Byte.TYPE, map.get(Byte.class));
        map.put(Short.TYPE, map.get(Short.class));
        map.put(Integer.TYPE, map.get(Integer.class));
        map.put(Long.TYPE, map.get(Long.class));
        map.put(Float.TYPE, map.get(Float.class));
        map.put(Double.TYPE, map.get(Double.class));

        return map;
    }


    abstract class Converter {
        public abstract Object convert(Class type, String raw) throws Exception;

        public String getInputControl(int argNumber, Class type) throws Exception {
            return "<INPUT type='text' NAME='arg" + argNumber + "'>";
        }
    }

    class StringConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return raw;
        }
    }

    class CharacterConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Character(raw.charAt(0));
        }
    }

    class BooleanConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Boolean(raw);
        }
    }

    class ByteConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Byte(raw);
        }
    }

    class ShortConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Short(raw);
        }
    }

    class IntegerConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Integer(raw);
        }
    }

    class LongConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Long(raw);
        }
    }

    class FloatConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Float(raw);
        }
    }

    class DoubleConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return new Double(raw);
        }
    }

    class ObjectConverter extends Converter {
        public Object convert(Class type, String raw) throws Exception {
            return raw;
        }
    }
%>

