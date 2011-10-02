/**
 *
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
package org.apache.openejb.tomcat.loader;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.loader.SystemInstance;

public class TomcatHelper {

	private static boolean stopping = false;
	
	public static boolean isStopping() {
		return stopping;
	}

	public static void setStopping(boolean stopping) {
		TomcatHelper.stopping = stopping;
	}

	public static StandardServer getServer() {
		StandardServer server = null;

        server = SystemInstance.get().getComponent(StandardServer.class);
        if (server != null) return server;

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

	/**
	 * Helper method to call the correct org.apache.catalina.Realm.hasRole method based on the Tomcat version
	 * @param realm
	 * @param tomcatPrincipal
	 * @param logicalRole
	 * @return true the the principle has the specified role
	 */
	public static boolean hasRole(Realm realm, Principal tomcatPrincipal, String logicalRole) {
		Method method = null;
		try {

			if (isTomcat7()) {
				method = realm.getClass().getMethod("hasRole", new Class<?>[] { Wrapper.class, Principal.class, String.class });
				return (Boolean) method.invoke(realm, new Object[] { null, tomcatPrincipal, logicalRole});
			} else {
				method = realm.getClass().getMethod("hasRole", new Class<?>[] { Principal.class, String.class });
				return (Boolean) method.invoke(realm, new Object[] { tomcatPrincipal, logicalRole});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isTomcat7() {
		return System.getProperty("tomcat.version").startsWith("7.");
	}

	public static void configureJarScanner(StandardContext standardContext) {
		try {
			Class<?> cls = Class.forName("org.apache.openejb.tomcat.loader.TomEEJarScanner");
			Class<?> jarScannerCls = Class.forName("org.apache.tomcat.JarScanner");
			Object instance = cls.newInstance();
			StandardContext.class.getMethod("setJarScanner", jarScannerCls).invoke(standardContext, instance);
		} catch (Exception e) {
			// ignore
			e.printStackTrace();
		}
	}

	/**
	 * Get a comma separated list of all jars under $CATALINA_BASE/webapps/openejb/lib
	 * The idea is that all of these jars should be excluded from Tomcat's scanning for web fragments
	 * because these jar don't have any fragments in, and the scanning process is expensive in terms
	 * of PermGen space.
	 * 
	 * @return list of jars as string, comma separated
	 */
	private static String getJarsToSkip() {
		File openejbApp = new File(System.getProperty("openejb.war"));
		File libFolder = new File(openejbApp, "lib");
		StringBuilder builder = new StringBuilder();
		
		for (File f : libFolder.listFiles()) {
			if (f.getName().startsWith("javaee-api-embedded")) continue;
			if (f.getName().startsWith("myfaces")) continue;
			
			
			
			if (f.getName().toLowerCase().endsWith(".jar")) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				
				builder.append(f.getName());
			}
		}
		
		return builder.toString();
	}
}
