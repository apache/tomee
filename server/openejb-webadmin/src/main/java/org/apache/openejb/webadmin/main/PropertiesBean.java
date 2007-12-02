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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;

import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

/** Prints out a list of system properties for the server.
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
@Stateless(name = "Webadmin/Properties")
@RemoteHome(HttpHome.class)
public class PropertiesBean extends WebAdminBean {

    /**
     * Called after a new instance of PropertiesBean is created
     */
    public void ejbCreate() {
        // The section variable must match 
        // the deployment id name
        section = "Properties";
    }

    /** called after all content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {
    }

    /** called before any content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {
    }

    /** writes the main body content to the broswer.  This content is inside a <code>&lt;p&gt;</code> block
     *  
     * 
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writeBody(PrintWriter body) throws IOException {
        Properties p = System.getProperties();
        Enumeration e = p.keys();
        String[] propertyList = new String[p.size()];

        body.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\">");
        String currentProperty = null;
        body.println("<tr><th align=\"left\">Property Name</th><th align=\"left\">Property Value</th></tr>");
        int j = 0;
        while ( e.hasMoreElements() ) {
            propertyList[j++] = (String) e.nextElement();
        }
        Arrays.sort(propertyList);

        String[] color = new String[]{"c9c5fe", "FFFFFF"};
        for ( int i=0; i<propertyList.length; i++ ) {
            String name  = propertyList[i];
            String value = System.getProperty(propertyList[i]);

            body.println("<tr bgcolor=\"#"+ color[i%2] +"\"  >");
            body.println("<td valign=\"top\">" + name + "</td>");
            body.println("<td>");
            if (propertyList[i].endsWith(".path")) {
                StringTokenizer path = new StringTokenizer(value,File.pathSeparator);
                while (path.hasMoreTokens()) {
                    body.print(path.nextToken());
                    body.println("<br>");
                }
            } else {
                body.println(value);
            }
            body.println("&nbsp;</td>");
            body.println("</tr>");
        }
        body.println("</table>");
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
        body.print(HTML_TITLE);
    }

    /** Write the title of the page.  This is displayed right
     * above the main block of content.
     * 
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writePageTitle(PrintWriter body) throws IOException {
        body.print("System Properties");
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
    public void writeSubMenuItems(PrintWriter body) throws IOException {
    }
}
