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
import org.apache.catalina.startup.Bootstrap;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Core;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.DaemonThreadFactory;
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
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @version $Rev$ $Date$
 */
public class Container {

    static {
        // org.apache.naming
        Assembler.installNaming("org.apache.naming", true);
    }

    static {
        Core.warmup();
        final ExecutorService executor = Executors.newFixedThreadPool(4, new DaemonThreadFactory("warmup"));

        executor.execute(new JaxbJavaeeLoad(WebApp.class));
//        executor.execute(new JaxbJavaeeLoad(TldTaglib.class));
        executor.execute(new Runnable(){
            @Override
            public void run() {
                try {
                    JaxbOpenejb.getContext(Openejb.class);
                } catch (JAXBException e) {
                }
            }
        });
        executor.execute(new JaxbJavaeeLoad(Persistence.class));
    }


    private static class JaxbJavaeeLoad implements Runnable {

        private final Class<?> type;

        private JaxbJavaeeLoad(Class<?> type) {
            this.type = type;
        }

        @Override
        public void run() {
            try {
                JaxbJavaee.getContext(type);
            } catch (JAXBException e) {
            }
        }
    }

    private Bootstrap bootstrap;
    protected Configuration configuration;
    private File base;
    private Map<String, String> moduleIds = new HashMap<String, String>(); // TODO: manage multimap
    private Map<String, AppContext> appContexts = new HashMap<String, AppContext>(); // TODO: manage multimap
    private ConfigurationFactory configurationFactory;
    private Assembler assembler;
    private final Tomcat tomcat;

    public Container() {
        final Configuration configuration = new Configuration();
        configuration.setHttpPort(23880);
        configuration.setStopPort(23881);
        setup(configuration);
        final Class<Bootstrap> bootstrapClass = Bootstrap.class;
        tomcat = new TomcatWithFastSessionIDs();
    }

    public void setup(Configuration configuration) {
        this.configuration = configuration;
    }

    public void start() throws Exception {
        final String dir = getBaseDir();

        Logger.configure();

        base = new File(dir);
        if (base.exists()) {
            base.delete();
        }

        base.mkdirs();
        // TODO: this delete on exit won't actually work
        base.deleteOnExit();

        final File conf = createDirectory(base, "conf");
        final File lib = createDirectory(base, "lib");
        final File logs = createDirectory(base, "logs");
        final File webapps = createDirectory(base, "webapps");
        final File temp = createDirectory(base, "temp");
        final File work = createDirectory(base, "work");

        copyFileTo(conf, "catalina.policy");
        copyTemplateTo(conf, "catalina.properties");
        copyFileTo(conf, "context.xml");
        copyFileTo(conf, "openejb.xml");
        copyFileTo(conf, "server.xml");
        copyFileTo(conf, "tomcat-users.xml");
        copyFileTo(conf, "web.xml");

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

        Connector connector = new Connector(Http11Protocol.class.getName());
        connector.setPort(configuration.getHttpPort());
        connector.setAttribute("connectionTimeout", "3000");
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
        tomcat.setBaseDir(base.getAbsolutePath());
        tomcat.getHost().setAppBase(webapps.getAbsolutePath());

        // Bootstrap Tomcat
        System.out.println("Starting TomEE from: " + base.getAbsolutePath());

        String catalinaBase = base.getAbsolutePath();
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
        Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("openejb.loader", "tomcat-system");
        properties.setProperty("openejb.home", catalinaBase);
        properties.setProperty("openejb.base", catalinaBase);
        properties.setProperty("openejb.servicemanager.enabled", "false");

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Properties tomcatServerInfo = IO.readProperties(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"), new Properties());

            String serverNumber = tomcatServerInfo.getProperty("server.number");
            if (serverNumber == null) {
                // Tomcat5 only has server.info
                String serverInfo = tomcatServerInfo.getProperty("server.info");
                if (serverInfo != null) {
                    int slash = serverInfo.indexOf('/');
                    serverNumber = serverInfo.substring(slash + 1);
                }
            }
            if (serverNumber != null) {
                System.setProperty("tomcat.version", serverNumber);
            }

            String serverBuilt = tomcatServerInfo.getProperty("server.built");
            if (serverBuilt != null) {
                System.setProperty("tomcat.built", serverBuilt);
            }
        } catch (Throwable e) {
        }

        SystemInstance.init(System.getProperties());
        SystemInstance.get().setComponent(StandardServer.class, (StandardServer) tomcat.getServer());

        TomcatLoader loader = new TomcatLoader();
        loader.initDefaults(properties);
        loader.initialize(properties);


        assembler = SystemInstance.get().getComponent(Assembler.class);
        configurationFactory = new ConfigurationFactory();
    }

