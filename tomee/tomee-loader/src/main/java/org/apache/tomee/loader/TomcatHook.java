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
package org.apache.tomee.loader;

import org.apache.openejb.loader.Embedder;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * This class should only be loadded and used via reflection from TomcatEmbedder.
 *
 * Everything that happens up to the point of calling this particular method
 * (except setting tomee.war) ultimately means nothing and does not matter
 * to the integration.
 *
 * Requires tomee.war to be set, the sets the following properties:
 *
 * System properties:
 * set openejb.home -> catalina.home
 * set openejb.base -> catalina.base
 * set tomee.war -> $tomee.war
 * set tomcat.version if not set
 * set tomcat.built if not set
 *
 * Local properties: 
 * set openejb.loader -> tomcat-system
 * set openejb.libs -> $tomee.war/lib
 *
 * With these properties setup, this class with construct an {@link Embedder}
 * using the "org.apache.tomee.catalina.TomcatLoader" as the loader.
 *
 * The Embedder will use the openejb.libs property to find all the jars to be loaded
 * then it will use the openejb.loader property to find out *how* to add them into
 * the classpath of the right classloader.  Once all the jars are in the required
 * class loader, it loads the {@link org.apache.openejb.loader.Loader} implementation
 * and calls it's {@link org.apache.openejb.loader.Loader#init} method.
 *
 * See org.apache.tomee.catalina.TomcatLoader for the next part of the story
 */
class TomcatHook {
    /**
     * Using tomee.war path, it sets several required
     * system properties and init {@link SystemInstance#init(Properties)}
     * 
     * <p>
     * This method is called from {@link TomcatEmbedder#embed(Properties, ClassLoader)}
     * method from classloader that contains openejb-loader and tomee-loader 
     * with CatalinaClassLoader as the parent.
     *
     * @param properties properties file
     */
    static void hook(final Properties properties) {
        
        // verify properties and make sure it contains the tomee.war property
        if (properties == null) {
            throw new NullPointerException("properties is null");
        }
        
        //Check tomee.war property
        //This property is set by the LoaderServlet or OpenEJBListener
        //When you deploy tomee.war into webapps/ directory of the tomcat
        //Loader servlet automatically starts and initialize this property
        if (!properties.containsKey("tomee.war")) {
            throw new IllegalArgumentException("properties must contain the tomee.war property");
        }

        
        //Get the openejb directory (under webapps) using the tomee.war property
        final File openejbWar = new File(properties.getProperty("tomee.war"));
        if (!openejbWar.isDirectory()) {
            throw new IllegalArgumentException("tomee.war is not a directory: " + openejbWar);
        }

        // if SystemInstance is already initialized, then return
        if (SystemInstance.isInitialized()) {
            return;
        }

        // set the openejb.loader property to tomcat-system
        properties.setProperty("openejb.loader", "tomcat-system");

        // Get the value of catalina.home and set it to openejb.home
        final String catalinaHome = System.getProperty("catalina.home");
        properties.setProperty("openejb.home", catalinaHome);
        
        //Sets system property for openejb.home
        System.setProperty("openejb.home", catalinaHome);

        //get the value of catalina.base and set it to openejb.base
        final String catalinaBase = System.getProperty("catalina.base");
        properties.setProperty("openejb.base", catalinaBase);
        
        //Sets system property for openejb.base
        System.setProperty("openejb.base", catalinaBase);

        // Set the tomee.war property as a *System* property
        System.setProperty("tomee.war", openejbWar.getAbsolutePath());
        
        // set the property openejb.libs to contain the absolute path of the lib directory of openejb webapp
        final File libDir = new File(openejbWar, "lib");
        final String libPath = libDir.getAbsolutePath();
        
        //Sets openejb.libs to tomee.war/lib folder
        properties.setProperty("openejb.libs", libPath);

        // System.setProperty("tomcat.version", "x.y.z.w");
        // System.setProperty("tomcat.built", "mmm dd yyyy hh:mm:ss");
        // set the System properties, tomcat.version, tomcat.built
        final ClassLoader classLoader = TomcatHook.class.getClassLoader();
        try {
            final Properties tomcatServerInfo = IO.readProperties(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"), new Properties());

            String serverNumber = tomcatServerInfo.getProperty("server.number");
            if (serverNumber == null) {
                // Tomcat5 only has server.info
                final String serverInfo = tomcatServerInfo.getProperty("server.info");
                if (serverInfo != null) {
                    final int slash = serverInfo.indexOf('/');
                    serverNumber = serverInfo.substring(slash + 1);
                }
            }
            if (serverNumber != null) {
                System.setProperty("tomcat.version", serverNumber);
            }

            final String serverBuilt = tomcatServerInfo.getProperty("server.built");
            if (serverBuilt != null) {
                System.setProperty("tomcat.built", serverBuilt);
            }
        } catch (final Throwable e) {
            // no-op
        }

        if( properties.getProperty("openejb.libs") == null){
            throw new NullPointerException("openejb.libs property is not set");
        }

        // manage additional libraries
        if (URLClassLoader.class.isInstance(classLoader)) {
            final URLClassLoader ucl = URLClassLoader.class.cast(classLoader);
            try {
                final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                for (final File f : ProvisioningUtil.addAdditionalLibraries()) {
                    if (!addUrl.isAccessible()) { // set it lazily
                        addUrl.setAccessible(true);
                    }
                    addUrl.invoke(ucl, f.toURI().toURL());
                }
            } catch (final Exception e) {
                e.printStackTrace(); // shouldn't block
            }
        }

        // set the embedder
        final Embedder embedder = new Embedder("org.apache.tomee.catalina.TomcatLoader");
        SystemInstance.get().setComponent(Embedder.class, embedder);
        try {
            // create the loader

            // This init call affects only this WebappClassloader and is just required
            // for runnig the Embedder.  The SystemInstance will be initialized more permanently
            // in the parent classloader once the required libraries are added.
            SystemInstance.init(properties);

            // This guy does the magic of squishing the openejb libraries into the parent classloader
            // and kicking off the reall integration.
            embedder.init(properties);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
