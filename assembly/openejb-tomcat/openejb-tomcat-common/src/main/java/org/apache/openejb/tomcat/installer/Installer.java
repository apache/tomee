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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.webapp.common.Alerts;
import org.apache.openejb.webapp.common.Installers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.jar.JarFile;

public class Installer {
    private final Alerts alerts = new Alerts();

    public enum Status {
        NONE, INSTALLED, REBOOT_REQUIRED
    }

    private static final boolean listenerInstalled;
    private static final boolean agentInstalled;

    private boolean force = false;

    static {
        // is the OpenEJB listener installed
        listenerInstalled = "OpenEJBListener".equals(SystemInstance.get().getProperty("openejb.embedder.source"));

        // is the OpenEJB javaagent installed
        agentInstalled = invokeStaticNoArgMethod("org.apache.openejb.javaagent.Agent", "getInstrumentation") != null;
    }

    public static boolean isListenerInstalled() {
        return listenerInstalled;
    }

    public static boolean isAgentInstalled() {
        return agentInstalled;
    }

    private final Paths paths;
    private Status status = Status.NONE;

    public Installer(Paths paths) {
        this.paths = paths;

        if (listenerInstalled && agentInstalled) {
            status = Status.INSTALLED;
        }
    }

    public Installer(Paths paths, boolean force) {
        this (paths);
        this.force = force;
    }


    public Alerts getAlerts() {
        return alerts;
    }
    
    public void reset() {
        alerts.reset();
    }

    public Status getStatus() {
        return status;
    }

    public void installAll() {
        installListener();

        installJavaagent();

        installConfigFiles();
        
        if (!alerts.hasErrors()) {
            status = Status.REBOOT_REQUIRED;
        }
    }

    public void installListener() {
        if (listenerInstalled && !force) {
//            addInfo("OpenEJB Listener already installed");
            return;
        }

        boolean copyOpenEJBLoader = true;

        // copy loader jar to lib
        File destination = new File(paths.getCatalinaLibDir(), paths.getOpenEJBTomcatLoaderJar().getName());
        if (destination.exists()) {
            if (paths.getOpenEJBTomcatLoaderJar().length() != destination.length()) {
                // md5 diff the files
            } else {
//                addInfo("OpenEJB loader jar already installed in Tomcat lib directory.");
                copyOpenEJBLoader = false;
            }
        }

        if (copyOpenEJBLoader) {
            try {
                Installers.copyFile(paths.getOpenEJBTomcatLoaderJar(), destination);
                alerts.addInfo("Copy " + paths.getOpenEJBTomcatLoaderJar().getName() + " to lib");
            } catch (IOException e) {
                alerts.addError("Unable to copy OpenEJB Tomcat loader jar to Tomcat lib directory.  This will need to be performed manually.", e);
            }
        }

        // read server.xml
        String serverXmlOriginal = Installers.readAll(paths.getServerXmlFile(), alerts);

        // server xml will be null if we couldn't read the file
        if (serverXmlOriginal == null) {
            return;
        }

        // does the server.xml contain our listener name... it is possible that they commented out our listener, but that would be a PITA to detect
        if (serverXmlOriginal.contains("org.apache.openejb.tomcat.loader.OpenEJBListener")) {
            alerts.addWarning("OpenEJB Listener already declared in Tomcat server.xml file.");
            return;
        }

        // if we can't backup the file, do not modify it
        if (!Installers.backup(paths.getServerXmlFile(), alerts)) {
            return;
        }

        // add our listener
        String newServerXml = null;
        try {
            newServerXml = Installers.replace(serverXmlOriginal,
                    "<Server",
                    "<Server",
                    ">",
                    ">\r\n" +
                            "  <!-- OpenEJB plugin for Tomcat -->\r\n" +
                            "  <Listener className=\"org.apache.openejb.tomcat.loader.OpenEJBListener\" />");
        } catch (IOException e) {
            alerts.addError("Error while adding listener to server.xml file", e);
        }

        // overwrite server.xml
        if (Installers.writeAll(paths.getServerXmlFile(), newServerXml, alerts)) {
            alerts.addInfo("Add OpenEJB listener to server.xml");
        }
    }

