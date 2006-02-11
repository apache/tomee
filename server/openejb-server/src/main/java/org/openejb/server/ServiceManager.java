package org.openejb.server;

import org.openejb.ClassLoaderUtil;
import org.openejb.loader.SystemInstance;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

public class ServiceManager {

    static Messages messages = new Messages("org.openejb.server.util.resources");
    static Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.openejb.server.util.resources");

    private static ServiceManager manager;

    private static HashMap propsByFile = new HashMap();
    private static HashMap fileByProps = new HashMap();

    private static ServerService[] daemons;

    private boolean stop = false;

    private ServiceManager() {
    }

    public static ServiceManager getManager() {
        if (manager == null) {
            manager = new ServiceManager();
        }

        return manager;
    }

    public void init() throws Exception {
        try {
            org.apache.log4j.MDC.put("SERVER", "main");
            InetAddress localhost = InetAddress.getLocalHost();
            org.apache.log4j.MDC.put("HOST", localhost.getHostName());
        } catch (Exception e) {
        }

        String[] serviceFiles = new String[]{
                "admin.properties",
                "ejbd.properties",
                "telnet.properties",
                "webadmin.properties"
        };

        Vector enabledServers = new Vector();

        for (int i = 0; i < serviceFiles.length; i++) {
            try {

                Properties props = getProperties(serviceFiles[i]);
                if (isEnabled(props)) {
                    ServerService server = createService(props);
                    server = wrapService(server);
                    server.init(props);
                    enabledServers.add(server);
                }
            } catch (Throwable e) {
                logger.i18n.error("service.not.loaded", serviceFiles[i], e.getMessage());
            }
        }

        daemons = new ServerService[enabledServers.size()];
        enabledServers.copyInto(daemons);

//        daemons = new Service[]{
//            new AdminService(),
//            new EjbService(),
//            new TelnetService(),
//            new WebAdminService()
//            new XmlRpcService(),
//            new EjbXmlService(),
//            new WebEjbService(),
//        };

    }

    private static Properties getProperties(String file) throws ServiceException {
        Properties props = (Properties) propsByFile.get(file);

        if (props == null) {
            props = loadProperties(file);
            propsByFile.put(file, props);
            fileByProps.put(props, file);
        }

        return props;
    }

    private static Properties loadProperties(String file) throws ServiceException {

        Properties props = new Properties();
        try {
            File propFile = SystemInstance.get().getBase().getFile("conf/" + file);
            props.load(new FileInputStream(propFile));
        } catch (IOException e) {
            InputStream in = null;
            OutputStream out = null;
            try {
                URL url = new URL("resource:/" + file);
                in = url.openStream();
                props.load(in);
                in.close();

                File propFile = SystemInstance.get().getBase().getFile("conf/" + file, false);
                out = new FileOutputStream(propFile);
                in = url.openStream();

                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
            } catch (Exception e2) {
                throw new ServiceException("Cannot load properties", e2);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e1) {

                }
            }
        }

        return props;
    }

    private ServerService createService(Properties props) throws ServiceException {
        ServerService service = null;

        String serviceClassName = getRequiredProperty("server", props);
        Class serviceClass = loadClass(serviceClassName);
        checkImplementation(serviceClass);
        service = instantiateService(serviceClass);

        return service;
    }

    private ServerService wrapService(ServerService service) {
        service = new ServiceLogger(service);
        service = new ServiceAccessController(service);
        service = new ServiceDaemon(service);
        return service;
    }

    public synchronized void start() throws ServiceException {
        boolean display = System.getProperty("openejb.nobanner") == null;

        if (display) {
            System.out.println("  ** Starting Services **");
            printRow("NAME", "IP", "PORT");
        }

        for (int i = 0; i < daemons.length; i++) {
            ServerService d = daemons[i];
            try {
                d.start();
                if (display) {
                    printRow(d.getName(), d.getIP(), d.getPort() + "");
                }
            } catch (Exception e) {
                logger.error(d.getName() + " " + d.getIP() + " " + d.getPort() + ": " + e.getMessage());
                if (display) {
                    printRow(d.getName(), "----", "FAILED");
                }
            }
        }
        if (display) {
            System.out.println("-------");
            System.out.println("Ready!");
        }
        /*
         * This will cause the user thread (the thread that keeps the
         *  vm alive) to go into a state of constant waiting.
         *  Each time the thread is woken up, it checks to see if
         *  it should continue waiting.
         *
         *  To stop the thread (and the VM), just call the stop method
         *  which will set 'stop' to true and notify the user thread.
         */
        try {
            while (!stop) {

                this.wait(Long.MAX_VALUE);
            }
        } catch (Throwable t) {
            logger.fatal("Unable to keep the server thread alive. Received exception: " + t.getClass().getName() + " : " + t.getMessage());
        }
        System.out.println("[] exiting vm");
        logger.info("Stopping Remote Server");

    }

    public synchronized void stop() throws ServiceException {
        System.out.println("[] received stop signal");
        stop = true;
        for (int i = 0; i < daemons.length; i++) {
            daemons[i].stop();
        }
        notifyAll();
    }

    public void printRow(String col1, String col2, String col3) {

        col1 += "                    ";
        col1 = col1.substring(0, 20);

        col2 += "                    ";
        col2 = col2.substring(0, 15);

        col3 += "                    ";
        col3 = col3.substring(0, 6);

        StringBuffer sb = new StringBuffer(50);
        sb.append("  ").append(col1);
        sb.append(" ").append(col2);
        sb.append(" ").append(col3);

        System.out.println(sb.toString());
    }

    private Class loadClass(String className) throws ServiceException {
        ClassLoader loader = ClassLoaderUtil.getContextClassLoader();
        Class clazz = null;
        try {
            clazz = Class.forName(className, true, loader);
        } catch (ClassNotFoundException cnfe) {
            String msg = messages.format("service.no.class", className);
            throw new ServiceException(msg);
        }
        return clazz;
    }

    private void checkImplementation(Class clazz) throws ServiceException {
        Class intrfce = org.openejb.server.ServerService.class;

        if (!intrfce.isAssignableFrom(clazz)) {
            String msg = messages.format("service.bad.impl", clazz.getName(), intrfce.getName());
            throw new ServiceException(msg);
        }
    }

    private ServerService instantiateService(Class clazz) throws ServiceException {
        ServerService service = null;

        try {
            service = (ServerService) clazz.newInstance();
        } catch (Throwable t) {
            String msg = messages.format("service.instantiation.err",
                    clazz.getName(),
                    t.getClass().getName(),
                    t.getMessage());

            throw new ServiceException(msg, t);
        }

        return service;
    }

    private boolean isEnabled(Properties props) throws ServiceException {

        String dissabled = props.getProperty("dissabled", "");

        if (dissabled.equalsIgnoreCase("yes") || dissabled.equalsIgnoreCase("true")) {
            return false;
        } else {
            return true;
        }
    }

    public static String getRequiredProperty(String name, Properties props) throws ServiceException {

        String value = props.getProperty(name);
        if (value == null) {
            String msg = messages.format("service.missing.property",
                    name, fileByProps.get(props));

            throw new ServiceException(msg);
        }

        return value;
    }

}
