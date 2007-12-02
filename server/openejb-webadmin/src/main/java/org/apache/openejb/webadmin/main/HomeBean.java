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
package org.apache.openejb.webadmin.main;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;

import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

/** This class represents the "Home" page for the webadmin.  It contains general
 * information, and more content to be added later.
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
@Stateless(name = "Webadmin/Home")
@RemoteHome(HttpHome.class)
public class HomeBean extends WebAdminBean {

    /** Creates a new instance of HomeBean */
    public void ejbCreate() {
        this.section = "Home";
    }

    /** after the processing is completed
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

    /** before the process is done
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {}

    /** Write the main content
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeBody(PrintWriter body) throws IOException {
        body.println(
            "Welcome to the OpenEJB Web Administration website.  This website is designed to help automate");
        body.println(
            "many of the command line processes in OpenEJB.  Please begin by selecting from one of the menu");
        body.println("options.<br><br>");
        body.println(
            "We encourage our users to participate in giving suggestions and submitting code and documentation");
        body.println(
            "for the improvement of OpenEJB.  Because it's open source, it's not just our project, it's everyone's");
        body.println(
            "project!  <b>Your feedback and contributions make OpenEJB a better project for everyone!</b>  ");
        body.println("Future revisions of the OpenEJB Web Administration will contain:");
        body.println("<ul type=\"disc\">");
        body.println("<li>Better bean deployment</li>");
        body.println("<li>Container Managed Persistance Mapping</li>");
        body.println("<li>EJB Jar Validator</li>");
        body.println("<li>More system information</li>");
        body.println("<li>Better menu orginization</li>");
        body.println("<li>Addition of an extensive help section and documentation</li>");
        body.println("<li>Your suggestions!!</li>");
        body.println("</ul>");
        body.println("<br>");
        body.println(
            "If you have any problems with this website, please don�t hesitate to email the OpenEJB users list: ");
        body.println(
            "<a href=\"mailto:user@openejb.org\">user@openejb.org</a> and we�ll");
        body.println("respond to you as soon as possible.");
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
        body.println("Web Administration Home");
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
