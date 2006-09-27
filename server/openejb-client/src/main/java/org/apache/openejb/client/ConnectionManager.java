package org.apache.openejb.client;

import java.io.IOException;
import java.util.Properties;
import java.net.URI;

public class ConnectionManager {

    private static ConnectionFactory factory;
    private static Class defaultFactoryClass = SocketConnectionFactory.class;
    private static String factoryName;

    static {
        try {
            installFactory(defaultFactoryClass.getName());
        } catch (Exception e) {

        }
    }

    public static Connection getConnection(ServerMetaData server) throws IOException {
        URI location = server.getLocation();
        if (location.getScheme().equals("http")){
            return new HttpConnectionFactory().getConnection(server);
        } else {
            return factory.getConnection(server);
        }
    }

    public static void setFactory(String factoryName) throws IOException {
        installFactory(factoryName);
    }

    public static ConnectionFactory getFactory() {
        return factory;
    }

    public static String getFactoryName() {
        return factoryName;
    }

    private static void installFactory(String factoryName) throws IOException {

        Class factoryClass = null;
        ConnectionFactory factory = null;

        try {
            ClassLoader cl = getContextClassLoader();
            factoryClass = Class.forName(factoryName, true, cl);
        } catch (Exception e) {
            throw new IOException("No ConnectionFactory Can be installed. Unable to load the class " + factoryName);
        }

        try {
            factory = (ConnectionFactory) factoryClass.newInstance();
        } catch (Exception e) {
            throw new IOException("No ConnectionFactory Can be installed. Unable to instantiate the class " + factoryName);
        }

        try {

            factory.init(new Properties());
        } catch (Exception e) {
            throw new IOException("No ConnectionFactory Can be installed. Unable to initialize the class " + factoryName);
        }

        ConnectionManager.factory = factory;
        ConnectionManager.factoryName = factoryName;
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }

}
