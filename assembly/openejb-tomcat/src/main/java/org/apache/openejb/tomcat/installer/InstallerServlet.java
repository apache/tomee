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

import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Installs OpenEJB into Tomcat.
 * <p/>
 * NOTE: This servlet can not use any classes from OpenEJB since it is installing OpenEJB itself.
 */
public class InstallerServlet extends HttpServlet {
    private ServletContext servletContext;

    public void init(ServletConfig servletConfig) throws ServletException {
        servletContext = servletConfig.getServletContext();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        ServletOutputStream out = res.getOutputStream();
        out.println("Running Verifier...");

        try {
            boolean agentInstalled = invokeStaticNoArgMethod("org.apache.openejb.javaagent.Agent", "getInstrumentation") != null;

            Boolean listenerInstalled = (Boolean) invokeStaticNoArgMethod("org.apache.openejb.loader.OpenEJBListener", "isInstalled");
            if (listenerInstalled == null) listenerInstalled = false;

            boolean annotationJarRemoved;
            try {
                // Tomcat persistence context class is missing the properties method
                Class<?> persistenceContextClass = Class.forName("javax.persistence.PersistenceContext");
                persistenceContextClass.getMethod("properties", (Class[]) null);
                annotationJarRemoved = true;
            } catch (Exception e) {
                annotationJarRemoved = false;
            }

            out.println("Agent " + (agentInstalled ? "" : "NOT ") + "Installed");
            out.println("Listener " + (listenerInstalled ? "" : "NOT ") + "Installed");
            out.println("Annotation Jar " + (annotationJarRemoved ? "" : "NOT ") + "Removed");

            String tomcatVersion = getTomcatVersion();
            out.println("TomcatVersion = " + tomcatVersion);

            // find catalina home directory
            String catalinaHome = System.getProperty("catalina.home");
            out.println("catalina.home=" + catalinaHome);
            File catalinaHomeDir = new File(catalinaHome);
            if (!catalinaHomeDir.exists()) {
                out.println("Catalina home does not exist");
                return;
            }
            if (!catalinaHomeDir.isDirectory()) {
                out.println("Catalina home does not a directory");
                return;
            }

            // find catalina base directory
            String catalinaBase = System.getProperty("catalina.base");
            out.println("catalina.base=" + catalinaBase);
            File catalinaBaseDir = new File(catalinaBase);
            if (!catalinaBaseDir.exists()) {
                out.println("Catalina base does not exist");
                return;
            }
            if (!catalinaBaseDir.isDirectory()) {
                out.println("Catalina base does not a directory");
                return;
            }

            // find tomcat lib directory
            File libDir = new File(catalinaHomeDir, "lib");
            out.println("lib=" + libDir.getAbsolutePath());
            if (!libDir.exists()) {
                out.println("lib dir does not exist");
                return;
            }
            if (!libDir.isDirectory()) {
                out.println("lib dir is not a directory");
                return;
            }

            // find tomcat conf directory
            File confDir = new File(catalinaBaseDir, "conf");
            out.println("conf=" + confDir.getAbsolutePath());
            if (!confDir.exists()) {
                out.println("conf dir does not exist");
                return;
            }
            if (!confDir.isDirectory()) {
                out.println("conf dir is not a directory");
                return;
            }

            // find tomcat server.xml file
            File serverXml = new File(confDir, "server.xml");
            out.println("serverXml=" + serverXml.getAbsolutePath());
            if (!serverXml.exists()) {
                out.println("server.xml file does not exist");
                return;
            }
            if (!serverXml.canWrite()) {
                out.println("server.xml file is not writable");
                return;
            }
            if (!serverXml.isFile()) {
                out.println("server.xml file is not a file");
                return;
            }

            // find the tomcat bin directory
            File binDir = new File(catalinaHomeDir, "bin");
            out.println("bin=" + binDir.getAbsolutePath());
            if (!binDir.exists()) {
                out.println("bin dir does not exist");
                return;
            }
            if (!binDir.isDirectory()) {
                out.println("bin dir is not a directory");
                return;
            }

            // find tomcat catalina.sh file
            File catalinaSh = new File(binDir, "catalina.sh");
            out.println("catalinaSh=" + catalinaSh.getAbsolutePath());
            if (!catalinaSh.exists()) {
                out.println("catalina.sh file does not exist");
                return;
            }
            if (!catalinaSh.canWrite()) {
                out.println("catalina.sh file is not writable");
                return;
            }
            if (!catalinaSh.isFile()) {
                out.println("catalina.sh file is not a file");
                return;
            }

            // find openejb lib dir
            String openejbLib = servletContext.getRealPath("lib");
            if (openejbLib == null) {
                out.println("Can not find OpenEJB lib directory");
                return;
            }
            File openejbLibDir = new File(openejbLib);
            out.println("OpenEJB lib=" + openejbLibDir.getAbsolutePath());
            if (!openejbLibDir.exists()) {
                out.println("OpenEJB lib dir does not exist");
                return;
            }
            if (!openejbLibDir.isDirectory()) {
                out.println("OpenEJB lib dir is not a directory");
                return;
            }

            // find openejb-loader jar
            File openejbLoaderJar = null;
            for (File file : openejbLibDir.listFiles()) {
                if (file.getName().startsWith("openejb-loader-") && file.getName().endsWith(".jar")) {
                    openejbLoaderJar = file;
                }
            }
            if (openejbLoaderJar == null) {
                out.println("Can not find OpenEJB loader jar");
                return;
            }
            out.println("openejbLoaderJar=" + openejbLoaderJar.getAbsolutePath());


            // find openejb-javaagent jar
            File openejbJavaagentJar = null;
            for (File file : openejbLibDir.listFiles()) {
                if (file.getName().startsWith("openejb-javaagent-") && file.getName().endsWith(".jar")) {
                    openejbJavaagentJar = file;
                }
            }
            if (openejbJavaagentJar == null) {
                out.println("Can not find OpenEJB javaagent jar");
                return;
            }
            out.println("openejbJavaagentJar=" + openejbJavaagentJar.getAbsolutePath());

            String openejbJavaagentPath = catalinaBaseDir.toURI().relativize(openejbJavaagentJar.toURI()).getPath();
            out.println("openejbJavaagentPath=" + openejbJavaagentPath);


            out.println();
            if (annotationJarRemoved) {
                out.println("Annotation Jar already removed");
            } else {
                // copy loader jar to lib
                File destination = new File(libDir, "annotations-api.jar");
                if (destination.exists()) {
                    out.flush();

                    if (destination.delete()) {
                        out.println("Deleted non-compliant (invalid) Tomcat annotation jar.");
                    } else {
                        out.println("Can not delete non-compliant (invalid) Tomcat annotation jar.  Jar havs been marked to be deleted on a normal VM exit.");
                    }
                }
            }

//            out.println();
            if (listenerInstalled) {
                out.println("OpenEJB Listener already installed");
            } else {

                boolean copyOpenEJBLoader = true;

                // copy loader jar to lib
                File destination = new File(libDir, openejbLoaderJar.getName());
                if (destination.exists()) {
                    out.flush();

                    if (openejbLoaderJar.length() != destination.length()) {
                        // md5 diff the files
                    } else {
                        out.println("OpenEJB loader jar already installed in Tomcat lib directory.");
                        copyOpenEJBLoader = false;
                    }
                }

                if (copyOpenEJBLoader) {
                    copyFile(openejbLoaderJar, destination);
                    out.println("Coppied " + openejbLoaderJar.getName() + " to the Tomcat lib directory.");
                }

                // add listener to server.xml
                String serverXmlOriginal = readAll(serverXml);

                // write backup
                {
                    File backupFile = new File(confDir, "server.xml.original");
                    if (!backupFile.exists()) {
                        FileOutputStream fileOutputStream = new FileOutputStream(backupFile);
                        try {
                            writeAll(new ByteArrayInputStream(serverXmlOriginal.getBytes()), fileOutputStream);
                        } finally {
                            close(fileOutputStream);
                        }
                    }
                }

//                out.println();
//                out.println("====== ORIGINAL SERVER XML =====");
//                out.println(serverXmlOriginal);

                if (serverXmlOriginal.contains("org.apache.openejb.loader.OpenEJBListener")) {
                    out.println("OpenEJB Listener already declared in Tomcat server.xml file.");
                } else {
                    String newServerXml = replace(serverXmlOriginal,
                            "<Server",
                            "<Server",
                            ">",
                            ">\r\n" +
                                    "  <!-- OpenEJB plugin for Tomcat -->\r\n" +
                                    "  <Listener className=\"org.apache.openejb.loader.OpenEJBListener\" />");

//                out.println();
//                out.println("====== NEW SERVER XML =====");
//                out.println(newServerXml);

                    // overwrite server.xml
                    FileOutputStream fileOutputStream = new FileOutputStream(serverXml);
                    try {
                        writeAll(new ByteArrayInputStream(newServerXml.getBytes()), fileOutputStream);
                    } finally {
                        close(fileOutputStream);
                    }

                    out.println("Added OpenEJB listener to Tomcat server.xml file.");
                }
            }

//            out.println();
            if (agentInstalled) {
                out.println("OpenEJB Agent already installed");
            } else {

                // add agent to catalina.sh
                String catalinaShOriginal = readAll(catalinaSh);

                // write backup
                {
                    File backupFile = new File(binDir, "catalina.sh.original");
                    if (!backupFile.exists()) {
                        FileOutputStream fileOutputStream = new FileOutputStream(backupFile);
                        try {
                            writeAll(new ByteArrayInputStream(catalinaShOriginal.getBytes()), fileOutputStream);
                        } finally {
                            close(fileOutputStream);
                        }
                    }
                }

//                out.println();
//                out.println("====== ORIGINAL CATALINA SH =====");
//                out.println(catalinaShOriginal);

                if (catalinaShOriginal.contains("Add OpenEJB javaagent")) {
                    out.println("OpenEJB javaagent already declared in Tomcat catalina.sh file.");
                } else {
                    String newCatalinaSh = catalinaShOriginal.replace("# ----- Execute The Requested Command",
                            "# Add OpenEJB javaagent\n" +
                            "if [ -r \"$CATALINA_BASE\"/" + openejbJavaagentPath + " ]; then\n" +
                            "  JAVA_OPTS=\"\"-javaagent:$CATALINA_BASE/" + openejbJavaagentPath + "\" $JAVA_OPTS\"\n" +
                            "fi\n" +
                            "\n" +
                            "# ----- Execute The Requested Command");
//                    out.println();
//                    out.println("====== NEW CATALINA SH =====");
//                    out.println(newServerXml);

                    // overwrite server.xml
                    FileOutputStream fileOutputStream = new FileOutputStream(catalinaSh);
                    try {
                        writeAll(new ByteArrayInputStream(newCatalinaSh.getBytes()), fileOutputStream);
                    } finally {
                        close(fileOutputStream);
                    }

                    out.println("Added OpenEJB javaagent to Tomcat catalina.sh file.");
                }
            }

        } catch (Throwable e) {
            PrintStream printStream = new PrintStream(out, true);
            e.printStackTrace(printStream);
            printStream.flush();
        }
    }

