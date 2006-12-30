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
import java.security.Permission;

public class Agent {
    private static final Permission ACCESS_PERMISSION = new ReflectPermission("suppressAccessChecks");
    private static String agentArgs;
    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
    }

    public static String getAgentArgs() {
        return agentArgs;
    }

    /**
     * Gets the instrumentation instance.
     * You must have java.lang.ReflectPermission(suppressAccessChecks) to call this method
     * @return the instrumentation instance
     */
    public static Instrumentation getInstrumentation() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        return instrumentation;
    }
}
