package org.apache.openejb.tomcat.loader;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;

public class TomcatHelper {

	public static StandardServer getServer() {
		StandardServer server = null;
		
		// first try to use Tomcat's ServerFactory class to give us a reference to the server
		
		try {
			Class<?> tomcatServerFactory = Class.forName("org.apache.catalina.ServerFactory");
			Method getServerMethod = tomcatServerFactory.getMethod("getServer");
			server = (StandardServer) getServerMethod.invoke(null);
		} catch (Exception e) {
		}
		
		if (server != null) {
			return server;
		}
		
		// if this fails, we'll try and get a reference from the platform mbean server
		try {
			MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
			server = (StandardServer) mbeanServer.getAttribute(new ObjectName("Catalina:type=Server"), "managedResource");
		} catch (Exception e) {
		}

		// if this still fails, that's too bad.
		
		return server;
	}
	
	public static int getContextState(StandardContext standardContext) {
		int state;
		
		try {
			Method getStateMethod = StandardContext.class.getMethod("getState");
			Object result = getStateMethod.invoke(standardContext);
			
			
			if (Integer.TYPE.equals(result.getClass())) {
				state = (Integer) result;
				return state;
			}
			
			if (result.getClass().isEnum()) {
				Enum<?> e = (Enum<?>) result;
				
				if ("FAILED".equals(e.toString())) {
					return 4;
				} else if ("STOPPING".equals(e.toString()) || "STOPPING_PREP".equals(e.toString()) || "MUST_STOP".equals(e.toString()) || "MUST_DESTROY".equals(e.toString())) {
					return 2;
				} else if ("RUNNING".equals(e.toString()) || "STARTED".equals(e.toString())) {
					return 1;
				} else if ("INITIALIZED".equals(e.toString())) {
					return 0;
				}
			}
		} catch (Exception e) {
		}
		
		// return STOPPED by default
		return 3;
	}
}
