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
 * $Id: DeploymentListBean.java 445460 2005-06-16 22:29:56Z jlaskowski $
 */
package org.apache.openejb.webadmin.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.util.StringUtilities;

import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

/**
 * A bean which lists all deployed beans.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
@Stateless(name = "Webadmin/DeploymentList")
@RemoteHome(HttpHome.class)
public class DeploymentListBean extends WebAdminBean {
    private HashMap deploymentIdIndex;
    private HashMap containerIdIndex;

    /** Creates a new instance of DeploymentListBean */
    public void ejbCreate() {
        this.section = "DeploymentList";
    }

    /** called after all content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

    /** called before any content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {
        createIndexes();
    }

    /** writes the main body content to the broswer.  This content is inside a <code>&lt;p&gt;</code> block
     *
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeBody(PrintWriter body) throws IOException {
        String id = request.getQueryParameter("id");
        if (id != null) {
            showDeployment(body, id);
        } else {
            printDeployments(body);
        }
    }

    private void createIndexes() {
        deploymentIdIndex = new HashMap();
        containerIdIndex = new HashMap();
        OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        for (AppInfo appInfo : configuration.containerSystem.applications) {
            for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                for (EnterpriseBeanInfo bean : ejbJarInfo.enterpriseBeans) {
                    deploymentIdIndex.put(bean.ejbDeploymentId, bean);
                }
            }
        }
    }

    private void showDeployment(PrintWriter body, String id) throws IOException {
        EnterpriseBeanInfo bean = getBeanInfo(id);

        // TODO:0: Inform the user the id is bad
        if (bean == null)
            return;

        // Assuming things are good        
        body = response.getPrintWriter();

        body.println("<h2>General</h2><br>");
        body.println("<table width=\"100%\" border=\"1\">");
        body.println("<tr bgcolor=\"#5A5CB8\">");
        body.println("<td><font face=\"arial\" color=\"white\">ID</font></td>");
        body.println("<td><font color=\"white\">" + id + "</font></td>");
        body.println("</tr>");

        SystemInstance system = SystemInstance.get();
        ContainerSystem containerSystem = system.getComponent(ContainerSystem.class);

        CoreDeploymentInfo di = (CoreDeploymentInfo) containerSystem.getDeploymentInfo(id);

        printRow("Name", bean.ejbName, body);
        printRow(
            "Description",
            StringUtilities.replaceNullOrBlankStringWithNonBreakingSpace(bean.description),
            body);

        String type = null;

        switch (di.getComponentType()) {
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

        printRow("Bean Type", type, body);
        printRow("Bean Class", bean.ejbClass, body);
        printRow("Home Interface", bean.home, body);
        printRow("Remote Interface", bean.remote, body);
        printRow("Jar location", bean.codebase, body);

        //String container = URLEncoder.encode("" + di.getContainer().getContainerID());
        String container = (String)di.getContainer().getContainerID();
        printRow("Deployed in", container, body);

        body.println("</table>");

        JndiEncInfo enc = bean.jndiEnc;

        body.println("<h2>JNDI Environment Details</h2><br>");
        body.println("<table width=\"100%\" border=\"1\">");
        body.println("<tr bgcolor=\"#5A5CB8\">");
        body.println("<td><font face=\"arial\" color=\"white\">JNDI Name</font></td>");
        body.println("<td><font face=\"arial\" color=\"white\">Value</font></td>");
        body.println("<td><font face=\"arial\" color=\"white\">Type</font></td>");
        body.println("</tr>");

        for (EnvEntryInfo info : enc.envEntries) {
            printRow(info.name, info.value, info.type, body);
        }

        for (EjbLocalReferenceInfo info : enc.ejbLocalReferences) {
            printRow(info.referenceName, info.ejbDeploymentId, info.homeType, body);
        }

        for (EjbReferenceInfo info : enc.ejbReferences) {
            printRow(info.referenceName, info.ejbDeploymentId, info.homeType, body);
        }

        for (ResourceReferenceInfo info : enc.resourceRefs) {
            printRow(info.referenceName, info.resourceID, info.referenceType, body);
        }

        for (ResourceEnvReferenceInfo info : enc.resourceEnvRefs) {
            printRow(info.resourceEnvRefName, info.resourceID, info.resourceEnvRefType, body);
        }

        for (PersistenceUnitReferenceInfo info : enc.persistenceUnitRefs) {
            printRow(info.referenceName, info.persistenceUnitName, "", body);
        }

        for (PersistenceContextReferenceInfo info : enc.persistenceContextRefs) {
            printRow(info.referenceName, info.persistenceUnitName, "", body);
        }

        for (ServiceReferenceInfo info : enc.serviceRefs) {
            printRow(info.referenceName, "<not-supported>", "", body);
        }

        body.println("</table>");
    }

    private EnterpriseBeanInfo getBeanInfo(String id) {
        return (EnterpriseBeanInfo) deploymentIdIndex.get(id);
    }

    private void printDeployments(PrintWriter out) throws IOException {
        SystemInstance system = SystemInstance.get();
        ContainerSystem containerSystem = system.getComponent(ContainerSystem.class);

        DeploymentInfo[] deployments = containerSystem.deployments();
        String[] deploymentString = new String[deployments.length];
        out.println("<table width=\"100%\" border=\"1\">");
        out.println("<tr bgcolor=\"#5A5CB8\">");
        out.println("<td><font color=\"white\">Deployment ID</font></td>");
        out.println("</tr>");

        if (deployments.length > 0) {
            for (int i = 0; i < deployments.length; i++) {
                deploymentString[i] = (String) deployments[i].getDeploymentID();
            }
            Arrays.sort(deploymentString);

            for (int i = 0; i < deploymentString.length; i++) {
                if (i % 2 == 1) {
                    out.println("<tr bgcolor=\"#c9c5fe\">");
                } else {
                    out.println("<tr>");
                }

                out.print("<td><span class=\"bodyBlack\">");
                out.print("<a href=\"DeploymentList?id=" + deploymentString[i] + "\">");
                out.print(deploymentString[i]);
                out.print("</a>");
                out.println("</span></td></tr>");
            }
        }
        out.println("</table>");
    }

    /** Write the TITLE of the HTML document.  This is the part
     * that goes into the <code>&lt;head&gt;&lt;title&gt;
     * &lt;/title&gt;&lt;/head&gt;</code> tags
     *
     * @param body the output to write to
     * @exception IOException of an exception is thrown
     *
     */
    public void writeHtmlTitle(PrintWriter body) throws IOException {
        body.println(HTML_TITLE);
    }

    /** Write the title of the page.  This is displayed right
     * above the main block of content.
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writePageTitle(PrintWriter body) throws IOException {
        body.println("EnterpriseBean Details");
    }

    /** Write the sub items for this bean in the left navigation bar of
     * the page.  This should look somthing like the one below:
     *
     *      <code>
     *      &lt;tr&gt;
     *       &lt;td valign="top" align="left"&gt;
     *        &lt;a href="system?show=deployments"&gt;&lt;span class="subMenuOff"&gt;
     *        &nbsp;&nbsp;&nbsp;Deployments
     *        &lt;/span&gt;
     *        &lt;/a&gt;&lt;/td&gt;
     *      &lt;/tr&gt;
     *      </code>
     *
     * Alternately, the bean can use the method formatSubMenuItem(..) which
     * will create HTML like the one above
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeSubMenuItems(PrintWriter body) throws IOException {}

}
