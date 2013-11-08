/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jpa.tools.swing;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Searches for a given class in the set of directories recursively.
 * If a given directory contains jar file then searches inside the jar.
 * 
 *  Usage
 *  $ java find.Finder class dir1 dir2...
 *  where
 *     class is fully qualified class name
 *     dir name of a file system directory
 *  
 *  Example
 *     $ java find.Finder org.eclipse.ui.plugin.AbstractUIPlugin c:\eclipse\plugins
 *  will print
 *      org.eclipse.ui.plugin.AbstractUIPlugin found in 
 *      c:\eclipse\plugins\org.eclipse.ui.workbench_3.4.1.M20080827-0800a.jar
 *      
 * @author Pinaki Poddar
 *
 */
public class Finder {
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR_SUFFIX = ".jar";
    private static final char DOT = '.';
    private static final boolean DEBUG = false;
    
    private static void usage() {
        System.err.println("Searches a given class in the given directories."
        + "\r\nIf a given directory contains jar then searches within the jar."
        + "\r\nIf a given directory contains other directories then searches "
        + "recursively.\r\n");
        System.err.println("\r\n Usage:");
        System.err.println(" $ java find.Finder class dir [dir...]");
        System.err.println(" where");
        System.err.println("   class fully-qualified class name");
        System.err.println("   dir name of a directory");
        System.err.println("\r\n Example:");
        System.err.println(" $ java find.Finder java.lang.Object c:\\java");
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(0);
        }
        Finder finder = new Finder();
        for (int i = 1; i < args.length; i++) {
            File root = new File(args[i]);
            finder.scan(root, args[0]);
        }
    }
    
    private void scan(File dir, String cls) throws IOException {
        File[] classes = dir.listFiles(new FileFilter() {
            public boolean accept(File path) {
                return path.getName().endsWith(CLASS_SUFFIX);
            }
        });
        String clsName = cls+CLASS_SUFFIX;
        for (File c : classes) {
            String name = c.getName().replace(File.separatorChar, DOT);
            if (name.endsWith(clsName))
                System.err.println(cls + " found in " + c.getAbsolutePath());
        }

        File[] jars = dir.listFiles(new FileFilter() {
            public boolean accept(File path) {
                return path.getName().endsWith(JAR_SUFFIX);
            }
        });
        
        for (File jar : jars) {
            JarFile jarFile = new JarFile(jar);
            scan(jarFile, cls);
        }
        
        File[] dirs = dir.listFiles(new FileFilter() {
            public boolean accept(File path) {
                return path.isDirectory();
            }
        });
        for (File cdir : dirs) 
            scan(cdir, cls);
    }
    
    private void scan(JarFile jar, String cls) {
        String clsName = cls.replace('.', '/') + CLASS_SUFFIX;
        debug("Scanning " + jar.getName() + " for [" + clsName + "]");
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(CLASS_SUFFIX))
                debug("  " + entryName);
            if (entryName.equals(clsName))
                System.err.println(cls + " found in " + jar.getName());
        }
    }
    
    private void debug(String s) {
        if (DEBUG)
            System.err.println(s);
    }
}

