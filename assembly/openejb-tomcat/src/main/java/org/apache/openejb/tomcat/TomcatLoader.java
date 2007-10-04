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
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.Service;
import org.apache.catalina.Engine;
import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.ServerFactory;

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

        // initialize system instance before doing anything
        System.setProperty("openejb.provider.default", "org.apache.openejb.tomcat");
        SystemInstance.init(props);

        // Install tomcat thread context listener
        ThreadContext.addThreadContextListener(new TomcatThreadContextListener());

        // Install tomcat war builder
        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder == null) {
            tomcatWebAppBuilder = new TomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }

        // Start OpenEJB
        ejbServer = new EjbServer();
        SystemInstance.get().setComponent(EjbServer.class, ejbServer);
        OpenEJB.init(props, new ServerFederation());
        ejbServer.init(props);

        // Add our naming context listener to the server which registers all Tomcat resources with OpenEJB
        StandardServer standardServer = (StandardServer) ServerFactory.getServer();
        OpenEJBNamingContextListener namingContextListener = new OpenEJBNamingContextListener(standardServer);
        // Standard server has no state property, so we check global naming context to determine if server is started yet
        if (standardServer.getGlobalNamingContext() != null) {
            namingContextListener.start();
        }
        standardServer.addLifecycleListener(namingContextListener);

        // Process all applications already started.  This deploys EJBs, PersistenceUnits
        // and modifies JNDI ENC references to OpenEJB managed objects such as EJBs.
        processRunningApplications(tomcatWebAppBuilder, standardServer);
    }

    private void processRunningApplications(TomcatWebAppBuilder tomcatWebAppBuilder, StandardServer standardServer) {
        for (Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                for (Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof Host) {
                        Host host = (Host) engineChild;
                        for (Container hostChild : host.findChildren()) {
                            if (hostChild instanceof StandardContext) {
                                StandardContext standardContext = (StandardContext) hostChild;
                                int state = standardContext.getState();
                                if (state == 0) {
                                    // context only initialized
                                    tomcatWebAppBuilder.init(standardContext);
                                } else if (state == 1) {
                                    // context started
                                    standardContext.addParameter("openejb.start.late", "true");
                                    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
                                    Thread.currentThread().setContextClassLoader(standardContext.getLoader().getClassLoader());
                                    try {
                                        tomcatWebAppBuilder.init(standardContext);
                                        tomcatWebAppBuilder.beforeStart(standardContext);
                                        tomcatWebAppBuilder.start(standardContext);
                                        tomcatWebAppBuilder.afterStart(standardContext);
                                    } finally {
                                        Thread.currentThread().setContextClassLoader(oldCL);
                                    }
                                    standardContext.removeParameter("openejb.start.late");
                                }
                            }
                        }
                    }
                }
            }
        }
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
