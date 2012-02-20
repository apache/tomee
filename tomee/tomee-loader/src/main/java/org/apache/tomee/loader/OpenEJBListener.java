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

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The sole purpose of this class is to call the {@link TomcatEmbedder#embed} method
 *
 * This is an alternate way to load the Tomcat integration
 * This approach is mutually exclussive to the {@link LoaderServlet}
 *
 * This class does nothing more than scrape around in
 * Tomcat and look for the tomee.war so it can call the embedder
 *
 * This class can be installed in the Tomcat server.xml as an alternate
 * way to bootstrap OpenEJB into Tomcat.  The benefit of this is that
 * OpenEJB is guaranteed to start before all webapps. 
 */
public class OpenEJBListener implements LifecycleListener {
    private static final Logger LOGGER = Logger.getLogger(OpenEJBListener.class.getName());

    static private boolean listenerInstalled;
    static private boolean logWebappNotFound = true;

    public static boolean isListenerInstalled() {
        return listenerInstalled;
    }

    public void lifecycleEvent(LifecycleEvent event) {
        // only install once
        if (listenerInstalled) return;
        
        try {
	        File webappDir = findOpenEjbWar();
            if (webappDir == null && event.getSource() instanceof StandardServer) {
                final StandardServer server = (StandardServer) event.getSource();
                webappDir = tryToFindAndExtractWar(server);
                final File exploded = extractDirectory(webappDir);
                if (webappDir != null) {
                    extract(webappDir, exploded);
                }
                webappDir = exploded;
                TomcatHelper.setServer(server);
            }
            if (webappDir != null) {
                final Properties properties = new Properties();
                properties.setProperty("tomee.war", webappDir.getAbsolutePath());
                properties.setProperty("openejb.embedder.source", getClass().getSimpleName());
                TomcatEmbedder.embed(properties, StandardServer.class.getClassLoader());
                listenerInstalled = true;
            } else if (logWebappNotFound) {
                LOGGER.info("tomee webapp not found from the listener, will try from the webapp if exists");
                logWebappNotFound = false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "TomEE Listener can't start OpenEJB", e);
            // e.printStackTrace(System.err);
        }
    }

    private File extractDirectory(final File webappDir) {
        File exploded = new File(webappDir.getAbsolutePath().replace(".war", ""));
        int i = 0;
        while (exploded.exists()) {
            exploded = new File(exploded.getAbsolutePath() + "_" + i++);
        }
        return exploded;
    }

    private static File tryToFindAndExtractWar(final StandardServer source) {
        if (System.getProperties().containsKey("openejb.war")) {
            return new File(System.getProperty("openejb.war"));
        }

        for (Service service : source.findServices()) {
            final Container container = service.getContainer();
            if (container instanceof StandardEngine) {
                final StandardEngine engine = (StandardEngine) container;
                for (Container child : engine.findChildren()) {
                    if (child instanceof StandardHost) {
                        final StandardHost host = (StandardHost) child;
                        final File base = hostDir(System.getProperty("catalina.base"), host.getAppBase());

                        for (File file : base.listFiles()) {
                            if (isTomEEWar(file)) {
                                return file;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isTomEEWar(final File file) {
        final String name = file.getName();
        try {
            final JarFile jarFile = new JarFile(file);
            return jarFile.getEntry("lib") != null
                    && (name.startsWith("tomee") || name.startsWith("openejb")
                    && name.endsWith(".war"));
        } catch (IOException e) {
            return false;
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
							final File hostDir = hostDir(catalinaBase, host.getAppBase());

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

    private static File hostDir(final String catalinaBase, final String appBase) {
        File hostDir = new File(appBase);
        if (!hostDir.isAbsolute()) {
            hostDir = new File(catalinaBase, appBase);
        }
        return hostDir;
    }

    private static File findOpenEjbWar(StandardHost standardHost) {
    	//look for openejb war in a Tomcat context
    	for(Container container : standardHost.findChildren()) {
    		if(container instanceof StandardContext) {
    			StandardContext standardContext = (StandardContext)container;
    			File contextDocBase = new File(standardContext.getDocBase());
                if (!contextDocBase.isDirectory() && standardContext.getOriginalDocBase() != null) {
                    contextDocBase = new File(standardContext.getOriginalDocBase());
                }
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
            if (file.getName().startsWith("tomee-loader-") && file.getName().endsWith(".jar")) {
                // this should be the openejb war...
                // make sure it has a lib directory
                if (new File(contextDir, "lib").isDirectory()) {
                    return contextDir;
                }
            }
        }
        return null;
    }

    // copied for classloading reason
    public static void extract(final File src, final File dest) throws IOException {
        if (dest.exists()) {
            return;
        }

        LOGGER.info("Extracting openejb webapp from " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());

        dest.mkdirs();

        JarFile jarFile = null;
        InputStream input = null;
        try {
            jarFile = new JarFile(src);
            Enumeration jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                String name = jarEntry.getName();
                int last = name.lastIndexOf('/');
                if (last >= 0) {
                    File parent = new File(dest, name.substring(0, last));
                    parent.mkdirs();
                }
                if (name.endsWith("/")) {
                    continue;
                }
                input = jarFile.getInputStream(jarEntry);

                final File file = new File(dest, name);
                BufferedOutputStream output = null;
                try {
                    output = new BufferedOutputStream(new FileOutputStream(file));
                    byte buffer[] = new byte[2048];
                    while (true) {
                        int n = input.read(buffer);
                        if (n <= 0)
                            break;
                        output.write(buffer, 0, n);
                    }
                } finally {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }

                long lastModified = jarEntry.getTime();
                if (lastModified != -1 && lastModified != 0 && file != null) {
                    file.setLastModified(lastModified);
                }

                input.close();
                input = null;
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable t) {
                    // no-op
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable t) {
                    // no-op
                }
            }
        }

        LOGGER.info("Extracted openejb webapp");
    }
}
