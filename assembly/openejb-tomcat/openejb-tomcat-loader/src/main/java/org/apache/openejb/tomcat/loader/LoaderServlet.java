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
package org.apache.openejb.tomcat.loader;

import java.util.Properties;
import java.util.Enumeration;
import java.io.File;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

/**
 * The sole purpose of this class is to call the {@link TomcatEmbedder#embed} method
 *
 * This class gets the location of this webapp and assumes that it is
 * the openejb.war file then calls the embedder.
 *
 * This method of bootstrapping is mutually exclussive to the {@link OpenEJBListener} approach
 */
public class LoaderServlet extends HttpServlet {
    
    //Default serial version id
    private static final long serialVersionUID = 1L;
    
    /**Flag for starting embedded*/
    private static boolean embedded = false;

    /**
     * {@inheritDoc}
     */
    public void init(ServletConfig config) throws ServletException {
        // only install once
        if (embedded) return;
        embedded = true;
        
        //Gets parameters from servlet initialization parameter
        Properties properties = initParamsToProperties(config);
        
        //Web application directory
        File webappDir = new File(getWebappPath(config));
        
        //Sets openejb.war property
        properties.setProperty("openejb.war", webappDir.getAbsolutePath());
        
        //Sets source of the embedder
        properties.setProperty("openejb.embedder.source", getClass().getSimpleName());

        //@Tomcat
        TomcatEmbedder.embed(properties, config.getClass().getClassLoader());
    }
    
    /**
     * Retrieves all intialization parameters for this servlet and stores them in a java.util.Properties object.
     * @param config javax.servlet.ServletConfig
     * @return java.util.Properties
     */
    private Properties initParamsToProperties(ServletConfig config) {
        Properties properties = new Properties();

        //@Tomcat
        // Set some defaults
        properties.setProperty("openejb.loader", "tomcat");

        // Load in each init-param as a property
        Enumeration<?> enumeration = config.getInitParameterNames();
        System.out.println("OpenEJB Loader init-params:");
        if(!enumeration.hasMoreElements()) {
            System.out.println("\tThere are no initialization parameters.");
        }
        
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = config.getInitParameter(name);
            properties.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        return properties;
    }
    /**
     * Retrieves the absolute path of where this web application is located.
     * 
     * @param config
     * @return absolute path of this webapp directory
     */
    private String getWebappPath(ServletConfig config) {
        ServletContext ctx = config.getServletContext();
        File webInf = new File(ctx.getRealPath("WEB-INF"));
        File webapp = webInf.getParentFile();
        String webappPath = webapp.getAbsolutePath();
        return webappPath;
    }
}
