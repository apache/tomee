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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.WebXml;
import org.apache.catalina.startup.ContextConfig;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.routing.RouterInitializer;
import org.apache.xbean.finder.util.Classes;
import org.xml.sax.InputSource;

import javax.servlet.ServletContainerInitializer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OpenEJBContextConfig extends ContextConfig {

    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB, OpenEJBContextConfig.class);

    private static final String MYFACES_TOMEEM_CONTAINER_INITIALIZER = "org.apache.tomee.myfaces.TomEEMyFacesContainerInitializer";
    private static final String TOMEE_MYFACES_CONTEXT_LISTENER = "org.apache.tomee.myfaces.TomEEMyFacesContextListener";

    private static final String CLASSES = "classes";
    private static final String WEB_INF = "WEB-INF";

    private TomcatWebAppBuilder.StandardContextInfo info;

    public OpenEJBContextConfig(TomcatWebAppBuilder.StandardContextInfo standardContextInfo) {
        logger.debug("OpenEJBContextConfig({0})", standardContextInfo.toString());
        info = standardContextInfo;
    }

    @Override
    protected WebXml createWebXml() {
        String prefix = "";
        if (context instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) context;
            prefix = standardContext.getEncodedPath();
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }
        }
        return new OpenEJBWebXml(prefix);
    }

    public class OpenEJBWebXml extends WebXml {
        public static final String OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY = "openejb.web.xml.major";

        private String prefix;

        public OpenEJBWebXml(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public int getMajorVersion() {
            return SystemInstance.get().getOptions().get(prefix + "." + OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY,
                    SystemInstance.get().getOptions().get(OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY, super.getMajorVersion()));
        }
    }

    @Override
    protected void webConfig() {
        // routing config
        context.addServletContainerInitializer(new RouterInitializer(), null); // first one

        // read the real config
        super.webConfig();

        // add myfaces auto-initializer
        try {
            final Class<?> myfacesInitializer = Class.forName(MYFACES_TOMEEM_CONTAINER_INITIALIZER, true, context.getLoader().getClassLoader());
            final ServletContainerInitializer instance = (ServletContainerInitializer) myfacesInitializer.newInstance();
            context.addServletContainerInitializer(instance, getJsfClasses(context));
            context.addApplicationListener(TOMEE_MYFACES_CONTEXT_LISTENER); // cleanup listener
        } catch (Exception ignored) {
            // no-op
        }
    }

    private Set<Class<?>> getJsfClasses(final Context context) {
        final WebAppBuilder builder = SystemInstance.get().getComponent(WebAppBuilder.class);
        final ClassLoader cl = context.getLoader().getClassLoader();
        final Map<String, Set<String>> scanned = builder.getJsfClasses().get(cl);

        if (scanned == null || scanned.isEmpty()) {
            return null;
        }

        final Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Set<String> entry : scanned.values()) {
            for (String name : entry) {
                try {
                    classes.add(cl.loadClass(name));
                } catch (ClassNotFoundException ignored) {
                    logger.warning("class '" + name + "' was found but can't be loaded as a JSF class");
                }
            }
        }

        return classes;
    }

    @Override
    protected void parseWebXml(InputSource source, WebXml dest, boolean fragment) {
        super.parseWebXml(source, dest, fragment);
    }

    @Override
    protected void processAnnotationsFile(File file, WebXml fragment,
            boolean handlesTypesOnly) {
        logger.debug("processAnnotationsFile {0}", file.getAbsolutePath() );
        try {
            final WebAppInfo webAppInfo = info.get();

            if (webAppInfo == null) {
                logger.warning("WebAppInfo not found. " + info);
                super.processAnnotationsFile(file, fragment, handlesTypesOnly);
                return;
            }

            logger.debug("Optimized Scan of File {0}", file.getAbsolutePath());

            // TODO We should just remember which jars each class came from
            // then we wouldn't need to lookup the class from the URL in this
            // way to guarantee we only add classes from this URL.
            final URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            for (String webAnnotatedClassName : webAppInfo.webAnnotatedClasses) {

                final String includedPackage = getSubPackage(file);
                if (includedPackage == null || !webAnnotatedClassName.startsWith(includedPackage)) {
                    continue;
                }

                final String classFile = webAnnotatedClassName.substring(includedPackage.length()).replace('.', '/') + ".class";
                final URL classUrl = loader.getResource(classFile);

                if (classUrl == null) {
                    logger.debug("Not present " + webAnnotatedClassName);
                    continue;
                }

                logger.debug("Found {0}", webAnnotatedClassName);

                final InputStream inputStream = classUrl.openStream();
                try {
                    processAnnotationsStream(inputStream, fragment, handlesTypesOnly);
                    logger.debug("Succeeded {0}", webAnnotatedClassName);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    inputStream.close();
                }

            }
        } catch (Exception e) {
            logger.error("OpenEJBContextConfig.processAnnotationsFile: failed.", e);
        }
    }

    // because we don't always get WEB-INF/classes folder, simply get the already appended subpackage
    private static String getSubPackage(final File file) {
        File current = file.getParentFile();
        if (current == null) {
            return "";
        }

        File previous = file;
        while (current.getParentFile() != null) {
            if (CLASSES.equals(previous.getName()) && WEB_INF.equals(current.getName())) {
                String path = file.getAbsolutePath().replaceFirst(previous.getAbsolutePath(), "");
                if (path.startsWith(File.separator)) {
                    path = path.substring(File.separator.length());
                }
                if (path.endsWith(File.separator)) {
                    path = path.substring(0, path.length() - 1);
                }

                if (path.isEmpty()) {
                    return path;
                }

                return path + ".";
            }

            previous = current;
            current = current.getParentFile();
        }

        return ""; // no subpackage found
    }

    @Override
    protected void processAnnotationsUrl(URL url, WebXml fragment, boolean handlesTypeOnly) {
        logger.debug("processAnnotationsUrl " + url);
        if (SystemInstance.get().getOptions().get("tomee.tomcat.scan", false)) {
            super.processAnnotationsUrl(url, fragment, handlesTypeOnly);
            return;
        }

        try {
            final WebAppInfo webAppInfo = info.get();

            if (webAppInfo == null) {
                logger.warning("WebAppInfo not found. " + info);
                super.processAnnotationsUrl(url, fragment, handlesTypeOnly);
                return;
            }

            logger.debug("Optimized Scan of URL " + url);

            // TODO We should just remember which jars each class came from
            // then we wouldn't need to lookup the class from the URL in this
            // way to guarantee we only add classes from this URL.
            final URLClassLoader loader = new URLClassLoader(new URL[]{url});
            for (String webAnnotatedClassName : webAppInfo.webAnnotatedClasses) {

                final String classFile = webAnnotatedClassName.replace('.', '/') + ".class";
                final URL classUrl = loader.getResource(classFile);

                if (classUrl == null) {
                    logger.debug("Not present " + webAnnotatedClassName);
                    continue;
                }

                logger.debug("Found " + webAnnotatedClassName);

                final InputStream inputStream = classUrl.openStream();
                try {
                    processAnnotationsStream(inputStream, fragment, handlesTypeOnly);
                    logger.debug("Succeeded " + webAnnotatedClassName);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    inputStream.close();
                }

            }
        } catch (Exception e) {
            logger.error("OpenEJBContextConfig.processAnnotationsUrl: failed.", e);
        }
    }
}
