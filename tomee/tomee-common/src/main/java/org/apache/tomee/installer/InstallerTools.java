package org.apache.tomee.installer;

import java.lang.reflect.Method;
import java.util.LinkedList;

public class InstallerTools {

    public static Object invokeStaticNoArgMethod(String className, String propertyName) {
        try {
            Class<?> clazz = loadClass(className, Installer.class.getClassLoader());
            Method method = clazz.getMethod(propertyName);
            return method.invoke(null, (Object[]) null);
        } catch (Throwable e) {
            return null;
        }
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        LinkedList<ClassLoader> loaders = new LinkedList<ClassLoader>();
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            loaders.addFirst(loader);
        }
        for (ClassLoader loader : loaders) {
            try {
                return Class.forName(className, true, loader);
            } catch (ClassNotFoundException e) {
                // no-op
            }
        }
        return null;
    }

}
