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
 * $Id: ViewClassBean.java 445460 2005-06-16 22:29:56Z jlaskowski $
 */
package org.apache.openejb.webadmin.clienttools;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;

import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
@Stateless(name = "ClientTools/ViewClass")
@RemoteHome(HttpHome.class)
public class ViewClassBean extends WebAdminBean  implements Constants {

    boolean hasMethods;

    public void preProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void postProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void writeHtmlTitle(PrintWriter out) throws IOException {
        out.write("Client Tools -- JNDI Viewer");
    }

    public void writePageTitle(PrintWriter out) throws IOException {
        out.write("JNDI Viewer");
    }

    public void writeBody(PrintWriter out) throws IOException {
        try {
            String className = request.getQueryParameter("class");
            if (className == null) {
                out.print("<b>Enter a class name to browse:</b>");
                out.print(
                    "<FORM NAME='view' METHOD='GET' ACTION='"+VIEW_CLASS+"'>");
                out.print(
                    "<INPUT type='text' NAME='class' size='40' VALUE=''>");
                out.print("<INPUT type='SUBMIT' NAME='view' value='View'>");
                out.print("</form>");
                out.print("<b>Or browse one of these fun classes:</b><br><br>");
                out.print(tab + getClassRef("javax.ejb.EJBHome") + "<br>");
                out.print(tab + getClassRef("javax.ejb.EJBObject") + "<br>");
                out.print(
                    tab + getClassRef("javax.ejb.EnterpriseBean") + "<br>");
                out.print(tab + getClassRef("javax.ejb.SessionBean") + "<br>");
                out.print(tab + getClassRef("javax.ejb.EntityBean") + "<br>");
                out.print(
                    tab
                        + getClassRef("javax.servlet.http.HttpServlet")
                        + "<br>");
                out.print(
                    tab
                        + getClassRef("javax.servlet.http.HttpServletRequest")
                        + "<br>");
                out.print(
                    tab
                        + getClassRef("javax.servlet.http.HttpServletResponse")
                        + "<br>");
                out.print(
                    tab
                        + getClassRef("javax.servlet.http.HttpSession")
                        + "<br>");
                out.print(
                    tab + getClassRef("javax.naming.InitialContext") + "<br>");
                out.print(tab + getClassRef("javax.naming.Context") + "<br>");

            } else {
                Class clazz = this.getClass().forName(className);
                printClass(clazz, out);
            }
        } catch (Exception e) {
            out.println("FAIL");
            return;
        }
        out.print("<BR><BR>");
        if (hasMethods) {
            out.print("<font color='green'>*</font>&nbsp;Public &nbsp; ");
            out.print("<font color='red'>*</font>&nbsp;Private &nbsp;");
            out.print("<font color='blue'>*</font>&nbsp;Protected &nbsp;");
            out.print("<font color='yellow'>*</font>&nbsp;Default ");
            out.print("<BR>");
        }
    }


    public void printClass(Class clazz, PrintWriter out)
        throws Exception {
        out.print("<b>" + clazz.getName() + "</b><br>");
        Method[] methods = clazz.getDeclaredMethods();
        hasMethods = (methods.length > 0);
        for (int i = 0; i < methods.length; i++) {
            printMethod(methods[i], out);
        }

        /*
		 * //out.print("&nbsp;&nbsp; <font color='gray'><u> Public Methods:
		 * </u></font><br> "); for (int i=0; i < methods.length; i++){ if
		 * (Modifier.isPublic(methods[i].getModifiers())){ printMethod(
		 * methods[i], out ); } } //out.print("&nbsp;&nbsp; <font color='gray'>
		 * <u> Private Methods: </u></font><br> "); for (int i=0; i
		 * < methods.length; i++){ if
		 * (Modifier.isPrivate(methods[i].getModifiers())){ printMethod(
		 * methods[i], out ); } } for (int i=0; i < methods.length; i++){ if
		 * (Modifier.isProtected(methods[i].getModifiers())){ printMethod(
		 * methods[i], out ); } } for (int i=0; i < methods.length; i++){ if
		 * (!Modifier.isSrict(methods[i].getModifiers())){ printMethod(
		 * methods[i], out ); } }
		 */
        Class sup = clazz.getSuperclass();
        if (sup != null) {
            out.print("<br><b>Extends:</b><br>");
            out.print(tab + getClassRef(sup) + "<br>");
        }

        Class[] intf = clazz.getInterfaces();

        if (intf.length > 0) {
            out.print("<br><b>Implements:</b><br>");
            for (int i = 0; i < intf.length; i++) {
                out.print(tab + getClassRef(intf[i]) + "<br>");
            }
        }
    }

    public void printMethod(Method m, PrintWriter out)
        throws Exception {
        out.print(tab);
        out.print(" " + getModifier(m));

        out.print(" " + getShortClassRef(m.getReturnType()) + "&nbsp;&nbsp;");

        out.print("" + m.getName() + "&nbsp;");
        Class[] params = m.getParameterTypes();
        out.print("<font color='gray'>(</font>");
        for (int j = 0; j < params.length; j++) {
            out.print(getShortClassRef(params[j]));
            if (j != params.length - 1) {
                out.print(",&nbsp;");
            }
        }
        out.print("<font color='gray'>)</font>");

        Class[] excp = m.getExceptionTypes();
        if (excp.length > 0) {
            out.print(" <font color='gray'>throws</font>&nbsp;&nbsp;");
            for (int j = 0; j < excp.length; j++) {
                out.print(getShortClassRef(excp[j]));
                if (j != excp.length - 1) {
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
        return "<font color='" + color + "'>*</font>";
    }

    public String getClassRef(Class clazz) throws Exception {
        String name = clazz.getName();
        return "<a href='"+VIEW_CLASS+"?class=" + name + "'>" + name + "</a>";
    }

    public String getClassRef(String name) throws Exception {
        return "<a href='"+VIEW_CLASS+"?class=" + name + "'>" + name + "</a>";
    }

    public String getShortClassRef(Class clazz) throws Exception {
        if (clazz.isPrimitive()) {
            return "<font color='gray'>" + clazz.getName() + "</font>";
        } else if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
            return "<font color='gray'>"
                + clazz.getComponentType()
                + "[]</font>";
        } else if (clazz.isArray()) {
            String name = clazz.getComponentType().getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='"+VIEW_CLASS+"?class="
                + name
                + "'>"
                + shortName
                + "[]</a>";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='"+VIEW_CLASS+"?class="
                + name
                + "'>"
                + shortName
                + "</a>";
        }
    }
}
