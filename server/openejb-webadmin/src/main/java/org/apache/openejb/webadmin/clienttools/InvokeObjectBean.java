/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: InvokeObjectBean.java 445460 2005-06-16 22:29:56Z jlaskowski $
 */
package org.apache.openejb.webadmin.clienttools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.HttpSession;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;

import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
@Stateless(name = "ClientTools/ViewJndi")
@RemoteHome(HttpHome.class)
public class InvokeObjectBean extends WebAdminBean implements Constants {

    private PrintWriter out;
    private static String invLock = "lock";
    private static int invCount;

    private HttpSession session;

    public void preProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void postProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void writeHtmlTitle(PrintWriter out) throws IOException {
        out.write("Client Tools -- Object Invoker");
    }

    public void writePageTitle(PrintWriter out) throws IOException {
        out.write("Object Invoker");
    }

    public void writeBody(PrintWriter out) throws IOException {
        this.out = out;
        try{
            synchronized (this) {
                main(request.getSession(), out);
            }
        } catch (Exception e){
            out.println("FAIL");
            //throw e;
            return;
        }
    }

    class Invocation {

        protected String id = "inv";
        protected String objID;
        protected Class clazz;
        protected Object target;
        protected Method method;
        protected Object[] args;
        protected Object result;

        protected Invocation(){
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
    public void main(HttpSession session, PrintWriter out) throws Exception{
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
        String removeID = request.getQueryParameter("remove");
        if (removeID != null) {
            removeObject(removeID);
        }

        Invocation inv = null;
        String invID = request.getQueryParameter("inv");

        if (invID == null) {
            String objID = request.getQueryParameter("obj");
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
            out.print(tab+inv.objID+" <a href='"+INVOKE_OBJ+"'>[change]</a><br><br>");

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
            printRow(pepperImg,"<A HREF='"+VIEW_JNDI+"'>Browse for an EJB</A>");
            out.print("</table>");

        } else {
            out.print("<b>Pick and object to invoke</b><br>");

            //out.print("<b>Objects:</b><br>");
            Set keys = objects.keySet();
            Iterator iterator = keys.iterator();
            out.print("<table>");
            while (iterator.hasNext()) {
                String entry = (String)iterator.next();
                printRow(tab+"<a href='"+INVOKE_OBJ+"?obj="+entry+"'>"+entry+"</a>",
                        "<a href='"+INVOKE_OBJ+"?remove="+entry+"'>[remove]</a>");
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
        String methodID = request.getQueryParameter("m");

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
            out.print(tab+formatMethod(inv.method)+" <a href='"+INVOKE_OBJ+"?m=-1&inv="+inv.id+"'>[change]</a><br><br>");

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
                out.print(tab+"<a href='"+INVOKE_OBJ+"?inv="+inv.id+"&m="+i+"'>"+formatMethod(m)+"</a><br>");
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
        String args = request.getQueryParameter("args");

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
            String unparsedArg = request.getQueryParameter("arg"+i);
            inv.args[i] = getConverter(type).convert(type, unparsedArg);
        }
    }

    public void printArgumentList(Invocation inv) throws Exception{
        out.print("<b>Fill in the arguments</b><br>");
        Class[] pTypes = inv.method.getParameterTypes();
        out.print("<FORM NAME='args' METHOD='GET' ACTION='"+INVOKE_OBJ+"'>");
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
        String doInvoke = request.getQueryParameter("invoke");
        if (doInvoke != null) {
            invoke(inv);
        } else {
            out.print("<FORM NAME='invoke' METHOD='GET' ACTION='"+INVOKE_OBJ+"'>");
            out.print("<INPUT type='HIDDEN' NAME='inv' VALUE='"+inv.id+"'>");
            out.print("<INPUT type='SUBMIT' NAME='invoke' value='Invoke'>");
            out.print("</FORM>");
        }

    }
    String pepperImg = "<img src='/images/pepper.gif' border='0'>";
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
                printRow("<i>class</i>","<a href='"+VIEW_CLASS+"?class="+clazz+"'>"+clazz+"</a>");
                printRow("<i>toString</i>",formatObject(inv.result));
                out.print("</table>");

                out.print("<br><br><b>Actions:</b><br>");
                out.print("<table>");
                String invokerURL = "<a href='"+INVOKE_OBJ+"?obj="+objID+"'>Invoke a method on the object</a>";
                printRow(pepperImg,invokerURL);
                String discardURL = "<a href='"+INVOKE_OBJ+"?remove="+objID+"'>Discard the object</a>";
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
            return "<a href='"+VIEW_CLASS+"?class="+name+"'>"+shortName+"[]</a>";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".")+1;
            String shortName = name.substring(dot,name.length());
            return "<a href='"+VIEW_CLASS+"?class="+name+"'>"+shortName+"</a>";
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
        HttpSession session = request.getSession();
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
}
