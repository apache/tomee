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
package org.apache.openejb.tomcat;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.Loader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ejbd.EjbServer;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class TomcatLoader implements Loader {
    private EjbServer ejbServer;

    public void init(Properties props) throws Exception {
        // Not thread safe
        if (OpenEJB.isInitialized()) {
            ejbServer = SystemInstance.get().getComponent(EjbServer.class);
            return;
        }

        System.setProperty("openejb.provider.default", "org.apache.openejb.tomcat");
        
        ThreadContext.addThreadContextListener(new TomcatThreadContextListener());

        if (SystemInstance.get().getComponent(WebAppBuilder.class) == null) {
            TomcatWebAppBuilder tomcatWebAppBuilder = new TomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }

        SystemInstance.init(props);

        ejbServer = new EjbServer();
        SystemInstance.get().setComponent(EjbServer.class, ejbServer);
        OpenEJB.init(props, new ServerFederation());
        ejbServer.init(props);
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream in = request.getInputStream();
        ServletOutputStream out = response.getOutputStream();
        try {
            ejbServer.service(in, out);
        } catch (ServiceException e) {
            throw new ServletException("ServerService error: " + ejbServer.getClass().getName() + " -- " + e.getMessage(), e);
        }
    }
}
