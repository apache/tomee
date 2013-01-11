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
package org.apache.tomee.installer;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
        listenerInstalled = "OpenEJBListener".equals(SystemInstance.get().getOptions().get("openejb.embedder.source", "")) ||  "ServerListener".equals(SystemInstance.get().getOptions().get("openejb.embedder.source", ""));

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

        removeTomcatLibJar("annotations-api.jar");
        removeTomcatLibJar("el-api.jar");
        addJavaeeInEndorsed();

        addTomEEAdminConfInTomcatUsers();

        addTomEELinkToTomcatHome();

        if (!alerts.hasErrors()) {
            status = Status.REBOOT_REQUIRED;
        }
    }

    public void addTomEEAdminConfInTomcatUsers() {
        addTomEEAdminConfInTomcatUsers(false);
    }

    public void addTomEEAdminConfInTomcatUsers(boolean securityActivated) {
        // read server.xml
        String tomcatUsersXml = Installers.readAll(paths.getTomcatUsersXml(), alerts);

        // server xml will be null if we couldn't read the file
        if (tomcatUsersXml == null) {
            return;
        }

        if (tomcatUsersXml.contains("tomee-admin")) {
            alerts.addWarning("Can't add tomee user to tomcat-users.xml");
            return;
        }

        // if we can't backup the file, do not modify it
        if (!Installers.backup(paths.getTomcatUsersXml(), alerts)) {
            return;
        }

        // add our listener
        final String roleUserTags =
                "  <role rolename=\"tomee-admin\" />\n" +
                "  <user username=\"tomee\" password=\"tomee\" roles=\"tomee-admin,manager-gui\" />\n";
        String content = null;
        if (!securityActivated) {
            content =
                "  <!-- Activate those lines to get access to TomEE GUI -->\n" +
                "  <!--\n" +
                roleUserTags +
                "  -->\n" +
                "</tomcat-users>\n";
        } else {
            content =
                "  <!-- Activate those lines to get access to TomEE GUI\n -->" +
                roleUserTags +
                "</tomcat-users>\n";

        }
        final String newTomcatUsers = tomcatUsersXml.replace("</tomcat-users>", content);

        // overwrite server.xml
        if (Installers.writeAll(paths.getTomcatUsersXml(), newTomcatUsers, alerts)) {
            alerts.addInfo("Add tomee user to tomcat-users.xml");
        }
    }

    public void installFull() {
        installListener("org.apache.tomee.catalina.ServerListener");

        installJavaagent();

        commentDeploymentDir();
        installConfigFiles();

        removeTomcatLibJar("annotations-api.jar");
        removeTomcatLibJar("el-api.jar");
        addJavaeeInEndorsed();
        moveLibs();

        addTomEEAdminConfInTomcatUsers();

        addTomEELinkToTomcatHome();

        if (!alerts.hasErrors()) {
            status = Status.REBOOT_REQUIRED;
        }
    }

    private void commentDeploymentDir() {
        final File tomeeXml = new File(paths.getCatalinaConfDir(), "tomee.xml");
        if (!tomeeXml.exists()) {
            Installers.writeAll(tomeeXml,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<tomee>\n" +
                "  <!-- see http://tomee.apache.org/containers-and-resources.html -->\n" +
                "</tomee>\n", alerts);
        }
    }

    private void addTomEELinkToTomcatHome() {
        final File home = paths.getHome();
        final String indeJsp = Installers.readAll(home, alerts);
        if (indeJsp == null) {
            return;
        }

        if (!indeJsp.contains("tomcat7Url")) { // check the user didn't replaced the file, can be improved
            alerts.addWarning("webapps/ROOT/index.jsp was modified");
            return;
        }

        final String newIndeJsp = indeJsp.replaceFirst("<div id=\"actions\">",
                "<div id=\\\"actions\\\">\r\n" +
                "                    <div class=\"button\">\n" +
                "                        <a class=\"container shadow\" href=\"/tomee\"><span>TomEE Gui</span></a>\n" +
                "                    </div>");
        Installers.writeAll(home, newIndeJsp, alerts);
    }

    private void moveLibs() {

        final File libs = paths.getCatalinaLibDir();
        final File[] files = paths.getOpenEJBLibDir().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) continue;
                if (!file.getName().endsWith(".jar")) continue;

                try {
                    Installers.copyFile(file, new File(libs, file.getName()));
                    if(!file.delete()){
                        file.deleteOnExit();
                    }
                    alerts.addInfo("Copy " + file.getName() + " to lib");
                } catch (IOException e) {
                    alerts.addError("Unable to " + file.getName() + " to Tomcat lib directory.  This will need to be performed manually.", e);
                }
            }
        }
    }

    private void addJavaeeInEndorsed() {

        File endorsed = new File(paths.getCatalinaHomeDir(), "endorsed");
        if (!endorsed.mkdir()) {
            alerts.addWarning("can't create endorsed directory");
        }


        copyClasses(paths.getJavaEEAPIJar(), new File(endorsed, "annotation-api.jar"), "javax/annotation/.*");

        final File jaxbApi = paths.findOpenEJBJar("geronimo-jaxb_2.2_spec");
        copyClasses(paths.getJavaEEAPIJar(), jaxbApi, new File(endorsed, "jaxb-api.jar"), "javax/xml/bind/.*", Arrays.asList("javax/xml/bind/ContextFinder.class", "javax/xml/bind/DatatypeConverter.class"));
        removeJar(jaxbApi);

        // don't put jaxb-impl in endorsed since it relies on the jvm itself
        final File jaxbImpl = new File(paths.getCatalinaLibDir(), "jaxb-impl.jar");
        if (!jaxbImpl.exists()) {
            try {
                Installers.copyFile(paths.getJAXBImpl(), jaxbImpl);
            } catch (IOException e) {
                alerts.addError("can't copy " + paths.getJAXBImpl().getPath() + " to " + endorsed.getPath() + "/jaxb-impl.jar");
            }
        }

        /* no more needed with the last upgrade of jaxb
        String setClasspathSh = Installers.readAll(paths.getSetClasspathSh(), alerts);
        if (setClasspathSh != null && !setClasspathSh.contains("any endorsed lib for java 7")) {
            if (Installers.backup(paths.getSetClasspathSh(), alerts)) {
                // add our magic bits to the catalina sh file
                final String newSetClasspathSh = setClasspathSh.replace("JAVA_ENDORSED_DIRS=\"$CATALINA_HOME\"/endorsed",
                        "# Don't override the endorsed dir if the user has set it previously\n" +
                        "if [ -z \"$JAVA_ENDORSED_DIRS\" ]; then\n" +
                        "  # Set the default -Djava.endorsed.dirs argument\n" +
                        "  JAVA_ENDORSED_DIRS=\"$CATALINA_HOME\"/endorsed\n" +
                        "  java_version=`$JRE_HOME/bin/java -version 2>&1 | grep version`\n" +
                        "  case \"$java_version\" in \n" +
                        "    *1.7*)\n" +
                        "      JAVA_ENDORSED_DIRS=\"$CATALINA_HOME\"/endorsed7\n" +
                        "    ;;\n" +
                        "  esac\n" +
                        "fi\n");
                if (Installers.writeAll(paths.getSetClasspathSh(), newSetClasspathSh, alerts)) {
                    alerts.addInfo("Endorsed lib set for java 6 and ignored for java 7 (unix)");
                }
            }
        }

        String setClasspathBat = Installers.readAll(paths.getSetClasspathBat(), alerts);
        if (setClasspathBat != null && !setClasspathBat.contains("any endorsed lib for java 7")) {
            if (Installers.backup(paths.getSetClasspathBat(), alerts)) {
                // add our magic bits to the catalina bat file
                // note how windows is not adapted to scripting
                final String newSetClasspathBat = setClasspathBat.replace("set \"JAVA_ENDORSED_DIRS=%CATALINA_HOME%\\endorsed\"",
                        "\nrem Set the default -Djava.endorsed.dirs argument\n" +
                        "rem easier way to get java version in bat is to dump the version in a file\n" +
                        "set JAVA_VERSION_FILE=%CATALINA_HOME%\\tmp_java_version.txt\n" +
                        "\"%JRE_HOME%\\bin\\java\" -version 2> %JAVA_VERSION_FILE%\n" +
                        "set /p JAVA_VERSION= < %JAVA_VERSION_FILE%\n" +
                        "del %JAVA_VERSION_FILE%\n" +
                        "rem extract minor version\n" +
                        "set JAVA_VERSION=%JAVA_VERSION:~16,1%\n" +
                        "\n" +
                        "rem adjust endorsed lib depending on the java version\n" +
                        "set JAVA_ENDORSED_DIRS=endorsed\n" +
                        "if \"%JAVA_VERSION%\" == \"7\" set JAVA_ENDORSED_DIRS=\"%CATALINA_HOME%\"\\endorsed7\n");
                if (Installers.writeAll(paths.getSetClasspathBat(), newSetClasspathBat, alerts)) {
                    alerts.addInfo("Endorsed lib set for java 6 and ignored for java 7 (win)");
                }
            }
        }
        */
    }

    private void copyClasses(final File javaEEAPIJar, final File sourceJar, final File destinationJar, final String pattern, final List<String> exceptions) {
        if (javaEEAPIJar == null) throw new NullPointerException("javaEEAPIJar");
        if (sourceJar == null) throw new NullPointerException("sourceJar");
        if (destinationJar == null) throw new NullPointerException("destinationJar");
        if (pattern == null) throw new NullPointerException("pattern");
        if (exceptions == null) throw new NullPointerException("exceptions");

        if (destinationJar.exists()) return;

        try {

            final ByteArrayOutputStream destinationBuffer = new ByteArrayOutputStream(524288);
            final ZipOutputStream destination = new ZipOutputStream(destinationBuffer);

            final ZipInputStream source = new ZipInputStream(IO.read(sourceJar));
            for (ZipEntry entry; (entry = source.getNextEntry()) != null; ) {
                final String entryName = entry.getName();
                if (!entryName.matches(pattern) || exceptions.contains(entryName)) {
                    continue;
                }

                destination.putNextEntry(new ZipEntry(entryName));

                IO.copy(source, destination);
            }
            IO.close(source);

            final ZipInputStream source2 = new ZipInputStream(IO.read(javaEEAPIJar));
            for (ZipEntry entry; (entry = source2.getNextEntry()) != null; ) {
                final String entryName = entry.getName();
                if (!entryName.matches(pattern) || !exceptions.contains(entryName)) {
                    continue;
                }

                destination.putNextEntry(new ZipEntry(entryName));

                IO.copy(source2, destination);
            }
            IO.close(source2);

            IO.close(destination);

            IO.copy(destinationBuffer.toByteArray(), destinationJar);
        } catch (IOException e) {
            alerts.addError(e.getMessage());
        }
    }

    private void copyClasses(File sourceJar, File destinationJar, String pattern) {
        if (sourceJar == null) throw new NullPointerException("sourceJar");
        if (destinationJar == null) throw new NullPointerException("destinationJar");
        if (pattern == null) throw new NullPointerException("pattern");

        if (destinationJar.exists()) return;

        try {

            final ZipInputStream source = new ZipInputStream(IO.read(sourceJar));

            final ByteArrayOutputStream destinationBuffer = new ByteArrayOutputStream(524288);
            final ZipOutputStream destination = new ZipOutputStream(destinationBuffer);

            for (ZipEntry entry; (entry = source.getNextEntry()) != null; ) {
                String entryName = entry.getName();

                if (!entryName.matches(pattern)) continue;

                destination.putNextEntry(new ZipEntry(entryName));

                IO.copy(source, destination);
            }

            IO.close(source);
            IO.close(destination);

            IO.copy(destinationBuffer.toByteArray(), destinationJar);
        } catch (IOException e) {
            alerts.addError(e.getMessage());
        }
    }

    private void removeJar(final File jar) {
        if (jar.exists()) {
            if (!jar.delete()) {
                jar.deleteOnExit();
            }
            alerts.addInfo("Please restart the server or delete manually " + jar.getName());
        }
    }

    private void removeTomcatLibJar(String name) {
        final File jar = new File(paths.getCatalinaLibDir(), name);
        removeJar(jar);
    }

    public void installListener() {
        installListener("org.apache.tomee.loader.OpenEJBListener");
    }

    public void installListener(final String listener) {
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
        if (serverXmlOriginal.contains(listener)) {
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
                            "  <!-- TomEE plugin for Tomcat -->\r\n" +
                            "  <Listener className=\"" + listener + "\" />");
        } catch (IOException e) {
            alerts.addError("Error while adding listener to server.xml file", e);
        }

        // overwrite server.xml
        if (Installers.writeAll(paths.getServerXmlFile(), newServerXml, alerts)) {
            alerts.addInfo("Add OpenEJB listener to server.xml");
        }
    }

    // NOTE: we specify the jaxbcontext implementation because
    //       we are using geronimo jaxb API and we don't want to go to
    //       the geronimo locator to find the implementation
    //       because it needs some OSGi API we don't want to add
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
     * Installs conf/tomee.xml and conf/logging.properties files.
     * This method retrieves the tomee.xml and logging.properties files
     * from openejb core jar file and installs them under the conf directory
     * of tomcat. if there is already a conf/logging.properties file available
     * then this method appends the contents of openejb logging.properties file
     * to the exisiting properties file.
     *
     * Replace web.xml to set jasper in production mode instead of dev mode.
     *
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
        // conf/tomee.xml
        //

        File openEjbXmlFile = new File(confDir, "tomee.xml");
        if (!openEjbXmlFile.exists()) {
            // read in the openejb.xml file from the openejb core jar
            String openEjbXml = Installers.readEntry(coreJar, "default.openejb.conf", alerts);
            if (openEjbXml != null) {
                if (Installers.writeAll(openEjbXmlFile, openEjbXml.replace("<openejb>", "<tomee>").replace("</openejb>", "</tomee>"), alerts)) {
                    alerts.addInfo("Copy tomee.xml to conf");
                }
            }
        }


        //
        // conf/logging.properties
        // now we are using tomcat one of jdk one by default
        // this test should always fail
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

        final File openejbSystemProperties = new File(confDir, "system.properties");
        if (!openejbSystemProperties.exists()) {
            FileWriter systemPropertiesWriter = null;
            try {
                systemPropertiesWriter = new FileWriter(openejbSystemProperties);

                systemPropertiesWriter.write("# all this properties are added at JVM system properties at startup\n");
                systemPropertiesWriter.write("# here some default Apache TomEE system properties\n");
                systemPropertiesWriter.write("# for more information please see http://openejb.apache.org/properties-listing.html\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# openejb.check.classloader = false\n");
                systemPropertiesWriter.write("# openejb.check.classloader.verbose = false\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# tomee.jaxws.subcontext = webservices\n");
                systemPropertiesWriter.write("# tomee.jaxws.oldsubcontext = false\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# openejb.system.apps = true\n");
                systemPropertiesWriter.write("# openejb.servicemanager.enabled = true\n");
                systemPropertiesWriter.write("# openejb.jmx.active = false\n");
                systemPropertiesWriter.write("# openejb.descriptors.output = false\n");
                systemPropertiesWriter.write("# openejb.strict.interface.declaration = false\n");
                systemPropertiesWriter.write("# openejb.conf.file = conf/tomee.xml\n");
                systemPropertiesWriter.write("# openejb.debuggable-vm-hackery = false\n");
                systemPropertiesWriter.write("# openejb.validation.skip = false\n");
                systemPropertiesWriter.write("# openejb.webservices.enabled = true\n");
                systemPropertiesWriter.write("# openejb.validation.output.level = MEDIUM\n");
                systemPropertiesWriter.write("# openejb.user.mbeans.list = *\n");
                systemPropertiesWriter.write("# openejb.deploymentId.format = {appId}/{ejbJarId}/{ejbName}\n");
                systemPropertiesWriter.write("# openejb.jndiname.format = {deploymentId}{interfaceType.annotationName}\n");
                systemPropertiesWriter.write("# openejb.deployments.package.include = .*\n");
                systemPropertiesWriter.write("# openejb.deployments.package.exclude = \n");
                systemPropertiesWriter.write("# openejb.autocreate.jta-datasource-from-non-jta-one = true\n");
                systemPropertiesWriter.write("# openejb.altdd.prefix = \n");
                systemPropertiesWriter.write("# org.apache.openejb.default.system.interceptors = \n");
                systemPropertiesWriter.write("# openejb.jndiname.failoncollision = true\n");
                systemPropertiesWriter.write("# openejb.wsAddress.format = /{ejbDeploymentId}\n");
                systemPropertiesWriter.write("# org.apache.openejb.server.webservices.saaj.provider = \n");
                systemPropertiesWriter.write("# openejb.nobanner = true\n");
                systemPropertiesWriter.write("# openejb.offline = false\n");
                systemPropertiesWriter.write("# openejb.jmx.active = true\n");
                systemPropertiesWriter.write("# openejb.exclude-include.order = include-exclude\n");
                systemPropertiesWriter.write("# openejb.additional.exclude =\n");
                systemPropertiesWriter.write("# openejb.additional.include =\n");
                systemPropertiesWriter.write("# openejb.crosscontext = false\n");
                systemPropertiesWriter.write("# openejb.jsessionid-support = \n");
                systemPropertiesWriter.write("# openejb.myfaces.disable-default-values = true\n");
                systemPropertiesWriter.write("# openejb.web.xml.major = \n");
                systemPropertiesWriter.write("# openjpa.Log = \n");
                systemPropertiesWriter.write("# openejb.jdbc.log = false\n");
                systemPropertiesWriter.write("# javax.persistence.provider = org.apache.openjpa.persistence.PersistenceProviderImpl\n");
                systemPropertiesWriter.write("# javax.persistence.transactionType = \n");
                systemPropertiesWriter.write("# javax.persistence.jtaDataSource = \n");
                systemPropertiesWriter.write("# javax.persistence.nonJtaDataSource = \n");
            } catch (IOException e) {
                // ignored, this file is far to be mandatory
            } finally {
                if (systemPropertiesWriter != null) {
                    try {
                        systemPropertiesWriter.close();
                    } catch (IOException e) {
                        // no-op
                    }
                }
            }

        }

        //
        // conf/web.xml
        //

        JarFile openejbTomcatCommonJar;
        try {
            openejbTomcatCommonJar = new JarFile(paths.geOpenEJBTomcatCommonJar());
        } catch (IOException e) {
            return;
        }
        File webXmlFile = new File(confDir, "web.xml");
        String webXml = Installers.readEntry(openejbTomcatCommonJar, "conf/web.xml", alerts);
        if (Installers.writeAll(webXmlFile, webXml, alerts)) {
            alerts.addInfo("Set jasper in production mode in TomEE web.xml");
        }
    }

    public static Object invokeStaticNoArgMethod(String className, String propertyName) {
        try {
            Class<?> clazz = loadClass(className, Installer.class.getClassLoader());
            Method method = clazz.getMethod(propertyName);
            return method.invoke(null, (Object[]) null);
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
                return Class.forName(className, true, loader);
            } catch (ClassNotFoundException e) {
                // no-op
            }
        }
        return null;
    }

}
