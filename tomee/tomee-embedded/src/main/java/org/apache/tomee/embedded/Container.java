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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.openejb.*;
import org.apache.openejb.assembler.WebAppDeployer;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.catalina.TomcatLoader;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Container {

    static {
        // org.apache.naming
        Assembler.installNaming("org.apache.naming", true);
    }

    protected Configuration configuration;
    private File base;
    private final Map<String, String> moduleIds = new HashMap<String, String>(); // TODO: manage multimap
    private final Map<String, AppContext> appContexts = new HashMap<String, AppContext>(); // TODO: manage multimap
    private final Map<String, AppInfo> infos = new HashMap<String, AppInfo>(); // TODO: manage multimap
    private ConfigurationFactory configurationFactory;
    private Assembler assembler;
    private Tomcat tomcat;

    public Container() {
        final Configuration configuration = new Configuration();
        configuration.setHttpPort(23880);
        configuration.setStopPort(23881);
        setup(configuration);
    }

    public void setup(final Configuration configuration) {
        this.configuration = configuration;

        if (configuration.isQuickSession()) {
            tomcat = new TomcatWithFastSessionIDs();
        } else {
            tomcat = new Tomcat();
        }
    }

    public void start() throws Exception {
        final String dir = getBaseDir();

        Logger.configure();

        base = new File(dir);
        if (base.exists()) {
            Files.delete(base);
        }

        Files.mkdirs(base);
        Files.deleteOnExit(base);

        final File conf = createDirectory(base, "conf");
        final File webapps = createDirectory(base, "webapps");
        createDirectory(base, "lib");
        createDirectory(base, "logs");
        createDirectory(base, "temp");
        createDirectory(base, "work");

        copyFileTo(conf, "catalina.policy");
        copyTemplateTo(conf, "catalina.properties");
        copyFileTo(conf, "context.xml");
        copyFileTo(conf, "openejb.xml");
        copyFileTo(conf, "tomcat-users.xml");
        copyFileTo(conf, "web.xml");
        if (configuration.hasServerXml()) {
            final FileOutputStream fos = new FileOutputStream(new File(conf, "server.xml"));
            try {
                IO.copy(configuration.getServerXmlFile(), fos);
            } finally {
                IO.close(fos);
            }
        } else {
            copyFileTo(conf, "server.xml");
        }
        final Properties props = configuration.getProperties();
        if (props != null && !props.isEmpty()) {
            final FileWriter systemProperties = new FileWriter(new File(conf, "system.properties"));
            try {
                props.store(systemProperties, "");
            } finally {
                IO.close(systemProperties);
            }
        }

        // Need to use JULI so log messages from the tests are visible
        // using openejb logging conf in embedded mode
        /* if we use our config (Logger.configure()) don't override it
        copyFileTo(conf, "logging.properties");
        System.setProperty("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        final File logging = new File(conf, "logging.properties");
        if (logging.exists()) {
            System.setProperty("java.util.logging.config.file", logging.getAbsolutePath());
        }
        */
        System.setProperty("catalina.base", base.getAbsolutePath());

        // Trigger loading of catalina.properties
        CatalinaProperties.getProperty("foo");

        final Connector connector = new Connector(Http11Protocol.class.getName());
        connector.setPort(configuration.getHttpPort());
        connector.setAttribute("connectionTimeout", "3000");
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
        tomcat.setBaseDir(base.getAbsolutePath());
        tomcat.getHost().setAppBase(webapps.getAbsolutePath());
        tomcat.setHostname(configuration.getHost());
        tomcat.getEngine().setDefaultHost(configuration.getHost());

        // Bootstrap Tomcat
        System.out.println("Starting TomEE from: " + base.getAbsolutePath());

        final String catalinaBase = base.getAbsolutePath();
        System.setProperty("openejb.deployments.classpath", "false");
        System.setProperty("catalina.home", catalinaBase);
        System.setProperty("catalina.base", catalinaBase);
        System.setProperty("openejb.home", catalinaBase);
        System.setProperty("openejb.base", catalinaBase);
        System.setProperty("openejb.servicemanager.enabled", "false");

        tomcat.start();


//        bootstrap = new Bootstrap();
//        bootstrap.start();

        // Bootstrap OpenEJB
        final Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("openejb.loader", "tomcat-system");
        properties.setProperty("openejb.home", catalinaBase);
        properties.setProperty("openejb.base", catalinaBase);
        properties.setProperty("openejb.servicemanager.enabled", "false");
        if (configuration.getProperties() != null) {
            properties.putAll(configuration.getProperties());
        }

        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final Properties tomcatServerInfo = IO.readProperties(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"), new Properties());

            String serverNumber = tomcatServerInfo.getProperty("server.number");
            if (serverNumber == null) {
                // Tomcat5 only has server.info
                final String serverInfo = tomcatServerInfo.getProperty("server.info");
                if (serverInfo != null) {
                    final int slash = serverInfo.indexOf('/');
                    serverNumber = serverInfo.substring(slash + 1);
                }
            }
            if (serverNumber != null) {
                System.setProperty("tomcat.version", serverNumber);
            }

            final String serverBuilt = tomcatServerInfo.getProperty("server.built");
            if (serverBuilt != null) {
                System.setProperty("tomcat.built", serverBuilt);
            }
        } catch (Throwable e) {
            // no-op
        }

        SystemInstance.init(System.getProperties());
        SystemInstance.get().setComponent(StandardServer.class, (StandardServer) tomcat.getServer());

        final TomcatLoader loader = new TomcatLoader();
        loader.initDefaults(properties);
        loader.initialize(properties);


        assembler = SystemInstance.get().getComponent(Assembler.class);
        configurationFactory = new ConfigurationFactory();
    }

    private String getBaseDir() {

        File file = null;

        try {

            final String dir = configuration.getDir();
            if (dir != null && new File(dir).exists()) {
                return dir;
            }

            try {
                file = File.createTempFile("apache-tomee", "-home");
            } catch (Throwable e) {

                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                file = File.createTempFile("apache-tomee", "-home", tmp);
            }

            return file.getAbsolutePath();

        } catch (IOException e) {
            throw new TomEERuntimeException("Failed to get or create base dir: " + file, e);
        }
    }

    public void stop() throws Exception {
        tomcat.stop();
        tomcat.destroy();
        deleteTree(base);
        OpenEJB.destroy();
    }

    @SuppressWarnings("UnusedDeclaration")
    public AppContext deploy(final String name, final File file) throws OpenEJBException, IOException, NamingException {
        return deploy(name, file, false);
    }

    public AppContext deploy(final String name, final File file, final boolean overrideName) throws OpenEJBException, IOException, NamingException {

        final AppContext context;
        final AppInfo appInfo;

        if (WebAppDeployer.Helper.isWebApp(file)) {
            String contextRoot = file.getName();
            if (overrideName) {
                contextRoot = name;
            }

            appInfo = SystemInstance.get().getComponent(WebAppDeployer.class).deploy(contextRoot, file);

            if (appInfo != null) {
                context = SystemInstance.get().getComponent(ContainerSystem.class).getAppContext(appInfo.appId);
            } else {
                context = null;
            }
        } else {
            appInfo = configurationFactory.configureApplication(file);
            if (overrideName) {
                appInfo.appId = name;
                for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
                    if (file.getName().equals(ejbJar.moduleName)) {
                        ejbJar.moduleName = name;
                        ejbJar.moduleId = name;
                    }
                    for (final EnterpriseBeanInfo ejb : ejbJar.enterpriseBeans) {
                        if (BeanContext.Comp.openejbCompName(file.getName()).equals(ejb.ejbName)) {
                            ejb.ejbName = BeanContext.Comp.openejbCompName(name);
                        }
                    }
                }
                for (final WebAppInfo webApp : appInfo.webApps) {
                    if (sameApplication(file, webApp)) {
                        webApp.moduleId = name;
                        webApp.contextRoot = lastPart(name, webApp.contextRoot);
                    }
                }
            }

            context = assembler.createApplication(appInfo);
        }

        moduleIds.put(name, null != appInfo ? appInfo.path : null);
        infos.put(name, appInfo);
        appContexts.put(name, context);

        return context;
    }

    private static boolean sameApplication(final File file, final WebAppInfo webApp) {
        String filename = file.getName();
        if (filename.endsWith(".war")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        return filename.equals(webApp.moduleId);
    }

    private static String lastPart(final String name, final String defaultValue) {
        final int idx = name.lastIndexOf("/");
        final int space = name.lastIndexOf(" ");
        if (idx >= 0 && space < idx) {
            return name.substring(idx);
        } else if (idx < 0 && space < 0) {
            return name;
        }
        return defaultValue;
    }

    @SuppressWarnings("UnusedDeclaration")
    public AppInfo getInfo(final String name) {
        return infos.get(name);
    }

    public void undeploy(final String name) throws UndeployException, NoSuchApplicationException {
        final String moduleId = moduleIds.remove(name);
        infos.remove(name);
        appContexts.remove(name);
        if (moduleId != null) {
            assembler.destroyApplication(moduleId);
        }
    }

    public Context getJndiContext() {
        return assembler.getContainerSystem().getJNDIContext();
    }

    public AppContext getAppContexts(final String moduleId) {
        return appContexts.get(moduleId);
    }

    private void deleteTree(final File file) {
        if (file == null)
            return;
        if (!file.exists())
            return;

        if (file.isFile()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
            return;
        }

        if (file.isDirectory()) {
            if ("".equals(file.getName()))
                return;
            if ("src/main".equals(file.getName()))
                return;

            final File[] children = file.listFiles();

            if (children != null) {
                for (final File child : children) {
                    deleteTree(child);
                }
            }

            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    private void copyTemplateTo(final File targetDir, final String filename) throws Exception {
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
        Velocity.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        final Template template = Velocity.getTemplate("/org/apache/tomee/configs/" + filename);
        final VelocityContext context = new VelocityContext();
        context.put("tomcatHttpPort", Integer.toString(configuration.getHttpPort()));
        context.put("tomcatShutdownPort", Integer.toString(configuration.getStopPort()));
        final Writer writer = new FileWriter(new File(targetDir, filename));
        template.merge(context, writer);
        writer.flush();
        writer.close();
    }

    private void copyFileTo(final File targetDir, final String filename) throws IOException {
        final InputStream is = getClass().getResourceAsStream("/org/apache/tomee/configs/" + filename);
        if (is != null) { // should be null since we are using default conf
            try {
                IO.copy(is, new File(targetDir, filename));
            } finally {
                IO.close(is);
            }
        }
    }

    private File createDirectory(final File parent, final String directory) {
        final File dir = new File(parent, directory);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to make dir " + dir.getAbsolutePath());
        }

        return dir;
    }

    public void await() {
        tomcat.getServer().await();
    }

    private static class TomcatWithFastSessionIDs extends Tomcat {

        @Override
        public void start() throws LifecycleException {
            // Use fast, insecure session ID generation for all tests
            final Server server = getServer();
            for (final Service service : server.findServices()) {
                final org.apache.catalina.Container e = service.getContainer();
                for (final org.apache.catalina.Container h : e.findChildren()) {
                    for (final org.apache.catalina.Container c : h.findChildren()) {
                        StandardManager m = (StandardManager) c.getManager();
                        if (m == null) {
                            m = new StandardManager();
                            m.setSecureRandomClass("org.apache.catalina.startup.FastNonSecureRandom");
                            c.setManager(m);
                        }
                    }
                }
            }
            super.start();
        }
    }

}
