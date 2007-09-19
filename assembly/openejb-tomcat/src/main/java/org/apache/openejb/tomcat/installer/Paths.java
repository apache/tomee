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
package org.apache.openejb.tomcat.installer;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Paths {
    private final ServletContext servletContext;
    private final List<String> errors = new ArrayList<String>();
    private File catalinaHomeDir;
    private File catalinaBaseDir;
    private File serverXmlFile;

    public Paths(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public File getCatalinaHomeDir() {
        if (catalinaHomeDir == null) {
            String catalinaHome = System.getProperty("catalina.home");
            if (catalinaHome != null) {
                catalinaHomeDir = new File(catalinaHome);
            }
        }
        return catalinaHomeDir;
    }

    public void setCatalinaHomeDir(String catalinaHomeDir) {
        this.catalinaHomeDir = createFile(catalinaHomeDir);
    }

    public void setCatalinaHomeDir(File catalinaHomeDir) {
        this.catalinaHomeDir = catalinaHomeDir;
    }

    public File getCatalinaBaseDir() {
        if (catalinaBaseDir == null) {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                catalinaBaseDir = new File(catalinaBase);
            }
        }
        return catalinaBaseDir;
    }

    public void setCatalinaBaseDir(String catalinaBaseDir) {
        this.catalinaBaseDir = createFile(catalinaBaseDir);
    }

    public void setCatalinaBaseDir(File catalinaBaseDir) {
        this.catalinaBaseDir = catalinaBaseDir;
    }

    public File getServerXmlFile() {
        if (serverXmlFile == null) {
            File confdir = getCatalinaConfDir();

            if (confdir == null) return null;

            serverXmlFile = new File(confdir, "server.xml");
        }
        return serverXmlFile;
    }

    public void setServerXmlFile(String serverXmlFile) {
        this.serverXmlFile = createFile(serverXmlFile);
    }

    public void setServerXmlFile(File serverXmlFile) {
        this.serverXmlFile = serverXmlFile;
    }

    public File getCatalinaLibDir() {
        File catalinaHomeDir = getCatalinaHomeDir();

        if (catalinaHomeDir == null) return null;

        return new File(catalinaHomeDir, "lib");
    }

    public File getCatalinaConfDir() {
        File catalinaBaseDir = getCatalinaBaseDir();

        if (catalinaBaseDir == null) return null;

        return new File(catalinaBaseDir, "conf");
    }

    public File getCatalinaBinDir() {
        File catalinaHomeDir = getCatalinaHomeDir();

        if (catalinaHomeDir == null) return null;

        return new File(catalinaHomeDir, "bin");
    }

    public File getCatalinaShFile() {
        File binDir = getCatalinaBinDir();

        if (binDir == null) return null;

        return new File(binDir, "catalina.sh");
    }

    public File getCatalinaBatFile() {
        File binDir = getCatalinaBinDir();

        if (binDir == null) return null;

        return new File(binDir, "catalina.bat");
    }

    public File getOpenEJBLibDir() {
        if (servletContext == null) return null;

        return new File(servletContext.getRealPath("lib"));
    }

    public File getOpenEJBLoaderJar() {
        return findOpenEJBJar("openejb-loader");
    }

    public File getOpenEJBJavaagentJar() {
        return findOpenEJBJar("openejb-javaagent");
    }

    public File getOpenEJBCoreJar() {
        return findOpenEJBJar("openejb-core");
    }

    private File findOpenEJBJar(String namePrefix) {
        File openEJBLibDir = getOpenEJBLibDir();
        if (openEJBLibDir == null) return null;

        File openejbLoaderJar = null;
        for (File file : openEJBLibDir.listFiles()) {
            if (file.getName().startsWith(namePrefix + "-") && file.getName().endsWith(".jar")) {
                return file;
            }
        }

        return openejbLoaderJar;
    }

    public File getUpdatedAnnotationApiJar() {
        if (servletContext == null) return null;

        return new File(servletContext.getRealPath("tomcat/annotations-api.jar"));
    }

    public boolean verify() {
        if (getCatalinaHomeDir() == null) {
            addError("Catalina home directory is not defined");
        }
        if (getCatalinaBaseDir() == null) {
            addError("Catalina base directory is not defined");
        }

        verifyDirectory("Catalina home", getCatalinaHomeDir());
        verifyDirectory("Catalina base", getCatalinaBaseDir());

        // if catalina home or base has errors, just give up
        if (hasErrors()) {
            return false;
        }

        verifyWritableDirectory("Catalina lib", getCatalinaLibDir());
        verifyWritableDirectory("Catalina conf", getCatalinaConfDir());
        verifyDirectory("Catalina bin", getCatalinaBinDir());
        verifyWritableFile("Catalina server.xml", getServerXmlFile());
        verifyWritableFile("Catalina catalina.sh", getCatalinaShFile());
        verifyWritableFile("Catalina catalina.sh", getCatalinaBatFile());

        verifyDirectory("OpenEJB lib", getOpenEJBLibDir());

        File openejbLoaderJar = getOpenEJBLoaderJar();
        if (openejbLoaderJar == null) {
            addError("OpenEJB loader jar was not found in the OpenEJB lib dir");
        }
        verifyFile("OpenEJB loader jar", openejbLoaderJar);

        File openejbJavaagentJar = getOpenEJBJavaagentJar();
        if (openejbJavaagentJar == null) {
            addError("OpenEJB javaagent jar was not found in the OpenEJB lib dir");
        }
        verifyFile("OpenEJB javaagent jar", openejbJavaagentJar);

        File openejbCoreJar = getOpenEJBCoreJar();
        if (openejbCoreJar != null) {
            verifyFile("OpenEJB core jar", openejbCoreJar);
        }

        verifyFile("Updated Tomcat annotation-api jar", getUpdatedAnnotationApiJar());
        
        return !hasErrors();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }


    public List<String> getErrors() {
        return errors;
    }

    private void addError(String message) {
        System.out.println(message);
    }

    private void verifyDirectory(String description, File file) {
        if (file == null) {
            // ignore... files are built up based on other files, and probles
            // with the root files will have been logged else where
            return;
        }
        if (!file.exists()) {
            addError(description + " directory does not exist");
            return;
        }
        if (!file.isDirectory()) {
            addError(description + " directory is not a directory");
            return;
        }
        if (!file.canRead()) {
            addError(description + " directory is not readable");
        }
    }

    private void verifyWritableDirectory(String description, File file) {
        verifyDirectory(description, file);
        verifyWritable(description, file);
    }

    private void verifyFile(String description, File file) {
        if (file == null) {
            // ignore... files are built up based on other files, and probles
            // with the root files will have been logged else where
            return;
        }
        if (!file.exists()) {
            addError(description + " file does not exist");
            return;
        }
        if (!file.isFile()) {
            addError(description + " file is not a file");
            return;
        }
        if (!file.canRead()) {
            addError(description + " file is not readable");
        }
    }

    private void verifyWritableFile(String description, File file) {
        verifyFile(description, file);
        verifyWritable(description, file);
    }

    private void verifyWritable(String description, File file) {
        if (file == null) {
            // ignore... files are built up based on other files, and probles
            // with the root files will have been logged else where
            return;
        }
        if (!file.canWrite()) {
            addError(description + " file is not writable");
        }
    }

    private File createFile(String catalinaHomeDir) {
        if (catalinaHomeDir != null && catalinaHomeDir.trim().length() > 0) {
            return new File(catalinaHomeDir.trim());
        }
        return null;
    }

    public void dump(ServletOutputStream out) throws IOException {
        printFile(out, "Catalina home: ", getCatalinaHomeDir());
        printFile(out, "Catalina base: ", getCatalinaBaseDir());
        printFile(out, "Catalina server.xml: ", getServerXmlFile());
        printFile(out, "Catalina conf: ", getCatalinaConfDir());
        printFile(out, "Catalina lib: ", getCatalinaLibDir());
        printFile(out, "Catalina bin: ", getCatalinaBinDir());
        printFile(out, "Catalina catalina.sh: ", getCatalinaShFile());
        printFile(out, "Catalina catalina.bat: ", getCatalinaBatFile());
        printFile(out, "OpenEJB lib: ", getOpenEJBLibDir());
        printFile(out, "OpenEJB loader jar: ", getOpenEJBLoaderJar());
        printFile(out, "OpenEJB javaagent jar: ", getOpenEJBJavaagentJar());
    }

    private void printFile(ServletOutputStream out, String description, File file) throws IOException {
        out.println(description + ":");
        out.println("    " + file);
    }
}