    private String replace(String inputText, String begin, String newBegin, String end, String newEnd) throws IOException {
        BeginEndTokenHandler tokenHandler = new BeginEndTokenHandler(newBegin, newEnd);

        ByteArrayInputStream in = new ByteArrayInputStream(inputText.getBytes());

        InputStream replacementStream = new DelimitedTokenReplacementInputStream(in, begin, end, tokenHandler, true);
        String newServerXml = readAll(replacementStream);
        close(replacementStream);
        return newServerXml;
    }

    private void copyFile(File source, File destination) throws IOException {
        File destinationDir = destination.getParentFile();
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            throw new java.io.IOException("Cannot create directory : " + destinationDir);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            writeAll(in, out);
        } finally {
            close(in);
            close(out);
        }
    }

    private void writeAll(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }

    private String readAll(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            String text = readAll(in);
            return text;
        } finally {
            close(in);
        }
    }

    private String readAll(InputStream in) throws IOException {
        // SwizzleStream block read methods are broken so read byte at a time
        StringBuilder sb = new StringBuilder();
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString();
    }

    private String getTomcatVersion() {
        String tomcatVersion = null;
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"));
            tomcatVersion = properties.getProperty("server.number");
        } catch (IOException e) {
        }
        return tomcatVersion;
    }

    private Object invokeStaticNoArgMethod(String className, String propertyName) {
        try {
            Class<?> clazz = loadClass(className, getClass().getClassLoader());
            Method method = clazz.getMethod(propertyName);
            Object result = method.invoke(null, (Object[]) null);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
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

    private void close(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static class BeginEndTokenHandler extends StringTokenHandler {
        private final String begin;
        private final String end;

        public BeginEndTokenHandler(String begin, String end) {
            this.begin = begin;
            this.end = end;
        }

        public String handleToken(String token) throws IOException {
            String result = begin + token + end;
            return result;
        }
    }
}
