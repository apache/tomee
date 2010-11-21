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
package org.apache.openejb.tomcat.loader;

import java.io.File;
import java.util.Properties;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;

/**
 * The sole purpose of this class is to call the {@link TomcatEmbedder#embed} method
 *
 * This is an alternate way to load the Tomcat integration
 * This approach is mutually exclussive to the {@link LoaderServlet}
 *
 * This class does nothing more than scrape around in
 * Tomcat and look for the openejb.war so it can call the embedder
 *
 * This class can be installed in the Tomcat server.xml as an alternate
 * way to bootstrap OpenEJB into Tomcat.  The benefit of this is that
 * OpenEJB is guaranteed to start before all webapps. 
 */
public class OpenEJBListener implements LifecycleListener {
    static private boolean listenerInstalled;

    public static boolean isListenerInstalled() {
        return listenerInstalled;
    }

    public void lifecycleEvent(LifecycleEvent event) {
        // only install once
        if (listenerInstalled) return;
        
        try {
	        Properties properties = new Properties();
	        File webappDir = findOpenEjbWar();
	        properties.setProperty("openejb.war", webappDir.getAbsolutePath());
	        properties.setProperty("openejb.embedder.source", getClass().getSimpleName());
	        TomcatEmbedder.embed(properties, StandardServer.class.getClassLoader());
	        listenerInstalled = true;
        } catch (Exception e) {
        }
    }

    private static File findOpenEjbWar() {
        // in Tomcat 5.5 the OpenEjb war is in the server/webapps director
        String catalinaBase = System.getProperty("catalina.base");
        File serverWebapps = new File(catalinaBase, "server/webapps");
        File openEjbWar = findOpenEjbWar(serverWebapps);
        if (openEjbWar != null) {
            return openEjbWar;
        }
		        
		try {
			// in Tomcat 6 the OpenEjb war is normally in webapps, but we just
			// scan all hosts directories
			for (Service service : TomcatHelper.getServer().findServices()) {
				Container container = service.getContainer();
				if (container instanceof StandardEngine) {
					StandardEngine engine = (StandardEngine) container;
					for (Container child : engine.findChildren()) {
						if (child instanceof StandardHost) {
							StandardHost host = (StandardHost) child;
							String appBase = host.getAppBase();

							// determine the host dir (normally webapps)
							File hostDir = new File(appBase);
							if (!hostDir.isAbsolute()) {
								hostDir = new File(catalinaBase, appBase);
							}

							openEjbWar = findOpenEjbWar(hostDir);
							if (openEjbWar != null) {
								return openEjbWar;
							} else {
								return findOpenEjbWar(host);
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}      
		
		return null;
    }
    
    private static File findOpenEjbWar(StandardHost standardHost) {
    	//look for openejb war in a Tomcat context
    	for(Container container : standardHost.findChildren()) {
    		if(container instanceof StandardContext) {
    			StandardContext standardContext = (StandardContext)container;
    			File contextDocBase = new File(standardContext.getDocBase());
    			if(contextDocBase.isDirectory()) {
	    			File openEjbWar = findOpenEjbWarInContext(contextDocBase);
	    	        if (openEjbWar != null) {
	    	            return openEjbWar;
	    	        }
    			}
    		}
    	}
    	return null;
    }

    private static File findOpenEjbWar(File hostDir) {
        if (!hostDir.isDirectory()) {
            return null;
        }

        // iterate over the contexts
        for (File contextDir : hostDir.listFiles()) {
        	File foundContextDir = findOpenEjbWarInContext(contextDir);
        	if(foundContextDir != null) {
        		return foundContextDir;
        	}
        }
        return null;
    }
     
    private static File findOpenEjbWarInContext(File contextDir) {
        // does this war have a web-inf lib dir
        File webInfLib = new File(new File(contextDir, "WEB-INF"), "lib");
        if (!webInfLib.isDirectory()) {
             return null;
        }
        // iterate over the libs looking for the openejb-loader-*.jar
        for (File file : webInfLib.listFiles()) {
            if (file.getName().startsWith("openejb-tomcat-loader-") && file.getName().endsWith(".jar")) {
                // this should be the openejb war...
                // make sure it has a lib directory
                if (new File(contextDir, "lib").isDirectory()) {
                    return contextDir;
                }
            }
        }
        return null;
    }
}
