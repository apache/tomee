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
            throw new OpenEJBException(messages.format("file.0003", file, jarFile, e.getMessage()), e);
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("file.0003", file, jarFile, e.getMessage()), e);
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
            throw new OpenEJBException(messages.format("file.0001", jarFile, e.getLocalizedMessage()), e);
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("file.0002", jarFile, e.getLocalizedMessage()), e);
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
