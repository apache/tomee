Title: dynamic-proxy-to-access-mbean

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/dynamic-proxy-to-access-mbean) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/dynamic-proxy-to-access-mbean). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## Example

Acessing MBean is something simple through the JMX API but it is often technical and not very interesting.

This example simplify this work simply doing it generically in a proxy.

So from an user side you simple declare an interface to access your MBeans.

Note: the example implementation uses a local MBeanServer but enhancing the example API
it is easy to imagine a remote connection with user/password if needed.

## ObjectName API (annotation)

Simply an annotation to get the object

	package org.superbiz.dynamic.mbean;

	import java.lang.annotation.Retention;
	import java.lang.annotation.Target;

	import static java.lang.annotation.ElementType.TYPE;
	import static java.lang.annotation.RetentionPolicy.RUNTIME;

	@Target({TYPE, METHOD})
	@Retention(RUNTIME)
	public @interface ObjectName {
		String value();
	}

## DynamicMBeanHandler (thr proxy implementation)

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

## DynamicMBeanClient (the dynamic JMX client)

	package org.superbiz.dynamic.mbean;

	import org.apache.openejb.api.Proxy;
	import org.superbiz.dynamic.mbean.DynamicMBeanHandler;
	import org.superbiz.dynamic.mbean.ObjectName;

	import javax.ejb.Singleton;

	/**
	 * @author rmannibucau
	 */
	@Singleton
	@Proxy(DynamicMBeanHandler.class)
	@ObjectName(DynamicMBeanClient.OBJECT_NAME)
	public interface DynamicMBeanClient {
		static final String OBJECT_NAME = "test:group=DynamicMBeanClientTest";

		int getCounter();
		void setCounter(int i);
		int length(String aString);
	}

## The MBean used for the test

### SimpleMBean

	package org.superbiz.dynamic.mbean.simple;

	public interface SimpleMBean {
		int length(String s);

		int getCounter();
		void setCounter(int c);
	}

## Simple

	package org.superbiz.dynamic.mbean.simple;

	public class Simple implements SimpleMBean {
		private int counter = 0;

		@Override public int length(String s) {
		    if (s == null) {
		        return 0;
		    }
		    return s.length();
		}

		@Override public int getCounter() {
		    return counter;
		}

		@Override public void setCounter(int c) {
		    counter = c;
		}
	}

## DynamicMBeanClientTest (The test)

package org.superbiz.dynamic.mbean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.dynamic.mbean.simple.Simple;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.embeddable.EJBContainer;
import javax.management.Attribute;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static junit.framework.Assert.assertEquals;

public class DynamicMBeanClientTest {
    private static ObjectName objectName;
    private static EJBContainer container;

    @EJB private DynamicMBeanClient client;

    @BeforeClass public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before public void injectAndRegisterMBean() throws Exception {
        container.getContext().bind("inject", this);
        objectName = new ObjectName(DynamicMBeanClient.OBJECT_NAME);
        ManagementFactory.getPlatformMBeanServer().registerMBean(new Simple(), objectName);
    }

    @After public void unregisterMBean() throws Exception {
        if (objectName != null) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
        }
    }

    @Test public void get() throws Exception {
        assertEquals(0, client.getCounter());
        ManagementFactory.getPlatformMBeanServer().setAttribute(objectName, new Attribute("Counter", 5));
        assertEquals(5, client.getCounter());
    }

    @Test public void set() throws Exception {
        assertEquals(0, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
        client.setCounter(8);
        assertEquals(8, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
    }

    @Test public void operation() {
        assertEquals(7, client.length("openejb"));
    }

    @AfterClass public static void close() {
        if (container != null) {
            container.close();
        }
    }
}

