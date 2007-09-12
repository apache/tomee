/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat.installer;

import static org.apache.openejb.tomcat.installer.Installer.Status.INSTALLED;
import static org.apache.openejb.tomcat.installer.Installer.Status.REBOOT_REQUIRED;
import static org.apache.openejb.tomcat.installer.Installer.Status.NONE;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;

/**
 * Installs OpenEJB into Tomcat.
 * <p/>
 * NOTE: This servlet can not use any classes from OpenEJB since it is installing OpenEJB itself.
 */
public class InstallerServlet extends HttpServlet {
    protected Paths paths;
    protected Installer installer;
    protected int attempts;

    public void init(ServletConfig servletConfig) throws ServletException {
        paths = new Paths(servletConfig.getServletContext());
        installer = new Installer(paths);
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doIt(httpServletRequest, httpServletResponse);
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doIt(httpServletRequest, httpServletResponse);
    }

    protected void doIt(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // if they clicked the install button...
        if ("install".equals(req.getParameter("action"))) {
            // If not already installed, try to install
            if (installer.getStatus() == NONE) {
                attempts++;
                paths.setCatalinaHomeDir(req.getParameter("catalinaHome"));
                paths.setCatalinaBaseDir(req.getParameter("catalinaBase"));
                paths.setServerXmlFile(req.getParameter("serverXml"));

                if (paths.verify()) {
                    installer.install();
                }
            }

            // send redirect to avoid double post lameness
            res.sendRedirect(req.getRequestURI());
        }

        res.setHeader("Pragma", "No-cache");
        res.setHeader("Cache-Control", "no-cache");
        res.setDateHeader("Expires", 1);
        res.setContentType("text/html");

        ServletOutputStream out = res.getOutputStream();
        out.println("<html>");
        out.println("<head>");
        out.println("<meta HTTP-EQUIV='Pragma' content='no-cache'>");
        out.println("<meta HTTP-EQUIV='Expires' content='-1'>");
        out.println("<title>OpenEJB Installer for Tomcat</title>");
        out.println("</head>");
        out.println("<body>");

        // Is OpenEJB already installed?
        if (installer.getStatus() == INSTALLED) {
            out.println("<h1>INSTALLATION SUCCESSFUL!</h1>");
        } else if (installer.getStatus() == REBOOT_REQUIRED) {
            out.println("<h1>Installation Complete.  REBOOT REQUIRED</h1>");

            if (installer.hasWarnings()) {
                out.println("<h3>Warnings:</h3>");
                for (String warning : installer.getWarnings()) {
                    out.println(warning + "<br>");
                }
            }
            if (installer.hasInfos()) {
                out.println("<h3>Info:</h3>");
                for (String info : installer.getInfos()) {
                    out.println(info + "<br>");
                }
            }
        } else {
            // Not installed

            // Did an installation fail?
            if (paths.hasErrors()) {
                out.println("<h1>Installation Failed</h1>");
                for (String error : paths.getErrors()) {
                    out.println(error + "<br>");
                }
            } else if (installer.hasErrors()) {
                out.println("<h1>Installation Failed</h1>");
                for (String error : installer.getErrors()) {
                    out.println(error + "<br>");
                }
            }

            // write the for either way
            writeInstallerForm(req, out);
        }

        out.println("</body>");
        out.println("</html>");
    }

    private void writeInstallerForm(HttpServletRequest req, ServletOutputStream out) throws IOException {
        // if we have tried once (and failed) add more text
        if (attempts > 0) {
            out.println("<h1>Try Again?</h1>");
        }

        out.println("<form action='" + req.getRequestURI() + "' method='post'>");

        out.println("Catalina Home:");
        out.println("<input type='text' size='100' name='catalinaHome' value='" + safeGetAbsolutePath(paths.getCatalinaHomeDir()) + "'>");
        out.println("<br>");

        out.println("Catalina Base:");
        out.println("<input type='text' size='100' name='catalinaBase' value='" + safeGetAbsolutePath(paths.getCatalinaBaseDir()) + "'>");
        out.println("<br>");

        out.println("Catalina server.xml:");
        out.println("<input type='text' size='100' name='serverXml' value='" + safeGetAbsolutePath(paths.getServerXmlFile()) + "'>");
        out.println("<br>");

        out.println("<input type='submit' name='action' value='install'>");
        
        out.println("</form>");

    }

    private String safeGetAbsolutePath(File file) {
        if (file == null) return "";
        return file.getAbsolutePath();
    }
}