    public void installJavaagent() {
        if (agentInstalled && !force) {
//            addInfo("OpenEJB Agent already installed");
            return;
        }

        //
        // Copy openejb-javaagent.jar to lib
        //
        boolean copyJavaagentJar = true;
        File javaagentJar = new File(paths.getCatalinaLibDir(), "openejb-javaagent.jar");
        if (javaagentJar.exists()) {
            if (paths.getOpenEJBJavaagentJar().length() != javaagentJar.length()) {
                // md5 diff the files
            } else {
//                addInfo("OpenEJB javaagent jar already installed in Tomcat lib directory.");
                copyJavaagentJar = false;
            }
        }

        if (copyJavaagentJar) {
            try {
                Installers.copyFile(paths.getOpenEJBJavaagentJar(), javaagentJar);
                alerts.addInfo("Copy " + paths.getOpenEJBJavaagentJar().getName() + " to lib");
            } catch (IOException e) {
                alerts.addError("Unable to copy OpenEJB javaagent jar to Tomcat lib directory.  This will need to be performed manually.", e);
            }
        }


        //
        // bin/catalina.sh
        //

        // read the catalina sh file
        String catalinaShOriginal = Installers.readAll(paths.getCatalinaShFile(), alerts);

        // catalina sh will be null if we couldn't read the file
        if (catalinaShOriginal == null) {
            return;
        }

        // does the catalina sh contain our comment... it is possible that they commented out the magic script code, but there is no way to detect that
        if (catalinaShOriginal.contains("Add OpenEJB javaagent")) {
            alerts.addWarning("OpenEJB javaagent already declared in Tomcat catalina.sh file.");
            return;
        }

        // if we can't backup the file, do not modify it
        if (!Installers.backup(paths.getCatalinaShFile(), alerts)) {
            return;
        }

        // add our magic bits to the catalina sh file
        String openejbJavaagentPath = paths.getCatalinaHomeDir().toURI().relativize(javaagentJar.toURI()).getPath();
        String newCatalinaSh = catalinaShOriginal.replace("# ----- Execute The Requested Command",
                "# Add OpenEJB javaagent\n" +
                "if [ -r \"$CATALINA_HOME\"/" + openejbJavaagentPath + " ]; then\n" +
                "  JAVA_OPTS=\"\"-javaagent:$CATALINA_HOME/" + openejbJavaagentPath + "\" $JAVA_OPTS\"\n" +
                "fi\n" +
                "\n" +
                "# ----- Execute The Requested Command");

        // overwrite the catalina.sh file
        if (Installers.writeAll(paths.getCatalinaShFile(), newCatalinaSh, alerts)) {
            alerts.addInfo("Add OpenEJB JavaAgent to catalina.sh");
        }

        //
        // bin/catalina.bat
        //

        // read the catalina bat file
        String catalinaBatOriginal = Installers.readAll(paths.getCatalinaBatFile(), alerts);

        // catalina bat will be null if we couldn't read the file
        if (catalinaBatOriginal == null) {
            return;
        }

        // does the catalina bat contain our comment... it is possible that they commented out the magic script code, but there is no way to detect that
        if (catalinaBatOriginal.contains("Add OpenEJB javaagent")) {
            alerts.addWarning("OpenEJB javaagent already declared in Tomcat catalina.bat file.");
            return;
        }

        // if we can't backup the file, do not modify it
        if (!Installers.backup(paths.getCatalinaBatFile(), alerts)) {
            return;
        }

        // add our magic bits to the catalina bat file
        openejbJavaagentPath = openejbJavaagentPath.replace('/', '\\');
        String newCatalinaBat = catalinaBatOriginal.replace("rem ----- Execute The Requested Command",
                "rem Add OpenEJB javaagent\r\n" +
                "if not exist \"%CATALINA_HOME%\\" + openejbJavaagentPath + "\" goto noOpenEJBJavaagent\r\n" +
                "set JAVA_OPTS=\"-javaagent:%CATALINA_HOME%\\" + openejbJavaagentPath + "\" %JAVA_OPTS%\r\n" +
                ":noOpenEJBJavaagent\r\n" +
                "\r\n" +
                "rem ----- Execute The Requested Command");

        // overwrite the catalina.bat file
        if (Installers.writeAll(paths.getCatalinaBatFile(), newCatalinaBat, alerts)) {
            alerts.addInfo("Add OpenEJB JavaAgent to catalina.bat");
        }
    }
    /**
     * Installs conf/openejb.xml and conf/logging.properties files.
     * This method retrieves the openejb.xml and logging.properties files
     * from openejb core jar file and installs them under the conf directory
     * of tomcat. if there is already a conf/logging.properties file available
     * then this method appends the contents of openejb logging.properties file
     * to the exisiting properties file. 
     * NOTE:- If the existing conf/logging.properties file already has some openejb specific
     * configuration, then this method will just leave the logging.properties file alone
     */
    public void installConfigFiles() {
        final File openejbCoreJar = paths.getOpenEJBCoreJar();
        final File confDir = paths.getCatalinaConfDir();
        final Alerts alerts = this.alerts;

        if (openejbCoreJar == null) {
            // the core jar contains the config files
            return;
        }
        JarFile coreJar;
        try {
            coreJar = new JarFile(openejbCoreJar);
        } catch (IOException e) {
            return;
        }

        //
        // conf/openejb.xml
        //

        File openEjbXmlFile = new File(confDir, "openejb.xml");
        if (!openEjbXmlFile.exists()) {
            // read in the openejb.xml file from the openejb core jar
            String openEjbXml = Installers.readEntry(coreJar, "default.openejb.conf", alerts);
            if (openEjbXml != null) {
                if (Installers.writeAll(openEjbXmlFile, openEjbXml, alerts)) {
                    alerts.addInfo("Copy openejb.xml to conf");
                }
            }
        }


        //
        // conf/logging.properties
        //
        String openejbLoggingProps = Installers.readEntry(coreJar, "logging.properties", alerts);
        if (openejbLoggingProps != null) {
            File loggingPropsFile = new File(confDir, "logging.properties");
            String newLoggingProps = null;
            if (!loggingPropsFile.exists()) {
                newLoggingProps = openejbLoggingProps;
            } else {
                String loggingPropsOriginal = Installers.readAll(loggingPropsFile, alerts);
                if (!loggingPropsOriginal.toLowerCase().contains("openejb")) {
                    // strip off license header
                    String[] strings = openejbLoggingProps.split("## --*", 3);
                    if (strings.length == 3) {
                        openejbLoggingProps = strings[2];
                    }
                    // append our properties
                    newLoggingProps = loggingPropsOriginal +
                            "\r\n" +
                            "############################################################\r\n" +
                            "# OpenEJB Logging Configuration.\r\n" +
                            "############################################################\r\n" +
                            openejbLoggingProps + "\r\n";
                }
            }
            if (newLoggingProps != null) {
                if (Installers.writeAll(loggingPropsFile, newLoggingProps, alerts)) {
                    alerts.addInfo("Append OpenEJB config to logging.properties");
                }
            }
        }
    }

    public static Object invokeStaticNoArgMethod(String className, String propertyName) {
        try {
            Class<?> clazz = loadClass(className, Installer.class.getClassLoader());
            Method method = clazz.getMethod(propertyName);
            Object result = method.invoke(null, (Object[]) null);
            return result;
        } catch (Throwable e) {
            return null;
        }
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        LinkedList<ClassLoader> loaders = new LinkedList<ClassLoader>();
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            loaders.addFirst(loader);
        }
        for (ClassLoader loader : loaders) {
            try {
                Class<?> clazz = Class.forName(className, true, loader);
                return clazz;
            } catch (ClassNotFoundException e) {
            }
        }
        return null;
    }

}
