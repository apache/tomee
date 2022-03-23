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
package org.apache.tomee.loader;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

/**
 * The sole purpose of this class is to call the {@link TomcatEmbedder#embed} method
 * <p>
 * This class gets the location of this webapp and assumes that it is
 * the tomee.war file then calls the embedder.
 * </p>
 * This method of bootstrapping is mutually exclusive to the {@link OpenEJBListener} approach
 */
@Deprecated // use tomee-overlay-runner or another way to ensure you work deterministically
public class LoaderServlet extends HttpServlet {

    //Default serial version id
    private static final long serialVersionUID = 1L;

    /**
     * Flag for starting embedded
     */
    private static boolean embedded;

    /**
     * {@inheritDoc}
     */
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        // only install once
        if (embedded) {
            return;
        }
        embedded = true;

        if (System.getProperties().containsKey("openejb.embedder.source")) {
            return;
        }

        //Gets parameters from servlet initialization parameter
        final Properties properties = initParamsToProperties(config);

        //Web application directory
        final File webappDir = new File(getWebappPath(config));

        //Sets tomee.war property
        properties.setProperty("tomee.war", webappDir.getAbsolutePath());

        //Sets source of the embedder
        properties.setProperty("openejb.embedder.source", getClass().getSimpleName());

        //@Tomcat
        TomcatEmbedder.embed(properties, config.getClass().getClassLoader());
    }

    /**
     * Retrieves all intialization parameters for this servlet and stores them in a java.util.Properties object.
     *
     * @param config jakarta.servlet.ServletConfig
     * @return java.util.Properties
     */
    protected Properties initParamsToProperties(final ServletConfig config) {
        final Properties properties = new Properties();

        //@Tomcat
        // Set some defaults
        properties.setProperty("openejb.loader", "tomcat");

        // Load in each init-param as a property
        final Enumeration<?> enumeration = config.getInitParameterNames();
        System.out.println("OpenEJB Loader init-params:");
        if (!enumeration.hasMoreElements()) {
            System.out.println("\tThere are no initialization parameters.");
        }

        while (enumeration.hasMoreElements()) {
            final String name = (String) enumeration.nextElement();
            final String value = config.getInitParameter(name);
            properties.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        return properties;
    }

    /**
     * Retrieves the absolute path of where this web application is located.
     *
     * @param config ServletConfig
     * @return absolute path of this webapp directory
     */
    private String getWebappPath(final ServletConfig config) {
        final ServletContext ctx = config.getServletContext();
        final File webInf = new File(ctx.getRealPath("/WEB-INF"));
        final File webapp = webInf.getParentFile();
        return webapp.getAbsolutePath();
    }
}
