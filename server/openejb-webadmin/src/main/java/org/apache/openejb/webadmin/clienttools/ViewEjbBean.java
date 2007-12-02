/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.webadmin.clienttools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.HttpSession;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.BeanType;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
@Stateless(name = "ClientTools/ViewEjb")
@RemoteHome(HttpHome.class)
public class ViewEjbBean extends WebAdminBean implements Constants {

    public void preProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void postProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void writeHtmlTitle(PrintWriter out) throws IOException {
        out.write("Client Tools -- EJB Viewer");
    }

    public void writePageTitle(PrintWriter out) throws IOException {
        out.write("EJB Viewer");
    }

    public void writeBody(PrintWriter out) throws IOException {
        try {
            String ejb = request.getQueryParameter("ejb");
            if (ejb == null) {
                OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
                for (AppInfo appInfo : configuration.containerSystem.applications) {
                    for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                        for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                            out.print("<a href='"+VIEW_EJB+"?ejb="+bean.ejbDeploymentId+"'>"+ejbImg+"&nbsp;&nbsp;"+bean.ejbDeploymentId+"</a><br>");
                        }
                    }
                }
            } else {
                printEjb(ejb, out, request.getSession());
            }
        } catch (Exception e) {

            out.println("FAIL: ");
            out.print(e.getMessage());
            //            throw e;
            //return;
        }
    }

    public void printEjb(String name, PrintWriter out, HttpSession session)
        throws Exception {

        SystemInstance system = SystemInstance.get();
        ContainerSystem containerSystem = system.getComponent(ContainerSystem.class);

        String id = (name.startsWith("/")) ? name.substring(1, name.length()) : name;

        org.apache.openejb.DeploymentInfo ejb = containerSystem.getDeploymentInfo(id);


        if (ejb == null) {
            out.print("No such EJB: " + id);
            return;
        }
        String type = null;

        switch (ejb.getComponentType()) {
            case CMP_ENTITY :
                type = "EntityBean with Container-Managed Persistence";
                break;
            case BMP_ENTITY :
                type = "EntityBean with Bean-Managed Persistence";
                break;
            case STATEFUL :
                type = "Stateful SessionBean";
                break;
            case STATELESS :
                type = "Stateless SessionBean";
                break;
            default :
                type = "Unkown Bean Type";
                break;
        }
        out.print("<b>" + type + "</b><br>");
        out.print("<table>");
        printRow("JNDI Name", name, out);


        boolean hasLocal = ejb.getLocalInterface() != null;
        boolean hasRemote = ejb.getRemoteInterface() != null;

        String remoteInterfaceClassRef;
        String homeInterfaceClassRef;
        if (hasRemote){
            remoteInterfaceClassRef = getClassRef(ejb.getRemoteInterface());
            homeInterfaceClassRef = getClassRef(ejb.getHomeInterface());
        } else {
            remoteInterfaceClassRef = "none";
            homeInterfaceClassRef = "none";
        }

        printRow("Remote Interface",remoteInterfaceClassRef,out);
        printRow("Home Interface", homeInterfaceClassRef, out);

        if (hasLocal){
            String clzz = getClassRef(ejb.getLocalInterface());
            printRow("Local Interface",clzz,out);
            clzz = getClassRef(ejb.getLocalHomeInterface());
            printRow("LocalHome Interface",clzz,out);
        }

        printRow("Bean Class", getClassRef(ejb.getBeanClass()), out);

        if (ejb.getComponentType() == BeanType.BMP_ENTITY
            || ejb.getComponentType() == BeanType.CMP_ENTITY) {
            printRow("Primary Key", getClassRef(ejb.getPrimaryKeyClass()), out);
        }

        out.print("</table>");
        out.print("<br><br><b>Actions:</b><br>");
        out.print("<table>");

        // Browse JNDI with this ejb
        //javax.servlet.http.HttpSession session = this.session;
        HashMap objects = (HashMap) session.getAttribute("objects");
        if (objects == null) {
            objects = new HashMap();
            session.setAttribute("objects", objects);
        }

        InitialContext ctx;
        Properties p = new Properties();

        p.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.openejb.client.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");

        ctx = new InitialContext(p);


        if (hasRemote){
            Object obj = ctx.lookup(name);
            String objID = ejb.getHomeInterface().getName() + "@" + obj.hashCode();
            objects.put(objID, obj);
            String invokerURL =
                "<a href='"
                    + INVOKE_OBJ
                    + "?obj="
                    + objID
                    + "'>Invoke this EJB's home interface</a>";
            printRow(pepperImg, invokerURL, out);
        }
        if (hasLocal){
            Object obj = ctx.lookup(name+"Local");
            String objID = ejb.getLocalHomeInterface().getName() + "@" + obj.hashCode();
            objects.put(objID, obj);
            String invokerURL =
                "<a href='"
                    + INVOKE_OBJ
                    + "?obj="
                    + objID
                    + "'>Invoke this EJB's local home interface</a>";
            printRow(pepperImg, invokerURL, out);
        }

        Context enc = ((org.apache.openejb.core.CoreDeploymentInfo) ejb).getJndiEnc();
        String ctxID = "enc" + enc.hashCode();
        session.setAttribute(ctxID, enc);
        String jndiURL =
            "<a href='"
                + VIEW_JNDI
                + "?ctx="
                + ctxID
                + "'>Browse this EJB's private JNDI namespace</a>";
        printRow(pepperImg, jndiURL, out);
        out.print("</table>");

    }

    protected void printRow(String col1, String col2, PrintWriter out)
        throws IOException {
        out.print("<tr><td><font size='2'>");
        out.print(col1);
        out.print("</font></td><td><font size='2'>");
        out.print(col2);
        out.print("</font></td></tr>");
    }

    public String getClassRef(Class clazz) throws Exception {
        String name = clazz.getName();
        return "<a href='"
            + VIEW_CLASS
            + "?class="
            + name
            + "'>"
            + name
            + "</a>";
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
            return "<a href='"
                + VIEW_CLASS
                + "?class="
                + name
                + "'>"
                + shortName
                + "[]</a>";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='"
                + VIEW_CLASS
                + "?class="
                + name
                + "'>"
                + shortName
                + "</a>";
        }
    }
}
