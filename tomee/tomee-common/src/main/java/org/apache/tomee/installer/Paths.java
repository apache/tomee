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

import org.apache.openejb.jpa.integration.MakeTxLookup;
import org.apache.openejb.loader.JarLocation;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
/**
 * This class is used to verify that all the main directories and files exist.
 * @see #verify() for more details
 * 
 *
 */
public class Paths implements PathsInterface {

    /**
     * The openejb webapp directory under <<tomcat-install>>/webapps
     */
    private final File openejbWarDir;
    /**
     * Used to store errors which represent verification failures
     * @see #verify()
     */
    private final List<String> errors = new ArrayList<>();
    /**
     * The directory represented by the catalina.home system property
     */
    private File catalinaHomeDir;
    /**
     * The directory represented by the catalina.base system property
     */
    private File catalinaBaseDir;
    /**
     * The <<tomcat-install>>/conf/server.xml file
     */
    private File serverXmlFile;

    private File openEJBWebLibDir;

    private File tomcatUsersXml;

    public Paths(final File openejbWarDir) {
        this.openejbWarDir = openejbWarDir;
    }
    /**
     * Returns the directory represented by the catalina.home system property
     * @return The directory represented by the catalina.home system property
     */
    @Override
    public File getCatalinaHomeDir() {
        if (catalinaHomeDir == null) {
            final String catalinaHome = System.getProperty("catalina.home");
            if (catalinaHome != null) {
                catalinaHomeDir = new File(catalinaHome);
            }
        }
        return catalinaHomeDir;
    }
    /**
     * Sets the catalina home directory
     * @param catalinaHomeDir the absolute path of the catalina home directory
     */
    @Override
    public void setCatalinaHomeDir(final String catalinaHomeDir) {
        this.catalinaHomeDir = createFile(catalinaHomeDir);
    }
    /**
     * Sets the catalina home directory
     * @param catalinaHomeDir the file representing the absolute path of the catalina home directory
     */
    @Override
    public void setCatalinaHomeDir(final File catalinaHomeDir) {
        this.catalinaHomeDir = catalinaHomeDir;
    }
    /**
     * Returns the directory represented by the catalina.base system property
     * @return The directory represented by the catalina.base system property
     */
    @Override
    public File getCatalinaBaseDir() {
        if (catalinaBaseDir == null) {
            final String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                catalinaBaseDir = new File(catalinaBase);
            }
        }
        return catalinaBaseDir;
    }
    /**
     * Sets the catalina base directory
     * @param catalinaBaseDir the absolute path of the catalina base directory
     */
    @Override
    public void setCatalinaBaseDir(final String catalinaBaseDir) {
        setCatalinaBaseDir(createFile(catalinaBaseDir));
    }
    /**
     * Sets the catalina base directory
     * @param catalinaBaseDir the file representing the absolute path of the catalina base directory
     */
    @Override
    public void setCatalinaBaseDir(final File catalinaBaseDir) {
        this.catalinaBaseDir = catalinaBaseDir;
    }
    /**
     * Returns the file representing <<tomcat-install>>/conf/server.xml
     * @return the file representing <<tomcat-install>>/conf/server.xml
     */
    @Override
    public File getServerXmlFile() {
        if (serverXmlFile == null) {
            final File confdir = getCatalinaConfDir();

            if (confdir == null) {
                return null;
            }

            serverXmlFile = new File(confdir, "server.xml");
        }
        return serverXmlFile;
    }

    @Override
    public File getHome() {
        return new File(getCatalinaBaseDir(), "webapps/ROOT/index.jsp");
    }

    /**
     * Sets the server.xml file
     * @param serverXmlFile the absolute path of the server.xml file
     */
    @Override
    public void setServerXmlFile(final String serverXmlFile) {
        this.serverXmlFile = createFile(serverXmlFile);
    }
    /**
     * Sets the server.xml file
     * @param serverXmlFile the file representing the absolute path of the server.xml file
     */    
    @Override
    public void setServerXmlFile(final File serverXmlFile) {
        this.serverXmlFile = serverXmlFile;
    }
    /**
     * Returns the directory representing {@link #catalinaHomeDir}/lib for Tomcat 6. For Tomcat 5.x it returns {@link #catalinaHomeDir}/server/lib
     * @return the directory representing {@link #catalinaHomeDir}/lib
     */
    @Override
    public File getCatalinaLibDir() {
        final File catalinaHomeDir = getCatalinaHomeDir();

        if (catalinaHomeDir == null) {
            return null;
        }

        return new File(catalinaHomeDir, "lib");
    }
    /**
     * Returns the directory representing {@link #catalinaBaseDir}/conf
     * @return the directory representing {@link #catalinaBaseDir}/conf
     */
    @Override
    public File getCatalinaConfDir() {
        final File catalinaBaseDir = getCatalinaBaseDir();

        if (catalinaBaseDir == null) {
            return null;
        }

        return new File(catalinaBaseDir, "conf");
    }
    /**
     * Returns the directory representing {@link #catalinaHomeDir}/bin
     * @return the directory representing {@link #catalinaHomeDir}/bin
     */
    @Override
    public File getCatalinaBinDir() {
        final File catalinaHomeDir = getCatalinaHomeDir();

        if (catalinaHomeDir == null) {
            return null;
        }

        return new File(catalinaHomeDir, "bin");
    }
    /**
     * Returns the {@link #getCatalinaBinDir()}/catalina.sh file
     * @return the {@link #getCatalinaBinDir()}/catalina.sh file
     */
    @Override
    public File getCatalinaShFile() {
        final File binDir = getCatalinaBinDir();

        if (binDir == null) {
            return null;
        }

        return new File(binDir, "catalina.sh");
    }
    /**
     * Returns the {@link #getCatalinaBinDir()}/catalina.bat file
     * @return the {@link #getCatalinaBinDir()}/catalina.bat file
     */
    @Override
    public File getCatalinaBatFile() {
        final File binDir = getCatalinaBinDir();

        if (binDir == null) {
            return null;
        }

        return new File(binDir, "catalina.bat");
    }
    /**
     * Returns the {@link #openejbWarDir}/lib directory.
     * Returns null if {@link #openejbWarDir} is null
     * @return the {@link #openejbWarDir}/lib directory
     */
    @Override
    public File getOpenEJBLibDir() {
        if (openejbWarDir == null) {
            return null;
        }

        return new File(openejbWarDir, "lib");
    }
    /**
     * Returns the tomee-loader.jar file
     * @return the tomee-loader.jar file
     */
    @Override
    public File getOpenEJBTomcatLoaderJar() {
        return findOpenEJBJar("tomee-loader");
    }

    @Override
    public File getJavaEEAPIJar() {
        return findOpenEJBJar("jakartaee-api");
    }

    /**
     * Returns the openejb-javaagent.jar file
     * @return the openejb-javaagent.jar file
     */
    @Override
    public File getOpenEJBJavaagentJar() {
        return findOpenEJBJar("openejb-javaagent");
    }
    /**
     * Returns the openejb-core.jar file
     * @return the openejb-core.jar file
     */
    @Override
    public File getOpenEJBCoreJar() {
        return findOpenEJBJar("openejb-core");
    }

    @Override
    public File geOpenEJBTomcatCommonJar() {
        return findOpenEJBJar("tomee-common");
    }

    @Override
    public File findOpenEJBJar(final String namePrefix) {
        return findJar(getOpenEJBLibDir(), namePrefix);
    }

    @Override
    public File findOpenEJBWebJar(final String namePrefix) {
        return findJar(getOpenEJBWebLibDir(), namePrefix);
    }

    @Override
    public File findTomEELibJar(final String prefix) {
        File jar = findJar(getCatalinaLibDir(), prefix);
        if (jar == null) { // maybe tomcat/openejb integration
            final String tomeeWar = System.getProperty("tomee.war");
            if (tomeeWar != null) {
                jar = findJar(new File(tomeeWar, "lib"), prefix);
            }
            if ((jar == null || !jar.exists()) && prefix.equals("openejb-jpa-integration")) {
                jar = JarLocation.jarLocation(MakeTxLookup.class);
            }
        }
        return jar;
    }

    private File findJar(final File dir, final String namePrefix) {
        if (dir == null) {
            return null;
        }

        final File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return (name.startsWith(namePrefix + "-") && name.endsWith(".jar")) || name.equals(namePrefix);
            }
        });

        return files != null && files.length > 0? files[0] : null;
    }

    /**Verifies the following:
     * <ul>
     *     <li>{@link #openejbWarDir} is unpacked</li>
     *  <li>{@link #catalinaHomeDir} is defined </li>
     *  <li>{@link #catalinaBaseDir} is defined </li>
     *  <li>{@link #catalinaHomeDir} exists, is a directory and is readable </li>
     *  <li>{@link #catalinaBaseDir} exists, is a directory and is readable </li>
     *  <li>{@link #getCatalinaLibDir()} exists, is a directory , is readable and is writable </li>
     *  <li>{@link #getCatalinaConfDir()} exists, is a directory , is readable and is writable </li>
     *  <li>{@link #getCatalinaBinDir()} exists, is a directory , is readable</li>
     *  <li>{@link #getCatalinaShFile()} exists, is a File , is readable and is writable </li>
     *  <li>{@link #getCatalinaBatFile()()} exists, is a File , is readable and is writable </li>
     *  <li>{@link #getServerXmlFile()} exists, is a File , is readable and is writable </li>
     *  <li>{@link #getOpenEJBLibDir()} exists, is a directory and is readable </li>
     *  <li>{@link #getOpenEJBTomcatLoaderJar()} is not null, exists, is a File and is readable </li>
     *  <li>{@link #getOpenEJBJavaagentJar()} is not null, exists, is a File and is readable </li>
     *  <li>{@link #getOpenEJBCoreJar()} is not null, exists, is a File and is readable </li>
     *  <li>{@link #getOpenEJBJavaagentJar()} exists, is a File and is readable </li>
     * </ul>
     * 
     * @return true if verification succeeds
     */
    @Override
    public boolean verify() {
        if (openejbWarDir == null) {
            addError("OpenEJB war is not unpacked");
        }
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
        verifyWritableFile("Catalina catalina.bat", getCatalinaBatFile());

        verifyDirectory("OpenEJB lib", getOpenEJBLibDir());

        final File openejbLoaderJar = getOpenEJBTomcatLoaderJar();
        if (openejbLoaderJar == null) {
            addError("OpenEJB loader jar was not found in the OpenEJB lib dir");
        }
        verifyFile("OpenEJB loader jar", openejbLoaderJar);

        final File openejbJavaagentJar = getOpenEJBJavaagentJar();
        if (openejbJavaagentJar == null) {
            addError("OpenEJB javaagent jar was not found in the OpenEJB lib dir");
        }
        verifyFile("OpenEJB javaagent jar", openejbJavaagentJar);

        final File openejbCoreJar = getOpenEJBCoreJar();
        if (openejbCoreJar != null) {
            verifyFile("OpenEJB core jar", openejbCoreJar);
        }

        return !hasErrors();
    }
    /**
     * Clears out all verification errors from the underlying list
     */
    @Override
    public void reset() {
        errors.clear();
    }
    /**
     * Checks to see if there are any verification errors
     * @return true if there are verification errors
     */
    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    /**
     * Returns a list of verification errors
     * @return a list of verification errors
     */
    @Override
    public List<String> getErrors() {
        return errors;
    }
    
    private void addError(final String message) {
        errors.add(message);
    }

    private boolean verifyDirectory(final String description, final File file) {
        if (file == null) {
            // ignore... files are built up based on other files, and probles
            // with the root files will have been logged else where
            return false;
        }
        if (!file.exists()) {
            addError(description + " directory does not exist");
            return false;
        }
        if (!file.isDirectory()) {
            addError(description + " directory is not a directory");
            return false;
        }
        if (!file.canRead()) {
            addError(description + " directory is not readable");
            return false;
        }
        return true;
    }

    private void verifyWritableDirectory(final String description, final File file) {
        if (verifyDirectory(description, file)) {
            verifyWritable(description, file);
        }
    }

    private boolean verifyFile(final String description, final File file) {
        if (file == null) {
            // ignore... files are built up based on other files, and probles
            // with the root files will have been logged else where
            return false;
        }
        if (!file.exists()) {
            addError(description + " file does not exist");
            return false;
        }
        if (!file.isFile()) {
            addError(description + " file is not a file");
            return false;
        }
        if (!file.canRead()) {
            addError(description + " file is not readable");
            return false;
        }
        return true;
    }

    private void verifyWritableFile(final String description, final File file) {
        if (verifyFile(description, file)) {
            verifyWritable(description, file);
        }
    }

    private void verifyWritable(final String description, final File file) {
        if (file == null) {
            // ignore... files are built up based on other files, and probles
            // with the root files will have been logged else where
            return;
        }
        if (!file.canWrite()) {
            addError(description + " file is not writable");
        }
    }

    private File createFile(final String fileName) {
        if (fileName != null && fileName.trim().length() > 0) {
            return new File(fileName.trim());
        }
        return null;
    }

    @Override
    public File getOpenEJBWebLibDir() {
        if (openEJBWebLibDir == null) {
            openEJBWebLibDir = new File(openejbWarDir, "WEB-INF/lib");
        }
        return openEJBWebLibDir;
    }

    @Override
    public File getTomcatUsersXml() {
        if (tomcatUsersXml == null) {
            final File confdir = getCatalinaConfDir();
            if (confdir == null) {
                return null;
            }
            tomcatUsersXml = new File(confdir, "tomcat-users.xml");
        }
        return tomcatUsersXml;
    }

    @Override
    public File getSetClasspathSh() {
        final File binDir = getCatalinaBinDir();
        if (binDir == null) {
            return null;
        }
        return new File(binDir, "setclasspath.sh");
    }

    @Override
    public File getSetClasspathBat() {
        final File binDir = getCatalinaBinDir();
        if (binDir == null) {
            return null;
        }
        return new File(binDir, "setclasspath.bat");
    }

    @Override
    public File getCatalinaPolicy() {
        final File confDir = getCatalinaConfDir();
        if (confDir == null) {
            return null;
        }
        return new File(confDir, "catalina.policy");
    }
}
