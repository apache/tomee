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
javax.servlet.jsp.JspWriter,
java.io.PrintWriter,
java.util.*,
java.io.*,
java.lang.reflect.Method,
java.lang.reflect.InvocationTargetException,
java.lang.reflect.Modifier
"%>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Integration/1.0</title>
    <link href="default.css" rel="stylesheet">
    <!-- $Id: invokeobj.jsp 445516 2005-07-04 08:10:54Z dblevins $ -->
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
                            OpenEJB Object Invoker
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

    class Invocation {
        
        String id = "inv";
        String objID;
        Class clazz;
        Object target;
        Method method;
        Object[] args;
        Object result;
        Invocation(){
            synchronized (invLock){
                id += ++invCount;
            }
        }

        public Object invoke() throws Exception{
            if (target == null || method == null || args == null) {
                throw new Exception("This invocation contains null objects.");
            }
            return method.invoke(target,args);
        }
    }
    
    /**
     * The main method of this JSP
     */ 
    public void main(HttpServletRequest request, HttpSession session, JspWriter out) throws Exception{
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
    public void printObjectSection() throws Exception{
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
                setInvocation(inv.id,inv);
            } 
        } else {
            inv = getInvocation(invID);
        }

        if (inv == null || inv.target == null) {
            // Pick from the list
            printObjectList();

        } else {
            out.print("<b>Object:</b><br>");
            out.print(tab+inv.objID+" <a href='invokeobj.jsp'>[change]</a><br><br>");
            
            // Show the selected item and continue
            printMethodSection(inv);
        }
    }

    /**
     * Prints the list of objects that can be invoked
     */ 
    public void printObjectList() throws Exception{
        
        HashMap objects = getObjectMap();
        if (objects.size() == 0){
            out.print("<b>No object have been created</b><br>");
            out.print("<table>");
            printRow(pepperImg,"<A HREF='viewjndi.jsp'>Browse for an EJB</A>");
            out.print("</table>");
            
        } else {
            out.print("<b>Pick and object to invoke</b><br>");

            //out.print("<b>Objects:</b><br>");
            Set keys = objects.keySet();
            Iterator iterator = keys.iterator();
            out.print("<table>");
            while (iterator.hasNext()) {
               String entry = (String)iterator.next();
               printRow(tab+"<a href='invokeobj.jsp?obj="+entry+"'>"+entry+"</a>",
                            "<a href='invokeobj.jsp?remove="+entry+"'>[remove]</a>");
            }
            out.print("</table>");
        }
    }
    /**
     * Print the list of methods with the focused method as
     * selected in the box.
     * If no method is selected, make an entry called "Pick a Method"
     */
    public void printMethodSection(Invocation inv) throws Exception{
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
            out.print("<b>Method:</b><br>");
            out.print(tab+formatMethod(inv.method)+" <a href='invokeobj.jsp?m=-1&inv="+inv.id+"'>[change]</a><br><br>");
            
            // Show the selected item and continue
            printArgumentSection(inv);
        }
        
    }
    
    /**
     * Prints the list of methods that can be invoked
     */ 
    public void printMethodList(Invocation inv) throws Exception{
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

        out.print("<table>");
        Method[] methods = clazz.getMethods();
        for (int i=0; i < methods.length; i++){
            Method m = methods[i];
            if (Modifier.isPublic(m.getModifiers())){
                out.print("<tr><td><font size='2'>");
                out.print(tab+"<a href='invokeobj.jsp?inv="+inv.id+"&m="+i+"'>"+formatMethod(m)+"</a><br>");
                out.print("</font></td></tr>");
            }
        }
        out.print("</table>");
    }

    /**
     * Print the list of arguments.
     * If no arguments have been selected, 
     * show the argument entry form.
     */
    public void printArgumentSection(Invocation inv) throws Exception{
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
            out.print("<b>Arguments:</b><br>");
            if (inv.args.length == 0) {
                out.print(tab+"none<br>");
            }
            for (int i=0; i < inv.args.length; i++){
                String val = formatObject(inv.args[i]);
                out.print(tab+"arg"+i+"&nbsp;&nbsp;<i>"+val+"</i><br>");
            }
            out.print("<br>");
            printInvokeSection(inv);
        }
    }
    
    public void parseArgs(Invocation inv) throws Exception{
        Class[] pTypes = inv.method.getParameterTypes();
        inv.args = new Object[pTypes.length];

        for (int i=0; i < pTypes.length; i++){
            Class type = pTypes[i];
            String unparsedArg = request.getParameter("arg"+i);
            inv.args[i] = getConverter(type).convert(type, unparsedArg);
        }
    }

    public void printArgumentList(Invocation inv) throws Exception{
        out.print("<b>Fill in the arguments</b><br>");
        Class[] pTypes = inv.method.getParameterTypes();
        out.print("<FORM NAME='args' METHOD='GET' ACTION='invokeobj.jsp'>");
        out.print("<INPUT type='HIDDEN' NAME='inv' VALUE='"+inv.id+"'>");
        out.print("<table>");
        for (int i=0; i < pTypes.length; i++){
            Converter con = getConverter(pTypes[i]);
            out.print("<tr>");
            out.print("<td align='right'><font size='2'>");
            out.print(tab+getShortClassRef(pTypes[i]));
            out.print("</font></td>");
            out.print("<td><font size='2'>");
            out.print("&nbsp;&nbsp;arg"+i);
            out.print("</font></td>");
            out.print("<td><font size='2'>");
            out.print("&nbsp;&nbsp;"+con.getInputControl(i,pTypes[i]));
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
    public void printInvokeSection(Invocation inv) throws Exception{
        String doInvoke = request.getParameter("invoke");
        if (doInvoke != null) {
            invoke(inv);
        } else {
            out.print("<FORM NAME='invoke' METHOD='GET' ACTION='invokeobj.jsp'>");
            out.print("<INPUT type='HIDDEN' NAME='inv' VALUE='"+inv.id+"'>");
            out.print("<INPUT type='SUBMIT' NAME='invoke' value='Invoke'>");
            out.print("</FORM>");
        }

    }
    String pepperImg = "<img src='images/pepper.gif' border='0'>";
    public void invoke(Invocation inv) throws Exception{

        try{
            inv.result = inv.invoke();

            out.print("<b>Result:</b><br>");
            if (inv.method.getReturnType() == java.lang.Void.TYPE) {
                out.print(tab+"Done");
            } else if (inv.result == null) {
                out.print(tab+"<i>null</i>");
            } else {
                String clazz = inv.result.getClass().getName();
                String objID = getObjectID(inv.result);
                setObject(objID,inv.result);

                out.print("<table>");
                printRow("<i>id</i>",objID);
                printRow("<i>class</i>","<a href='viewclass.jsp?class="+clazz+"'>"+clazz+"</a>");
                printRow("<i>toString</i>",formatObject(inv.result));
                out.print("</table>");

                out.print("<br><br><b>Actions:</b><br>");
                out.print("<table>");
                String invokerURL = "<a href='invokeobj.jsp?obj="+objID+"'>Invoke a method on the object</a>";
                printRow(pepperImg,invokerURL);
                String discardURL = "<a href='invokeobj.jsp?remove="+objID+"'>Discard the object</a>";
                printRow(pepperImg,discardURL);
                out.print("</table>");
            }
        } catch (InvocationTargetException e){
            out.print("<b>Exception:</b><br><br>");
            Throwable t = e.getTargetException();
            out.print("Received a "+t.getClass().getName());
            //out.print(inv.method+"<br><br>");
            if (t instanceof java.rmi.RemoteException) {
                out.print(" <a href='re-help.html'>[Tip]</a><br><br>");
                java.rmi.RemoteException re = (java.rmi.RemoteException)t;
                out.print("<i>RemoteException message:</i><br>");
                out.print(t.getMessage()+"<br><br>");
                out.print("<i>Nested exception's stack trace:</i><br>");
                
                while (t instanceof java.rmi.RemoteException) {
                    t = ((java.rmi.RemoteException)t).detail;
                }
                out.print(formatThrowable(t));
            } else {
                out.print("<br><br>"+formatThrowable(t));
            }

        } catch (Throwable e){
            out.print("<b>Exception:</b><br><br>");
            out.print(formatObject(e));
        }
    }

    public String formatThrowable(Throwable err) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        err.printStackTrace(new PrintStream(baos));
        byte[] bytes = baos.toByteArray();
        StringBuffer sb = new StringBuffer(bytes.length);
        for (int i=0; i < bytes.length; i++){
            char c = (char)bytes[i];
            switch (c) {
            case ' ': sb.append("&nbsp;"); break;
            case '\n': sb.append("<br>"); break;
            case '\r': break;
            default: sb.append(c);
            }
        }
        return sb.toString();
    }


    public String formatObject(Object obj) throws Exception{
        int max = 75;
        String val = obj.toString();
        val = (val.length() > max)? val.substring(0,max-3)+"...":val;
        char[] chars = new char[val.length()];
        val.getChars(0,chars.length,chars,0);

        StringBuffer sb = new StringBuffer(chars.length);
        for (int j=0; j < chars.length; j++){
            char c = chars[j];
            switch (c) {
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            case '&': sb.append("&amp;"); break;
            default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /*-----------------------------------------------------------*/
    // Method name formatting
    /*-----------------------------------------------------------*/
    public String formatMethod(Method m) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append(getShortClassName(m.getReturnType())+"&nbsp;&nbsp;");
        sb.append(m.getName());
        
        Class[] params = m.getParameterTypes();
        sb.append("(");
        for (int j=0; j < params.length; j++){
            sb.append(getShortClassName(params[j]));
            if (j != params.length-1) {
                sb.append(",&nbsp;");
            }
        }
        sb.append(")");

        Class[] excp = m.getExceptionTypes();
        if (excp.length > 0) {
            sb.append(" throws&nbsp;");
            for (int j=0; j < excp.length; j++){
                sb.append(getShortClassName(excp[j]));
                if (j != excp.length-1) {
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
            return clazz.getComponentType()+"[]";
        } else if (clazz.isArray()) {
            String name = clazz.getComponentType().getName();
            int dot = name.lastIndexOf(".")+1;
            String shortName = name.substring(dot,name.length());
            return shortName+"[]";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".")+1;
            String shortName = name.substring(dot,name.length());
            return shortName;
        }
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

    protected void printRow(String col1, String col2) throws Exception{
        out.print("<tr><td><font size='2'>"  );
        out.print(col1);
        out.print("</font></td><td><font size='2'>");
        out.print(col2);
        out.print("</font></td></tr>");
    }

    /*-----------------------------------------------------------*/
    // Object list support
    /*-----------------------------------------------------------*/
    public String getObjectID(Object obj){
        Class clazz = obj.getClass();
        if (obj instanceof javax.ejb.EJBHome) {
            clazz = obj.getClass().getInterfaces()[0];
        } else if (obj instanceof javax.ejb.EJBObject) {
            clazz = obj.getClass().getInterfaces()[0];
        }
        return clazz.getName()+"@"+obj.hashCode();
    }

    public Object getObject(String objID){
        return getObjectMap().get(objID);
    }
    
    public void setObject(String objID, Object obj){
        getObjectMap().put(objID, obj);
    }

    public void removeObject(String objID){
        getObjectMap().remove(objID);
    }

    public HashMap getObjectMap(){
        HashMap objects = (HashMap)session.getAttribute("objects");
        if (objects == null) {
            objects = new HashMap();
            session.setAttribute("objects",objects);
        }
        return objects;
    }
    
    /*-----------------------------------------------------------*/
    // Invocation list support
    /*-----------------------------------------------------------*/
    public Invocation getInvocation(String invID) {
        return (Invocation)getInvocationMap().get(invID);
    }
    
    public void setInvocation(String invID, Invocation obj){
        getInvocationMap().put(invID, obj);
    }

    public HashMap getInvocationMap(){
        HashMap invocations = (HashMap)session.getAttribute("invocations");
        if (invocations == null) {
            invocations = new HashMap();
            session.setAttribute("invocations",invocations);
        }
        return invocations;
    }

    /*-----------------------------------------------------------*/
    // String conversion support
    /*-----------------------------------------------------------*/
    final HashMap converters = initConverters();

    public Converter getConverter(Class type){
        Converter con = (Converter) converters.get(type);
        if (con == null) {
            con = defaultConverter;
        }
        return con;
    }

    final Converter defaultConverter = new ObjectConverter();

    private HashMap initConverters(){
        HashMap map = new HashMap();

        map.put(String.class,    new StringConverter());
        map.put(Character.class, new CharacterConverter());
        map.put(Boolean.class,   new BooleanConverter());
        map.put(Byte.class,      new ByteConverter());
        map.put(Short.class,     new ShortConverter());
        map.put(Integer.class,   new IntegerConverter());
        map.put(Long.class,      new LongConverter());
        map.put(Float.class,     new FloatConverter());
        map.put(Double.class,    new DoubleConverter());
        map.put(Object.class,    new ObjectConverter());
        map.put(Character.TYPE,  map.get(Character.class));
        map.put(Boolean.TYPE,    map.get(Boolean.class));
        map.put(Byte.TYPE,       map.get(Byte.class));
        map.put(Short.TYPE,      map.get(Short.class));
        map.put(Integer.TYPE,    map.get(Integer.class));
        map.put(Long.TYPE,       map.get(Long.class));
        map.put(Float.TYPE,      map.get(Float.class));
        map.put(Double.TYPE,     map.get(Double.class));
        
        return map;
    }


    abstract class Converter {
        public abstract Object convert(Class type, String raw) throws Exception;
        public String getInputControl(int argNumber, Class type) throws Exception{
            return "<INPUT type='text' NAME='arg"+argNumber+"'>";
        }
    }
    
    class StringConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return raw;
        }
    }
    
    class CharacterConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Character(raw.charAt(0));
        }
    }
    
    class BooleanConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Boolean(raw);
        }
    }
    
    class ByteConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Byte(raw);
        }
    }
    
    class ShortConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Short(raw);
        }
    }
    
    class IntegerConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Integer(raw);
        }
    }
    
    class LongConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Long(raw);
        }
    }
    
    class FloatConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Float(raw);
        }
    }
    
    class DoubleConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return new Double(raw);
        }
    }
    
    class ObjectConverter extends Converter{
        public Object convert(Class type, String raw) throws Exception {
            return raw;
        }
    }
%>

