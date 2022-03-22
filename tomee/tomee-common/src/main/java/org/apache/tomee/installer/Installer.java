/*
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

import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.JarFile;

public class Installer implements InstallerInterface {
    private final Alerts alerts = new Alerts();

    private final Paths paths;
    private Status status = Status.NONE;
    private boolean force;
    private Map<String, String> properties;

    private static final boolean LISTENER_INSTALLED;
    private static final boolean AGENT_INSTALLED;

    static {
        final Options opts = SystemInstance.get().getOptions();
        // is the OpenEJB listener installed
        LISTENER_INSTALLED = "OpenEJBListener".equals(opts.get("openejb.embedder.source", ""))
                || "ServerListener".equals(opts.get("openejb.embedder.source", ""));

        // is the OpenEJB javaagent installed
        AGENT_INSTALLED = InstallerTools.invokeStaticNoArgMethod(
                "org.apache.openejb.javaagent.Agent", "getInstrumentation") != null;
    }

    public static boolean isListenerInstalled() {
        return LISTENER_INSTALLED;
    }

    public static boolean isAgentInstalled() {
        return AGENT_INSTALLED;
    }

    public Installer(final Paths paths) {
        this.paths = paths;

        if (LISTENER_INSTALLED && AGENT_INSTALLED) {
            status = Status.INSTALLED;
        }

        this.properties = new HashMap<>();
    }

    public Installer(final Paths paths, final boolean force) {
        this(paths);
        this.force = force;
    }

    public Installer(final Paths paths, final Map<String, String> properties, final boolean force) {
        this(paths, force);
        this.properties = properties;
    }

    @Override
    public PathsInterface getPaths() {
        return paths;
    }

    @Override
    public Alerts getAlerts() {
        return alerts;
    }

    @Override
    public void reset() {
        alerts.reset();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void installAll() {
        installListener();
        installJavaagent();
        installConfigFiles(false);

        removeTomcatLibJar("annotations-api.jar");
        // addJavaeeInEndorsed();
        addTomEEJuli();

        addTomEEAdminConfInTomcatUsers();
        // addTomEELinkToTomcatHome(); // we don't provide tomee GUI anymore

        workaroundOnBat();

        if (!alerts.hasErrors()) {
            status = Status.REBOOT_REQUIRED;
        }
    }

    // switch tomcat-juli with tomee-juli
    // we keep the same name to let all tomcat tooling work as expected
    private void addTomEEJuli() {
        final File original = new File(paths.getCatalinaBinDir(), "tomcat-juli.jar");

        final File juli = paths.findOpenEJBJar("tomee-juli");
        try {
            Installers.copyFile(juli, new File(original.getAbsolutePath()));
            if (!juli.delete()) { // remove original
                juli.deleteOnExit();
            }
        } catch (final IOException e) {
            alerts.addInfo("Add tomee user to tomcat-users.xml");
        }
    }

    public void addTomEEAdminConfInTomcatUsers() {
        addTomEEAdminConfInTomcatUsers(false);
    }

    public void addTomEEAdminConfInTomcatUsers(final boolean securityActivated) {
        // read server.xml
        final String tomcatUsersXml = Installers.readAll(paths.getTomcatUsersXml(), alerts);

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
        final String content;
        if (!securityActivated) {
            content =
                    "  <!-- Activate those lines to get access to TomEE GUI if added (tomee-webaccess) -->\n" +
                            "  <!--\n" +
                            roleUserTags +
                            "  -->\n" +
                            "</tomcat-users>\n";
        } else {
            content =
                    "  <!-- Activate those lines to get access to TomEE GUI if added (tomee-webaccess)\n -->" +
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
        installConfigFiles(true);

        removeTomcatLibJar("annotations-api.jar");
        // addJavaeeInEndorsed();
        addTomEEJuli(); // before moveLibs
        moveLibs();

        addTomEEAdminConfInTomcatUsers();
        // addTomEELinkToTomcatHome(); // we don't provide it anymore

        workaroundOnBat();

        if (!alerts.hasErrors()) {
            status = Status.REBOOT_REQUIRED;
        }
    }

    private void workaroundOnBat() {
        final File setclasspath = new File(paths.getCatalinaBinDir(), "setclasspath.bat");
        String bat = Installers.readAll(setclasspath, alerts);
        if (bat == null || bat.contains(" NOT DEFINED ") /* already done, tomcat doesnt use yet this new Windows NT 4 syntax */) {
            return;
        }

        // add our magic bits to the catalina bat file
        bat = bat // could be regex but here the diff is explicit which is better IMO
                .replace("not \"%JRE_HOME%\" == \"\"", "DEFINED JRE_HOME")
                .replace("not \"%JAVA_HOME%\" == \"\"", "DEFINED JAVA_HOME")
                .replace("not \"%_RUNJAVA%\" == \"\"", "DEFINED _RUNJAVA")
                .replace("not \"%_RUNJDB%\" == \"\"", "DEFINED _RUNJDB")
                .replace("\"%JAVA_HOME%\" == \"\"", "NOT DEFINED JAVA_HOME");

        // overwrite the catalina.bat file
        if (!Installers.writeAll(setclasspath, bat, alerts)) {
            alerts.addInfo("Can't add workarounds for setclasspath.bat");
        }
    }

    private void removeTomcatLibJar(final String name) {
        final File jar = new File(paths.getCatalinaLibDir(), name);
        removeJar(jar);
    }

    private void commentDeploymentDir() {
        final File tomeeXml = new File(paths.getCatalinaConfDir(), "tomee.xml");
        if (!tomeeXml.exists()) {
            Installers.writeAll(tomeeXml,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!--\n" +
                            "    Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                            "    contributor license agreements.  See the NOTICE file distributed with\n" +
                            "    this work for additional information regarding copyright ownership.\n" +
                            "    The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                            "    (the \"License\"); you may not use this file except in compliance with\n" +
                            "    the License.  You may obtain a copy of the License at\n" +
                            "\n" +
                            "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "    Unless required by applicable law or agreed to in writing, software\n" +
                            "    distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "    See the License for the specific language governing permissions and\n" +
                            "    limitations under the License.\n" +
                            "-->\n" +
                            "<tomee>\n" +
                            "  <!-- see https://tomee.apache.org/latest/docs/admin/configuration/containers.html -->\n\n" +
                            "  <!-- activate next line to be able to deploy applications in apps -->\n" +
                            "  <!-- <Deployments dir=\"apps\" /> -->\n" +
                            "</tomee>\n", alerts);
        }
    }

    /*
    private void addTomEELinkToTomcatHome() {
        final File home = paths.getHome();
        if(!home.exists()) {
            return;
        }
        final String indeJsp = Installers.readAll(home, alerts);
        if (indeJsp == null) {
            return;
        }

        if (!indeJsp.contains("tomcatUrl")) { // check the user didn't replaced the file, can be improved
            alerts.addWarning("webapps/ROOT/index.jsp was modified");
            return;
        }

        final String newIndeJsp = indeJsp.replaceFirst("<div id=\"actions\">",
                "<div id=\"actions\">\n" +
                        "                    <div class=\"button\">\n" +
                        "                        <a class=\"container shadow\" href=\"/tomee\"><span>TomEE Gui</span></a>\n" +
                        "                    </div>");
        Installers.writeAll(home, newIndeJsp, alerts);
    }
*/

    private void moveLibs() {

        /*
         * When there are several SNAPSHOT versions of a jar available, Maven will often copy
         * the jar into the assembly as openejb-core-8.0.7-20210418.032600-163.jar rather than
         * openejb-core-8.0.7-SNAPSHOT.jar.  This breaks our TCK setup which expects it can
         * point at jars like "lib/openejb-core-$version.jar", where $version is something like
         * "8.0.7-SNAPSHOT".
         *
         * If we see that for any jar containing our version we will rename it from the date
         * stamped version to the "-SNAPSHOT" version.
         */

        Function<String, String> normalizeVersion = Function.identity();
        if (properties.containsKey("remove.datestamp")) {
            final String removeDatestamp = properties.get("remove.datestamp");
            final String[] strings = removeDatestamp.split(" *, *");
            for (final String string : strings) {
                if (!string.contains("-SNAPSHOT")) continue;
                normalizeVersion = normalizeVersion.andThen(removeDatestamp(string.trim()));
            }
        }

        /*
         * In TomEE 9 we produce the final server zip by bytecode transforming the classes
         * so they use jakarta instead of javax.  We do this by inputting the TomEE 8.x
         * jar and running them through a transformer and creating a new TomEE 9.x zip.
         *
         * In this process we also want to rename any libraries from 8.x to 9.x
         */
        if (properties.containsKey("rename.version")) {
            final String renameVersion = properties.get("rename.version");

            final String[] split = renameVersion.split(" *, *");
            if (split.length != 2) {
                throw new IllegalStateException("rename.version should be in the form of " +
                        "'version1, version2' for example '8.0.7, 9.0.0'.  Found '" + renameVersion + "'");
            }

            final String from = split[0].trim();
            final String to = split[1].trim();

            normalizeVersion = normalizeVersion
                    .andThen(removeDatestamp(from))
                    .andThen(removeDatestamp(to))
                    .andThen(changeVersion(from, to));

        }


        final File libs = paths.getCatalinaLibDir();
        final File[] files = paths.getOpenEJBLibDir().listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                final String name = normalizeVersion.apply(file.getName());

                if (!name.endsWith(".jar")) {
                    continue;
                }

                try {
                    Installers.copyFile(file, new File(libs, name));
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                    alerts.addInfo("Copy " + name + " to lib");
                } catch (final IOException e) {
                    alerts.addError("Unable to " + name + " to Tomcat lib directory.  This will need to be " +
                            "performed manually.", e);
                }
            }
        }
    }

    public static Function<String, String> removeDatestamp(final String version) {
        return jarName -> removeDatestamp(version, jarName);
    }

    public static Function<String, String> changeVersion(final String from, final String to) {
        return jarName -> {
            if (!jarName.endsWith(from + ".jar")) return jarName;
            return jarName.replace(from + ".jar", to + ".jar");
        };
    }

    /**
     * Maven will occasionally give a datestamped version of a snapshot.  Our TCK
     * test harness and likely tooling others have expects the version number to
     * be predictable ("8.0.7-SNAPSHOT" or "8.0.5") so it can build paths without
     * fancy logic, i.e. a simple "openejb-core-" + version +" .jar"
     *
     * This doesn't work if the version number essentially contains a random string.
     *
     * If we see any Maven datestamps on our jars where we are expecting SNAPSHOT,
     * rename the jar to the expected "-SNAPSHOT" name.
     */
    public static String removeDatestamp(final String version, final String jarName) {
        if (!version.contains("-SNAPSHOT")) return jarName;

        final String versionNumber = version.replaceAll("-SNAPSHOT", "");
        if (!jarName.contains(versionNumber)) return jarName;

        // Replace 8.0.7-20210418.035728-165 with 8.0.7-SNAPSHOT

        final String regex = ""
                // turn 8.0.7 into 8\.0\.7
                + versionNumber.replace(".", "\\.")
                // replace the 'd' with '[0-9]'
                + "-dddddddd.dddddd-ddd".replace("d", "[0-9]");

        return jarName.replaceAll(regex, version);
    }
    /*
    private void addJavaeeInEndorsed() {
        final File endorsed = new File(paths.getCatalinaHomeDir(), "endorsed");
        if (!endorsed.mkdir()) {
            alerts.addWarning("can't create endorsed directory");
        }

        final File jaxbApi = paths.findOpenEJBJar("geronimo-jaxb_2.2_spec");
        copyClasses(paths.getJavaEEAPIJar(), jaxbApi, new File(endorsed, "jaxb-api.jar"), "javax/xml/bind/.*",
                Arrays.asList("javax/xml/bind/ContextFinder.class", "javax/xml/bind/DatatypeConverter.class"));
        removeJar(jaxbApi);

        // don't put jaxb-impl in endorsed since it relies on the jvm itself
        final File jaxbImpl = new File(paths.getCatalinaLibDir(), "jaxb-impl.jar");
        if (!jaxbImpl.exists()) {
            try {
                Installers.copyFile(paths.getJAXBImpl(), jaxbImpl);
            } catch (final IOException e) {
                alerts.addError("can't copy " + paths.getJAXBImpl().getPath() + " to " + endorsed.getPath() + "/jaxb-impl.jar");
            }
        }
    }


    private void copyClasses(final File javaEEAPIJar, final File sourceJar, final File destinationJar,
                             final String pattern, final List<String> exceptions) {
        if (javaEEAPIJar == null) {
            throw new NullPointerException("javaEEAPIJar");
        }
        if (sourceJar == null) {
            throw new NullPointerException("sourceJar");
        }
        if (destinationJar == null) {
            throw new NullPointerException("destinationJar");
        }
        if (pattern == null) {
            throw new NullPointerException("pattern");
        }
        if (exceptions == null) {
            throw new NullPointerException("exceptions");
        }

        if (destinationJar.exists()) {
            return;
        }

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
        } catch (final IOException e) {
            alerts.addError(e.getMessage());
        }
    }
    */

    private void removeJar(final File jar) {
        if (jar.exists()) {
            if (!jar.delete()) {
                jar.deleteOnExit();
            }
            alerts.addInfo("Please restart the server or delete manually " + jar.getName());
        }
    }

    public void installListener() {
        installListener("org.apache.tomee.loader.OpenEJBListener");
    }

    public void installListener(final String listener) {
        if (LISTENER_INSTALLED && !force) {
            // OpenEJB Listener already installed
            return;
        }
        boolean copyOpenEJBLoader = true;

        // copy loader jar to lib
        final File destination = new File(paths.getCatalinaLibDir(), paths.getOpenEJBTomcatLoaderJar().getName());
        if (destination.exists()) {
            if (paths.getOpenEJBTomcatLoaderJar().length() == destination.length()) {
                copyOpenEJBLoader = false;
            }
        }
        if (copyOpenEJBLoader) {
            try {
                Installers.copyFile(paths.getOpenEJBTomcatLoaderJar(), destination);
                alerts.addInfo("Copy " + paths.getOpenEJBTomcatLoaderJar().getName() + " to lib");
            } catch (final IOException e) {
                alerts.addError("Unable to copy OpenEJB Tomcat loader jar to Tomcat lib directory.  This will need to be performed manually.", e);
            }
        }

        // read server.xml
        final String serverXmlOriginal = Installers.readAll(paths.getServerXmlFile(), alerts);

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
        } catch (final IOException e) {
            alerts.addError("Error while adding listener to server.xml file", e);
        }

        //Add TomEE header
        try {
            newServerXml = Installers.replace(newServerXml,
                    "<Connector port=\"8080\"",
                    "<Connector port=\"8080\"",
                    "/>",
                    "xpoweredBy=\"false\" server=\"Apache TomEE\" />");

            newServerXml = Installers.replace(newServerXml,
                    "<Connector port=\"8443\"",
                    "<Connector port=\"8443\"",
                    ">",
                    " xpoweredBy=\"false\" server=\"Apache TomEE\" >");
        } catch (final IOException e) {
            alerts.addError("Error adding server attribute to server.xml file", e);
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
        if (AGENT_INSTALLED && !force) {
            // OpenEJB Agent already installed"
            return;
        }

        //
        // Copy openejb-javaagent.jar to lib
        //
        boolean copyJavaagentJar = true;
        final File javaagentJar = new File(paths.getCatalinaLibDir(), "openejb-javaagent.jar");
        if (javaagentJar.exists()) {
            if (paths.getOpenEJBJavaagentJar().length() == javaagentJar.length()) {
                copyJavaagentJar = false;
            }
        }

        if (copyJavaagentJar) {
            try {
                Installers.copyFile(paths.getOpenEJBJavaagentJar(), javaagentJar);
                alerts.addInfo("Copy " + paths.getOpenEJBJavaagentJar().getName() + " to lib");
            } catch (final IOException e) {
                alerts.addError("Unable to copy OpenEJB javaagent jar to Tomcat lib directory.  This will need to be performed manually.", e);
            }
        }


        //
        // bin/catalina.sh
        //

        // read the catalina sh file
        final String catalinaShOriginal = Installers.readAll(paths.getCatalinaShFile(), alerts);

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
        final String newCatalinaSh = catalinaShOriginal.replace("# ----- Execute The Requested Command",
                "# Add OpenEJB javaagent\n" +
                        "if [ -r \"$CATALINA_HOME\"/" + openejbJavaagentPath + " ]; then\n" +
                        "  JAVA_OPTS=\"\\\"-javaagent:$CATALINA_HOME/" + openejbJavaagentPath + "\\\" $JAVA_OPTS\"\n" +
                        "fi\n" +
                        "\n" +
                        "# ----- Execute The Requested Command");

        // overwrite the catalina.sh file
        if (Installers.writeAll(paths.getCatalinaShFile(), newCatalinaSh, alerts)) {
            alerts.addInfo("Add OpenEJB JavaAgent to catalina.sh");
        }

        boolean isCatalinaShExecutable = paths.getCatalinaShFile().canExecute();
        if (!isCatalinaShExecutable) {
            try {
                isCatalinaShExecutable = paths.getCatalinaShFile().setExecutable(true);
            } catch (final SecurityException e) {
                alerts.addWarning("Cannot change CatalinaSh executable attribute.");
            }
        }
        if (!isCatalinaShExecutable) {
            alerts.addWarning("CatalinaSh is not executable.");
        }

        //
        // bin/catalina.bat
        //

        // read the catalina bat file
        final String catalinaBatOriginal = Installers.readAll(paths.getCatalinaBatFile(), alerts);

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
        final String newCatalinaBat = catalinaBatOriginal.replace("rem ----- Execute The Requested Command",
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
     * @param builtIn
     */
    public void installConfigFiles(final boolean builtIn) {
        final File openejbCoreJar = paths.getOpenEJBCoreJar();
        final File confDir = paths.getCatalinaConfDir();
        final Alerts alerts = this.alerts;

        if (openejbCoreJar == null) {
            // the core jar contains the config files
            return;
        }
        try (final JarFile coreJar = new JarFile(openejbCoreJar)) {
            //
            // conf/tomee.xml
            //
            final File openEjbXmlFile = new File(confDir, "tomee.xml");
            if (!openEjbXmlFile.exists()) {
                // read in the openejb.xml file from the openejb core jar
                final String openEjbXml = Installers.readEntry(coreJar, "default.openejb.conf", alerts);
                if (openEjbXml != null) {
                    if (Installers.writeAll(openEjbXmlFile, openEjbXml.replace("<openejb>", "<tomee>").replace("</openejb>", "</tomee>"), alerts)) {
                        alerts.addInfo("Copy tomee.xml to conf");
                    }
                }
            }
        } catch (final IOException e) {
            return;
        }

        // conf/logging.properties
        // now we are using tomcat one of jdk one by default
        //
        final String openejbLoggingProps = "################################\r\n" +
                "# OpenEJB/TomEE specific loggers\r\n" +
                "################################\r\n" +
                "#\r\n" +
                "# ACTIVATE LEVEL/HANDLERS YOU WANT\r\n" +
                "# IF YOU ACTIVATE 5tomee.org.apache.juli.FileHandler\r\n" +
                "# ADD IT TO handlers LINE LIKE:\r\n" +
                "#\r\n" +
                "# handlers = 1catalina.org.apache.juli.FileHandler, 2localhost.org.apache.juli.FileHandler, 3manager.org.apache.juli.FileHandler, 4host-manager.org.apache.juli.FileHandler, 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "#\r\n" +
                "# LEVELS:\r\n" +
                "# =======\r\n" +
                "#\r\n" +
                "# OpenEJB.level             = WARNING\r\n" +
                "# OpenEJB.options.level     = INFO\r\n" +
                "# OpenEJB.server.level      = INFO\r\n" +
                "# OpenEJB.startup.level     = INFO\r\n" +
                "# OpenEJB.startup.service.level = WARNING\r\n" +
                "# OpenEJB.startup.config.level = INFO\r\n" +
                "# OpenEJB.hsql.level        = INFO\r\n" +
                "# CORBA-Adapter.level       = WARNING\r\n" +
                "# Transaction.level         = WARNING\r\n" +
                "# org.apache.activemq.level = SEVERE\r\n" +
                "# org.apache.geronimo.level = SEVERE\r\n" +
                "# openjpa.level             = WARNING\r\n" +
                "# OpenEJB.cdi.level         = INFO\r\n" +
                "# org.apache.webbeans.level = INFO\r\n" +
                "# org.apache.openejb.level = FINE\r\n" +
                "#\r\n" +
                "# HANDLERS:\r\n" +
                "# =========\r\n" +
                "#\r\n" +
                "# OpenEJB.handlers             = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.options.handlers     = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.server.handlers      = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.startup.handlers     = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.startup.service.handlers = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.startup.config.handlers = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.hsql.handlers        = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# CORBA-Adapter.handlers       = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# Transaction.handlers         = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# org.apache.activemq.handlers = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# org.apache.geronimo.handlers = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# openjpa.handlers             = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# OpenEJB.cdi.handlers         = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# org.apache.webbeans.handlers = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "# org.apache.openejb.handlers = 5tomee.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler\r\n" +
                "#\r\n" +
                "# TOMEE HANDLER SAMPLE:\r\n" +
                "# =====================\r\n" +
                "#\r\n" +
                "# 5tomee.org.apache.juli.FileHandler.level = FINEST\r\n" +
                "# 5tomee.org.apache.juli.FileHandler.directory = ${catalina.base}/logs\r\n" +
                "# 5tomee.org.apache.juli.FileHandler.prefix = tomee.\r\n";
        final File loggingPropsFile = new File(confDir, "logging.properties");
        String newLoggingProps = null;
        if (!loggingPropsFile.exists()) {
            newLoggingProps = openejbLoggingProps;
        } else {
            final String loggingPropsOriginal = Installers.readAll(loggingPropsFile, alerts);
            if (!loggingPropsOriginal.toLowerCase().contains("openejb")) {
                // append our properties
                newLoggingProps = loggingPropsOriginal +
                        "\r\n\r\n" +
                        openejbLoggingProps + "\r\n";
            }
        }
        if (builtIn) {
            installTomEEJuli(alerts, loggingPropsFile, newLoggingProps);
        }

        final File openejbSystemProperties = new File(confDir, "system.properties");
        if (!openejbSystemProperties.exists()) {
            FileWriter systemPropertiesWriter = null;
            try {
                systemPropertiesWriter = new FileWriter(openejbSystemProperties);

                systemPropertiesWriter.write("# Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                        "# contributor license agreements.  See the NOTICE file distributed with\n" +
                        "# this work for additional information regarding copyright ownership.\n" +
                        "# The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                        "# (the \"License\"); you may not use this file except in compliance with\n" +
                        "# the License.  You may obtain a copy of the License at\n" +
                        "#\n" +
                        "#     http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "#\n" +
                        "# Unless required by applicable law or agreed to in writing, software\n" +
                        "# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "# See the License for the specific language governing permissions and\n" +
                        "# limitations under the License.\n" +
                        "\n");


                systemPropertiesWriter.write("# all this properties are added at JVM system properties at startup\n");
                systemPropertiesWriter.write("# here some default Apache TomEE system properties\n");
                systemPropertiesWriter.write("# for more information please see http://tomee.apache.org/properties-listing.html\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write(
                        "# allowed packages to be deserialized, by security we denied all by default, " +
                                "tune tomee.serialization.class.whitelist packages to change it\n");
                systemPropertiesWriter.write("# tomee.remote.support = true\n");
                systemPropertiesWriter.write("tomee.serialization.class.blacklist = *\n");
                systemPropertiesWriter.write("# tomee.serialization.class.whitelist = my.package\n");

                systemPropertiesWriter.write("# Johnzon prevents too big string to be unserialized by default\n");
                systemPropertiesWriter.write("# You can either configure it by Mapper/Parser instance or globally\n");
                systemPropertiesWriter.write("# With this property:\n");
                systemPropertiesWriter.write("# org.apache.johnzon.max-string-length = 8192\n");


                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# Should a jar with at least one EJB activate CDI for this module?\n");
                systemPropertiesWriter.write("# Spec says so but this can imply more (permgen) memory usage\n");
                systemPropertiesWriter.write("# openejb.cdi.activated-on-ejb = true\n");


                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# openejb.check.classloader = false\n");
                systemPropertiesWriter.write("# openejb.check.classloader.verbose = false\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# Activate EE default resources (ManagedExecutorService, JMSConnectionFactory if JMS is there...)");
                systemPropertiesWriter.write("openejb.environment.default = true\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# tomee.jaxws.subcontext = webservices\n");
                systemPropertiesWriter.write("# tomee.jaxws.oldsubcontext = false\n");

                systemPropertiesWriter.write("\n");
                systemPropertiesWriter.write("# if you want to propagate a deployment on a cluster when a tomcat cluster is defined\n");
                systemPropertiesWriter.write("# tomee.cluster.deployment = false\n");

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

                systemPropertiesWriter.write("#\n");
                systemPropertiesWriter.write("# Properties for JAS RS\n");
                systemPropertiesWriter.write("# openejb.jaxrs.application = \n");
                systemPropertiesWriter.write("# openejb.cxf-rs.wadl-generator.ignoreRequests = false\n");
                systemPropertiesWriter.write("# openejb.cxf-rs.wadl-generator.ignoreMessageWriters = true\n");
                systemPropertiesWriter.write("# Replace the Jonhzon JSON Providers with the following classes [comma seperated, no spaces]\n");
                systemPropertiesWriter.write("# openejb.jaxrs.jsonProviders =\n");

                systemPropertiesWriter.write("#\n");
                systemPropertiesWriter.write("# These properties are only for cxf service (SOAP webservices) and TomEE+\n");
                systemPropertiesWriter.write("# If you don't use special tricks and sun default implementation, uncommenting these 4 lines forces TomEE to use it without overhead at all = \n");
                systemPropertiesWriter.write("# javax.xml.soap.MessageFactory = com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl\n");
                systemPropertiesWriter.write("# javax.xml.soap.SOAPFactory = com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl\n");
                systemPropertiesWriter.write("# javax.xml.soap.SOAPConnectionFactory = com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory\n");
                systemPropertiesWriter.write("# javax.xml.soap.MetaFactory = com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl\n");

                final String flavour = properties.getOrDefault("tomee.webapp", "");
                if (flavour.contains("microprofile")) {
                    systemPropertiesWriter.write("#\n");
                    systemPropertiesWriter.write("# MicroProfile\n");
                    systemPropertiesWriter.write("tomee.mp.scan = all\n");
                }

            } catch (final IOException e) {
                // ignored, this file is far to be mandatory
            } finally {
                if (systemPropertiesWriter != null) {
                    try {
                        systemPropertiesWriter.close();
                    } catch (final IOException e) {
                        // no-op
                    }
                }
            }
        }

        //
        // conf/web.xml
        //
        try (final JarFile openejbTomcatCommonJar = new JarFile(paths.geOpenEJBTomcatCommonJar())) {
            final File webXmlFile = new File(confDir, "web.xml");
            final String webXml = Installers.readEntry(openejbTomcatCommonJar, "conf/web.xml", alerts);
            if (Installers.writeAll(webXmlFile, webXml, alerts)) {
                alerts.addInfo("Set jasper in production mode in TomEE web.xml");
            }
        } catch (final IOException e) {
            // no-op
        }

        //
        // conf/catalina.policy
        //

        // if we can't backup the file, do not modify it
        if (!Installers.backup(paths.getCatalinaPolicy() , alerts)) {
            return;
        }

        String catalinaPolicy = Installers.readAll(paths.getCatalinaPolicy(), alerts);

        // catalina.policy will be null if we couldn't read the file
        if (catalinaPolicy == null) {
            return;
        }

        //Add TomEE-specific policies (see TOMEE-3840)
        try {
            catalinaPolicy = Installers.replace(catalinaPolicy,
                    "        permission java.util.PropertyPermission \"org.apache.juli.ClassLoaderLogManager.debug\", \"read\";",
                    "        permission java.util.PropertyPermission \"org.apache.juli.ClassLoaderLogManager.debug\", \"read\";",
                    "        permission java.util.PropertyPermission \"catalina.base\", \"read\";",
                    "        permission java.util.PropertyPermission \"catalina.base\", \"read\";\n\n" +
                            "        // TOMEE-3840\n" +
                            "        permission java.util.PropertyPermission \"tomee.skip-tomcat-log\", \"read\";\n" +
                            "        permission java.lang.RuntimePermission \"accessDeclaredMembers\";\n");

        } catch (final IOException e) {
            alerts.addError("Error adding TomEE specific policies to catalina.policy file", e);
        }

        // overwrite catalina.policy
        if (Installers.writeAll(paths.getCatalinaPolicy(), catalinaPolicy, alerts)) {
            alerts.addInfo("Add TomEE specific policies to catalina.policy");
        }


    }

    private void installTomEEJuli(final Alerts alerts, final File loggingPropsFile, final String newLoggingProps) {
        if (newLoggingProps != null && Installers.writeAll(
                loggingPropsFile,
                newLoggingProps.replace("java.util.logging.ConsoleHandler", "org.apache.tomee.jul.formatter.AsyncConsoleHandler"),
                alerts)) {
            alerts.addInfo("Append OpenEJB config to logging.properties");
        }
    }
}
