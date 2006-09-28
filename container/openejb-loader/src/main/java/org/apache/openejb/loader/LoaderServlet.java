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
package org.apache.openejb.loader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins </a>
 */
public class LoaderServlet extends HttpServlet {

    private Loader loader;

    public void init(ServletConfig config) throws ServletException {
        if (loader != null) {
            return;
        }

        // Do just enough to get the Tomcat Loader into the classpath
        // let it do the rest.
        Properties p = initParamsToProperties(config);

        String embeddingStyle = p.getProperty("openejb.loader");

        // Set the mandatory values for a webapp-only setup
        if (embeddingStyle.endsWith("tomcat-webapp")) {
            setPropertyIfNUll(p, "openejb.base", getWebappPath(config));
//            setPropertyIfNUll(p, "openejb.configuration", "META-INF/openejb.xml");
//            setPropertyIfNUll(p, "openejb.container.decorators", "org.apache.openejb.tomcat.TomcatJndiSupport");
//            setPropertyIfNUll(p, "log4j.configuration", "META-INF/log4j.properties");
        } else if (embeddingStyle.endsWith("tomcat-system")){
            String webappPath = getWebappPath(config);
            File webappDir = new File(webappPath);
            File libDir = new File(webappDir, "lib");
            String catalinaHome = System.getProperty("catalina.home");
            p.setProperty("openejb.home", catalinaHome);
            String catalinaBase = System.getProperty("catalina.base");
            p.setProperty("openejb.base", catalinaBase);
            String libPath = libDir.getAbsolutePath();
            p.setProperty("openejb.libs", libPath);
        }

        try {
            SystemInstance.init(p);
            Embedder embedder = new Embedder("org.apache.openejb.tomcat.TomcatLoader");
            Class loaderClass = embedder.load();
            Object instance = loaderClass.newInstance();
            try {
                loader = (Loader) instance;
            } catch (ClassCastException e) {
                loader = new LoaderWrapper(instance);
            }

            loader.init(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        loader.service(request, response);
    }

    private String getWebappPath(ServletConfig config) {
        ServletContext ctx = config.getServletContext();
        File webInf = new File(ctx.getRealPath("WEB-INF"));
        File webapp = webInf.getParentFile();
        String webappPath = webapp.getAbsolutePath();
        return webappPath;
    }

    private Properties initParamsToProperties(ServletConfig config) {
        Properties p = new Properties();

        // Set some defaults
        p.setProperty("openejb.loader", "tomcat");

        // Load in each init-param as a property
        Enumeration enumeration = config.getInitParameterNames();
        System.out.println("OpenEJB init-params:");
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = config.getInitParameter(name);
            p.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        return p;
    }

    private Object setPropertyIfNUll(Properties properties, String key, String value) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null) {
            properties.setProperty(key, value);
        }
        return currentValue;
    }

    /**
     * Ain't classloaders fun?
     * This class exists to reconcile that loader implementations
     * may exist in the parent classloader while the loader interface
     * is also in this classloader.  Use this class in the event that
     * this is the case.
     * Think of this as an adapter for adapting the parent's idea of a
     * Loader to our idea of a Loader.
     */
    public static class LoaderWrapper implements Loader {
        private final Object loader;
        private final Method init;
        private final Method service;

        public LoaderWrapper(Object loader) {
            this.loader = loader;
            try {
                Class loaderClass = loader.getClass();
                this.init = loaderClass.getMethod("init", new Class[]{ServletConfig.class});
                this.service = loaderClass.getMethod("service", new Class[]{HttpServletRequest.class, HttpServletResponse.class});
            } catch (NoSuchMethodException e) {
                throw (IllegalStateException) new IllegalStateException("Signatures for Loader are no longer correct.").initCause(e);
            }
        }

        public void init(ServletConfig servletConfig) throws ServletException {
            try {
                init.invoke(loader, new Object[]{servletConfig});
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw (ServletException) cause;
                }
            } catch (Exception e) {
                throw new RuntimeException("Loader.init: " + e.getMessage() + e.getClass().getName() + ": " + e.getMessage(), e);
            }
        }

        public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            try {
                service.invoke(loader, new Object[]{request, response});
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else {
                    throw (ServletException) cause;
                }
            } catch (Exception e) {
                throw new RuntimeException("Loader.service: " + e.getMessage() + e.getClass().getName() + ": " + e.getMessage(), e);
            }
        }
    }
}
