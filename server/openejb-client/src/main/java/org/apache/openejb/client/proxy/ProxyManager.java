package org.apache.openejb.client.proxy;

import java.util.Properties;

public class ProxyManager {

    private static ProxyFactory defaultFactory;
    private static String defaultFactoryName;

    static {
        String version = null;
        Class factory = null;
        try {
            version = System.getProperty("java.vm.version");
        } catch (Exception e) {

            throw new RuntimeException("Unable to determine the version of your VM.  No ProxyFactory Can be installed");
        }
        ClassLoader cl = getContextClassLoader();

        if (version.startsWith("1.1")) {
            throw new RuntimeException("This VM version is not supported: " + version);
        } else if (version.startsWith("1.2")) {
            defaultFactoryName = "JDK 1.2 ProxyFactory";

            try {
                Class.forName("org.opentools.proxies.Proxy", true, cl);
            } catch (Exception e) {

                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.opentools.proxies.Proxy.  This class is needed for generating proxies in JDK 1.2 VMs.");
            }

            try {
                factory = Class.forName("org.apache.openejb.client.proxy.Jdk12ProxyFactory", true, cl);
            } catch (Exception e) {

                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.apache.openejb.client.proxy.Jdk12ProxyFactory.");
            }
        } else {
            defaultFactoryName = "JDK 1.3 ProxyFactory";

            try {
                factory = Class.forName("org.apache.openejb.client.proxy.Jdk13ProxyFactory", true, cl);
            } catch (Exception e) {

                throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.apache.openejb.client.proxy.Jdk13ProxyFactory.");
            }
        }

        try {

            defaultFactory = (ProxyFactory) factory.newInstance();
            defaultFactory.init(new Properties());

        } catch (Exception e) {

            throw new RuntimeException("No ProxyFactory Can be installed. Unable to load the class org.apache.openejb.client.proxy.Jdk13ProxyFactory.");
        }

    }

    public static ProxyFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactoryName;
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        return defaultFactory.getInvocationHandler(proxy);
    }

    public static Object setInvocationHandler(Object proxy, InvocationHandler handler) {
        return defaultFactory.setInvocationHandler(proxy, handler);
    }

    public static Class getProxyClass(Class interfaceType) throws IllegalAccessException {
        return getProxyClass(new Class[]{interfaceType});
    }

    public static Class getProxyClass(Class[] interfaces) throws IllegalAccessException {
        return defaultFactory.getProxyClass(interfaces);
    }

    public static Object newProxyInstance(Class interfaceType, InvocationHandler h) throws IllegalAccessException {
        return newProxyInstance(new Class[]{interfaceType}, h);
    }

    public static Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalAccessException {
        return defaultFactory.newProxyInstance(interfaces, h);
    }

    public static boolean isProxyClass(Class cl) {
        return defaultFactory.isProxyClass(cl);
    }

    public static Object newProxyInstance(Class proxyClass) throws IllegalAccessException {
        return defaultFactory.newProxyInstance(proxyClass);
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
