package org.apache.openejb.util.proxy;

import java.util.HashMap;

public class ProxyManager {

    private static volatile ProxyFactory defaultFactory;
    private static final HashMap factories = new HashMap();
    private static volatile String defaultFactoryName;

    public static synchronized ProxyFactory registerFactory(String factoryName, ProxyFactory factory) {
        return (ProxyFactory) factories.put(factoryName, factory);
    }

    public static synchronized ProxyFactory unregisterFactory(String factoryName) {
        return (ProxyFactory) factories.remove(factoryName);
    }

    public static void checkDefaultFactory() {
        if (defaultFactory == null) throw new IllegalStateException("[Proxy Manager] No default proxy factory specified.");
    }

    public static ProxyFactory getFactory(String factoryName) {
        return (ProxyFactory) factories.get(factoryName);
    }

    public static synchronized ProxyFactory setDefaultFactory(String factoryName) {
        ProxyFactory newFactory = getFactory(factoryName);
        if (newFactory == null) return defaultFactory;

        ProxyFactory oldFactory = defaultFactory;
        defaultFactory = newFactory;
        defaultFactoryName = factoryName;

        return oldFactory;
    }

    public static ProxyFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactoryName;
    }

    public static InvocationHandler getInvocationHandler(Object proxy) {
        checkDefaultFactory();
        return defaultFactory.getInvocationHandler(proxy);
    }

    public static Object setInvocationHandler(Object proxy, InvocationHandler handler) {
        checkDefaultFactory();
        return defaultFactory.setInvocationHandler(proxy, handler);
    }

    public static Class getProxyClass(Class interfaceType) throws IllegalAccessException {
        return getProxyClass(new Class[]{interfaceType});
    }

    public static Class getProxyClass(Class[] interfaces) throws IllegalAccessException {
        checkDefaultFactory();
        return defaultFactory.getProxyClass(interfaces);
    }

    public static Object newProxyInstance(Class interfaceType, InvocationHandler h) throws IllegalAccessException {
        return newProxyInstance(new Class[]{interfaceType}, h);
    }

    public static Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalAccessException {
        checkDefaultFactory();
        return defaultFactory.newProxyInstance(interfaces, h);
    }

    public static boolean isProxyClass(Class cl) {
        checkDefaultFactory();
        return defaultFactory.isProxyClass(cl);
    }

    public static Object newProxyInstance(Class proxyClass) throws IllegalAccessException {
        checkDefaultFactory();
        return defaultFactory.newProxyInstance(proxyClass);
    }

}
