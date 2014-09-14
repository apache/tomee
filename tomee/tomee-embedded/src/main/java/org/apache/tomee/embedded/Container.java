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
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.WebAppDeployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.catalina.TomcatLoader;
import org.apache.tomee.catalina.session.FastNonSecureRandom;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class Container implements AutoCloseable {
    static {
        // org.apache.naming
        Assembler.installNaming("org.apache.naming", true);
    }

    private final Map<String, String> moduleIds = new HashMap<>(); // TODO: manage multimap
    private final Map<String, AppContext> appContexts = new HashMap<>(); // TODO: manage multimap
    private final Map<String, AppInfo> infos = new HashMap<>(); // TODO: manage multimap
    protected Configuration configuration;
    private File base;
    private ConfigurationFactory configurationFactory;
    private Assembler assembler;
    private Tomcat tomcat;

    // start the container directly
    public Container(final Configuration configuration) throws Exception {
        setup(configuration);
        start();
    }

    public Container() {
        this.configuration = new Configuration();
        this.configuration.setHttpPort(23880);
        this.configuration.setStopPort(23881);
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

    public void setup(final Configuration configuration) {
        this.configuration = configuration;

        if (configuration.isQuickSession()) {
            tomcat = new TomcatWithFastSessionIDs();
        } else {
            tomcat = new Tomcat();
        }

        // create basic installation in setup to be able to handle anything the caller does between setup() and start()
        base = new File(getBaseDir());
        if (base.exists()) {
            Files.delete(base);
        }

        Files.mkdirs(base);
        Files.deleteOnExit(base);

        createDirectory(base, "conf");
        createDirectory(base, "lib");
        createDirectory(base, "logs");
        createDirectory(base, "temp");
        createDirectory(base, "work");
        createDirectory(base, "webapps");
    }

    public File getBase() {
        return base;
    }

    public void start() throws Exception {
        if (base == null) {
            setup(configuration);
        }

        Logger.configure();

        final File conf = new File(base, "conf");
        final File webapps = new File(base, "webapps");

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

        tomcat.setBaseDir(base.getAbsolutePath());
        tomcat.getHost().setAppBase(webapps.getAbsolutePath());
        tomcat.setHostname(configuration.getHost());
        tomcat.getEngine().setDefaultHost(configuration.getHost());

        if (!configuration.isSkipHttp()) {
            final Connector connector = new Connector(Http11Protocol.class.getName());
            connector.setPort(configuration.getHttpPort());
            connector.setAttribute("connectionTimeout", "3000");
            tomcat.getService().addConnector(connector);
            tomcat.setConnector(connector);
        }

        // create https connector
        if (configuration.isSsl()) {
            final Connector httpsConnector = new Connector(Http11Protocol.class.getName());
            httpsConnector.setPort(configuration.getHttpsPort());
            httpsConnector.setSecure(true);
            httpsConnector.setProperty("SSLEnabled", "true");
            httpsConnector.setProperty("sslProtocol", configuration.getSslProtocol());

            if (configuration.getKeystoreFile() != null) {
                httpsConnector.setAttribute("keystoreFile", configuration.getKeystoreFile());
            }
            if (configuration.getKeystorePass() != null) {
                httpsConnector.setAttribute("keystorePass", configuration.getKeystorePass());
            }
            httpsConnector.setAttribute("keystoreType", configuration.getKeystoreType());
            httpsConnector.setAttribute("clientAuth", configuration.getClientAuth());
            httpsConnector.setAttribute("keyAlias", configuration.getKeyAlias());

            tomcat.getService().addConnector(httpsConnector);

            if (configuration.isSkipHttp()) {
                tomcat.setConnector(httpsConnector);
            }
        }

        // Bootstrap Tomcat
        Logger.getInstance(LogCategory.OPENEJB_STARTUP, Container.class).info("Starting TomEE from: " + base.getAbsolutePath()); // create it after Logger is configured

        final String catalinaBase = base.getAbsolutePath();
        System.setProperty("openejb.deployments.classpath", "false");
        System.setProperty("catalina.home", catalinaBase);
        System.setProperty("catalina.base", catalinaBase);
        System.setProperty("openejb.home", catalinaBase);
        System.setProperty("openejb.base", catalinaBase);
        System.setProperty("openejb.servicemanager.enabled", "false");

        tomcat.start();

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
        if (properties.getProperty("openejb.system.apps") == null)  { // will make startup faster and it is rarely useful for embedded case
            properties.setProperty("openejb.system.apps", "false");
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
        } catch (final Throwable e) {
            // no-op
        }

        final TomcatLoader loader = new TomcatLoader();
        loader.initDefaults(properties);

        // need to add properties after having initialized defaults
        // to properties passed to SystemInstance otherwise we loose some of them
        final Properties initProps = new Properties();
        initProps.putAll(System.getProperties());
        initProps.putAll(properties);
        SystemInstance.init(initProps);
        SystemInstance.get().setComponent(StandardServer.class, (StandardServer) tomcat.getServer());
        SystemInstance.get().setComponent(Server.class, tomcat.getServer()); // needed again cause of init()

        loader.initialize(properties);

        assembler = SystemInstance.get().getComponent(Assembler.class);
        configurationFactory = new ConfigurationFactory();
    }

    public ConfigurationFactory getConfigurationFactory() {
        return configurationFactory;
    }

    private String getBaseDir() {
        File file;
        try {

            final String dir = configuration.getDir();
            if (dir != null) {
                final File dirFile = new File(dir);
                if (dirFile.exists()) {
                    return dir;
                }
                return Files.mkdir(dirFile).getAbsolutePath();
            }

            try {
                file = File.createTempFile("apache-tomee", "-home");
            } catch (final Throwable e) {

                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                file = File.createTempFile("apache-tomee", "-home", tmp);
            }

            return file.getAbsolutePath();

        } catch (final IOException e) {
            throw new TomEERuntimeException("Failed to get or create base dir: " + configuration.getDir(), e);
        }
    }

    public void stop() throws Exception {
        tomcat.stop();
        tomcat.destroy();
        deleteTree(base);
        base = null;
        OpenEJB.destroy();
        // don't set base = null here to be able to use base after to clean up from outside of this class
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

            appInfo = SystemInstance.get().getComponent(WebAppDeployer.class).deploy(null, contextRoot, file);

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
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
            return;
        }

        if (file.isDirectory()) {
            if ("".equals(file.getName())) {
                return;
            }
            if ("src/main".equals(file.getName())) {
                return;
            }

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

    public Tomcat getTomcat() {
        return tomcat;
    }

    public void await() {
        tomcat.getServer().await();
    }

    @Override
    public void close() throws Exception {
        final CountDownLatch end = new CountDownLatch(1);
        new Thread() {
            {
                setName("tomee-embedded-await-" + hashCode());
            }

            @Override
            public void run() {
                try {
                    Container.this.await();
                    end.countDown();
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }.start();
        new Thread() {
            {
                setName("tomee-embedded-stop-" + hashCode());
            }

            @Override
            public void run() {
                try {
                    Container.this.stop();
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }.start();
        end.await();
    }

    public org.apache.catalina.Context addContext(final String context, final String path) {
        final File root = new File(path);
        if (!root.exists()) {
            Files.mkdirs(root);
        }
        return getTomcat().addContext(context, root.getAbsolutePath()); // we don't want to be relative
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
                        StandardManager m = (StandardManager) StandardContext.class.cast(c).getManager();
                        if (m == null) {
                            m = new StandardManager();
                            m.setSecureRandomClass(FastNonSecureRandom.class.getName());
                            StandardContext.class.cast(c).setManager(m);
                        }
                    }
                }
            }
            super.start();
        }
    }

}
