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
package org.apache.openejb.javaagent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.Field;
import java.security.Permission;

public class Agent {
    private static final Permission ACCESS_PERMISSION = new ReflectPermission("suppressAccessChecks");
    private static String agentArgs;
    private static Instrumentation instrumentation;
    private static boolean initialized = false;

    public static synchronized void premain(String agentArgs, Instrumentation instrumentation) {
        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
        initialized = true;
    }

    public static synchronized String getAgentArgs() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        checkInitialization();
        return agentArgs;
    }

    /**
     * Gets the instrumentation instance.
     * You must have java.lang.ReflectPermission(suppressAccessChecks) to call this method
     * @return the instrumentation instance
     */
    public static synchronized Instrumentation getInstrumentation() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        checkInitialization();
        return instrumentation;
    }

    private static synchronized void checkInitialization() {
        if (!initialized) {
            try {
                ClassLoader systemCl = ClassLoader.getSystemClassLoader();
                Class<?> systemAgentClass = systemCl.loadClass(Agent.class.getName());

                Field instrumentationField = systemAgentClass.getDeclaredField("instrumentation");
                instrumentationField.setAccessible(true);
                instrumentation = (Instrumentation) instrumentationField.get(null);

                Field agentArgsField = systemAgentClass.getDeclaredField("agentArgs");
                agentArgsField.setAccessible(true);
                agentArgs = (String) agentArgsField.get(null);
            } catch (Exception e) {
                new IllegalStateException("Unable to initialize agent", e).printStackTrace();
            }
            initialized = true;
        }
    }
}
