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
package org.apache.openejb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.openejb.OpenEJBException;

/**
 * @version $Rev$ $Date$
 */
public class JarUtils {

    private static Messages messages = new Messages("org.apache.openejb.util.resources");

    static {
        setHandlerSystemProperty();
    }

    private static boolean alreadySet = false;

    @SuppressWarnings("unchecked")
    public static void setHandlerSystemProperty() {
        if (!alreadySet) {
            /*
             * Setup the java protocol handler path to include org.apache.openejb.util.urlhandler
             * so that org.apache.openejb.util.urlhandler.resource.Handler will be used for URLs
             * of the form "resource:/path".
             */ 
            /*try {
                String oldPkgs = System.getProperty( "java.protocol.handler.pkgs" );
            
                if ( oldPkgs == null )
                    System.setProperty( "java.protocol.handler.pkgs", "org.apache.openejb.util.urlhandler" );
                else if ( oldPkgs.indexOf( "org.apache.openejb.util.urlhandler" ) < 0 )
                    System.setProperty( "java.protocol.handler.pkgs", oldPkgs + "|" + "org.apache.openejb.util.urlhandler" );
            
            } catch ( SecurityException ex ) {
            }*/
            Hashtable urlHandlers = (Hashtable) AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            java.lang.reflect.Field handlers = null;
                            try {
                                handlers = URL.class.getDeclaredField("handlers");
                                handlers.setAccessible(true);
                                return handlers.get(null);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            return null;
                        }
                    }
            );
            urlHandlers.put("resource", new org.apache.openejb.util.urlhandler.resource.Handler());
            alreadySet = true;
        }
    }

    public static File getJarContaining(String path) throws OpenEJBException {
        File jarFile = null;
        try {
            URL url = new URL("resource:/" + path);

            /*
             * If we loaded the configuration from a jar, either from a jar:
             * URL or a resource: URL, we must strip off the config file location
             * from the URL.
             */
            String jarPath = null;
            if (url.getProtocol().compareTo("resource") == 0) {
                String resource = url.getFile().substring(1);

                url = getContextClassLoader().getResource(resource);
                if (url == null) {
                    throw new OpenEJBException("Could not locate a jar containing the path " + path);
                }
            }

            if (url != null) {
                jarPath = url.getFile();
                jarPath = jarPath.substring(0, jarPath.indexOf('!'));
                jarPath = jarPath.substring("file:".length());
            }

            jarFile = new File(jarPath);
            jarFile = jarFile.getAbsoluteFile();
        } catch (Exception e) {
            throw new OpenEJBException("Could not locate a jar containing the path " + path, e);
        }
        return jarFile;
    }

    public static void addFileToJar(String jarFile, String file) throws OpenEJBException {
        try {
            JarInputStream jis = new JarInputStream(new FileInputStream(jarFile));
            File tempJar = File.createTempFile("temp", "jar");
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempJar));
            JarEntry nextJarEntry = null;
            while ((nextJarEntry = jis.getNextJarEntry()) != null) {
                jos.putNextEntry(nextJarEntry);
            }
            jis.close();
            jos.putNextEntry(new JarEntry(file));
            FileInputStream fis = new FileInputStream(file);
            for (int c = fis.read(); c != -1; c = fis.read()) {
                jos.write(c);
            }
            fis.close();
            jos.close();

            File oldJar = new File(jarFile);
            oldJar.delete();
            tempJar.renameTo(oldJar);
        } catch (FileNotFoundException e) {
            throw new OpenEJBException(messages.format("file.0003", file, jarFile, e.getMessage()));
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("file.0003", file, jarFile, e.getMessage()));
        }

    }

    @SuppressWarnings("unchecked")
    public static JarFile getJarFile(String jarFile) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        JarFile jar = null;
        try {
            File file = new File(jarFile);
            jar = new JarFile(file);
        } catch (FileNotFoundException e) {
            throw new OpenEJBException(messages.format("file.0001", jarFile, e.getLocalizedMessage()));
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("file.0002", jarFile, e.getLocalizedMessage()));
        }
        return jar;
    }

    @SuppressWarnings("unchecked")
    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );    
    }
}
