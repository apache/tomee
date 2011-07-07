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
package org.apache.openejb.jetty.common;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.OptionsLog;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OpenEJBLifecycle extends AbstractLifeCycle {
    private List<String> applications = new ArrayList<String>();
    private Server server;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    protected void doStart() throws Exception {
        for (String application : applications) {
            deploy(application);
        }

        super.doStart();
    }

    private void deploy(String application) throws Exception {
        if (server == null) {
            throw new IllegalStateException("Server is null");
        }

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setWar(application);
        webAppContext.setTempDirectory(getTempDir(application));
        webAppContext.setClassLoader(new WebAppClassLoader(webAppContext));
        webAppContext.setServer(server);
       
        webAppContext.setConfigurationClasses(new String[] {
                WebInfConfiguration.class.getName(),
                WebXmlConfiguration.class.getName(),
                FragmentConfiguration.class.getName(),
                OpenEJBConfiguration.class.getName(),
                EnvConfiguration.class.getName(),
                JettyWebXmlConfiguration.class.getName(),
                TagLibConfiguration.class.getName()
        });

        // intercept all requests and wire up to the security service
        FilterHolder filterHolder = new FilterHolder();
        filterHolder.setFilter(new Filter() {

            private ThreadLocal<Object> oldState = new ThreadLocal<Object>();

            public void init(FilterConfig filterConfig) throws ServletException {
            }

            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                enter(servletRequest);
                filterChain.doFilter(servletRequest, servletResponse);
                exit();
            }

            private void exit() {
                JettySecurityService securityService = getSecurityService();
                if (securityService != null) {
                    securityService.exitWebApp(oldState.get());
                }

                oldState.set(null);
            }

            private void enter(ServletRequest servletRequest) {
                JettySecurityService securityService = getSecurityService();
                Request request = (Request) servletRequest;

                String runAsRole = null;
                if (request.getUserIdentityScope() != null && (request.getUserIdentityScope() instanceof ServletHolder)) {
                    runAsRole = ((ServletHolder)request.getUserIdentityScope()).getRunAsRole();
                }

                if (securityService != null) {
                    Authentication auth = request.getAuthentication();

                    if (auth != null && (auth instanceof Authentication.User)) {
                        Authentication.User user = (Authentication.User) auth;
                        oldState.set(securityService.enterWebApp(user, runAsRole));
                    }
                }
            }

            public void destroy() {
            }
        });

        webAppContext.addFilter(filterHolder, "*", 0);
        try {
            webAppContext.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        
        server.setHandler(webAppContext);
    }

    private File getTempDir(String application) {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            if (tempDir.getAbsolutePath().startsWith("/var/folders")) {
                tempDir = new File("/tmp");
            }

            File dir = File.createTempFile("Jetty_" + application.substring(application.lastIndexOf("/") + 1), "", tempDir);
            if (dir.exists()) {
                dir.delete();
            }

            dir.mkdirs();
            return dir;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void doStop() throws Exception {
        super.doStop();
    }

    public OpenEJBLifecycle() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();  // TODO: something better than this
        }
    }


    public void addApplication(String application) {
        applications.add(application);
    }

    private void init() throws Exception {
        System.setProperty("openejb.provider.default", "org.apache.openejb.jetty");
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.eclipse.jetty.jndi.InitialContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.eclipse.jetty.jndi");
        new InitialContext();

        Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("openejb.deployments.classpath.filter.systemapps", "false");

        // Loader maybe the first thing executed in a new classloader
        // so we must attempt to initialize the system instance.
        SystemInstance.init(properties);
        OptionsLog.install();

        // Read in and apply the conf/system.properties
        try {
            File conf = SystemInstance.get().getBase().getDirectory("conf");
            File file = new File(conf, "system.properties");
            if (file.exists()) {
                Properties systemProperties = new Properties();
                FileInputStream fin = new FileInputStream(file);
                InputStream in = new BufferedInputStream(fin);
                systemProperties.load(in);
                System.getProperties().putAll(systemProperties);
                // store the system properties inside SystemInstance otherwise we will lose these properties.
                // i.e. any piece of code which is trying to look for properties inside SystemInstance will not be able to find it.
                SystemInstance.get().getProperties().putAll(systemProperties);
            }
        } catch (IOException e) {
            System.out.println("Processing conf/system.properties failed: " + e.getMessage());
        }

        System.setProperty("openejb.home", SystemInstance.get().getHome().getDirectory().getAbsolutePath());
        System.setProperty("openejb.base", SystemInstance.get().getBase().getDirectory().getAbsolutePath());

        OpenEJB.init(properties, new ServerFederation());
    }

    private JettySecurityService getSecurityService() {
        SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        if (securityService instanceof JettySecurityService) {
            return (JettySecurityService) securityService;
        }
        return null;
    }

}
