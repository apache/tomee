package org.superbiz.dynamic.mbean;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rmannibucau
 */
public class DynamicMBeanHandler implements InvocationHandler {
    private Map<Method, ObjectName> objectNames = new ConcurrentHashMap<Method, ObjectName>();

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class) && "toString".equals(method.getName())) {
            return getClass().getSimpleName() + " Proxy";
        }

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final String methodName = method.getName();
        final ObjectName objectName = getObjectName(method);
        final MBeanInfo infos = server.getMBeanInfo(objectName);
        if (methodName.startsWith("set") && methodName.length() > 3 && args != null && args.length == 1
                && (Void.TYPE.equals(method.getReturnType()) || Void.class.equals(method.getReturnType()))) {
            final String attributeName =  attributeName(infos, methodName, method.getParameterTypes()[0]);
            server.setAttribute(objectName, new Attribute(attributeName, args[0]));
            return null;
        } else if (methodName.startsWith("get") && (args == null || args.length == 0) && methodName.length() > 3) {
            final String attributeName =  attributeName(infos, methodName, method.getReturnType());
            return server.getAttribute(objectName, attributeName);
        }
        // operation
        return server.invoke(objectName, methodName, args, getSignature(method));
    }

    private String[] getSignature(Method method) {
        String[] args = new String[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            args[i] = method.getParameterTypes()[i].getName();
        }
        return args; // note: null should often work...
    }

    private String attributeName(MBeanInfo infos, String methodName, Class<?> type) {
        String found = null;
        String foundBackUp = null; // without checking the type
        final String attributeName = methodName.substring(3, methodName.length());
        final String lowerName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4, methodName.length());

        for (MBeanAttributeInfo attribute : infos.getAttributes()) {
            final String name = attribute.getName();
            if (attributeName.equals(name)) {
                foundBackUp = attributeName;
                if (attribute.getType().equals(type.getName())) {
                    found = name;
                }
            } else if (found == null && ((lowerName.equals(name) && !attributeName.equals(name))
                                                || lowerName.equalsIgnoreCase(name))) {
                foundBackUp = name;
                if (attribute.getType().equals(type.getName())) {
                    found = name;
                }
            }
        }

        if (found == null) {
            throw new UnsupportedOperationException("cannot find attribute " + attributeName);
        }

        return found;
    }

    private synchronized ObjectName getObjectName(Method method) throws MalformedObjectNameException {
        if (!objectNames.containsKey(method)) {
            synchronized (objectNames) {
                if (!objectNames.containsKey(method)) { // double check for synchro
                    org.superbiz.dynamic.mbean.ObjectName on = method.getAnnotation(org.superbiz.dynamic.mbean.ObjectName.class);
                    if (on == null) {
                        Class<?> current = method.getDeclaringClass();
                        do {
                            on = method.getDeclaringClass().getAnnotation(org.superbiz.dynamic.mbean.ObjectName.class);
                            current = current.getSuperclass();
                        } while (on == null && current != null);
                        if (on == null) {
                            throw new UnsupportedOperationException("class or method should define the objectName to use for invocation: " + method.toGenericString());
                        }
                    }
                    objectNames.put(method, new ObjectName(on.value()));
                }
            }
        }
        return objectNames.get(method);
    }
}