    private String getBaseDir() {
        try {
            final String dir = configuration.getDir();
            if (dir != null) return dir;
            final File file = File.createTempFile("apache-tomee", "-home");
            return file.getAbsolutePath();
        } catch (IOException e) {
            throw new TomEERuntimeException(e);
        }
    }

    public void stop() throws Exception {
        tomcat.stop();
        deleteTree(base);
        OpenEJB.destroy();
    }

    public AppContext deploy(String name, File file) throws OpenEJBException, IOException, NamingException {
        return deploy(name, file, false);
    }

    public AppContext deploy(String name, File file, boolean overrideName) throws OpenEJBException, IOException, NamingException {
        AppInfo appInfo = configurationFactory.configureApplication(file);
        if (overrideName) {
            appInfo.appId = name;
            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                if (file.getName().equals(ejbJar.moduleName)) {
                    ejbJar.moduleName = name;
                }
                for (EnterpriseBeanInfo ejb : ejbJar.enterpriseBeans) {
                    if (BeanContext.Comp.openejbCompName(file.getName()).equals(ejb.ejbName)) {
                        ejb.ejbName = BeanContext.Comp.openejbCompName(name);
                    }
                }
            }
            for (WebAppInfo webApp : appInfo.webApps) {
                if (file.getName().equals(webApp.moduleId)) {
                    webApp.moduleId = name;
                }
            }
        }

        AppContext context = assembler.createApplication(appInfo);
        appContexts.put(name, context);
        moduleIds.put(name, appInfo.path);
        return context;
    }

    public void undeploy(String name) throws UndeployException, NoSuchApplicationException {
        String moduleId = moduleIds.get(name);
        assembler.destroyApplication(moduleId);
        moduleIds.remove(name);
        appContexts.remove(name);
    }

    public Context getJndiContext() {
        return assembler.getContainerSystem().getJNDIContext();
    }

    public AppContext getAppContexts(String moduleId) {
        return appContexts.get(moduleId);
    }

    private void deleteTree(File file) {
        if (file == null)
            return;
        if (!file.exists())
            return;

        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            if ("".equals(file.getName()))
                return;
            if ("src/main".equals(file.getName()))
                return;

            File[] children = file.listFiles();

            for (File child : children) {
                deleteTree(child);
            }

            file.delete();
        }
    }

    private void copyTemplateTo(File targetDir, String filename) throws Exception {
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
        Velocity.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        Velocity.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        Template template = Velocity.getTemplate("/org/apache/tomee/configs/" + filename);
        VelocityContext context = new VelocityContext();
        context.put("tomcatHttpPort", Integer.toString(configuration.getHttpPort()));
        context.put("tomcatShutdownPort", Integer.toString(configuration.getStopPort()));
        Writer writer = new FileWriter(new File(targetDir, filename));
        template.merge(context, writer);
        writer.flush();
        writer.close();
    }

    private void copyFileTo(File targetDir, String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream("/org/apache/tomee/configs/" + filename);
        if (is != null) { // should be null since we are using default conf
            try {
                IO.copy(is, new File(targetDir, filename));
            } finally {
                IO.close(is);
            }
        }
    }

    private void createTomcatDirectories(File directory) {
        createDirectory(directory, "apps");
        createDirectory(directory, "conf");
        createDirectory(directory, "lib");
        createDirectory(directory, "logs");
        createDirectory(directory, "webapps");
        createDirectory(directory, "temp");
        createDirectory(directory, "work");
    }

    private File createDirectory(File parent, String directory) {
        File dir = new File(parent, directory);
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
            Server server = getServer();
            for (Service service : server.findServices()) {
                org.apache.catalina.Container e = service.getContainer();
                for (org.apache.catalina.Container h : e.findChildren()) {
                    for (org.apache.catalina.Container c : h.findChildren()) {
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
