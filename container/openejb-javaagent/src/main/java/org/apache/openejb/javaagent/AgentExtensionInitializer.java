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

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

class AgentExtensionInitializer implements ClassFileTransformer {
    private final WeakHashMap<ClassLoader,Boolean> knownClassLoaders = new WeakHashMap<ClassLoader,Boolean>();
    private final Set<String> installedExtensions = new TreeSet<String>();

    public byte[] transform(final ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String classLoaderName = loader.getClass().getName();
        boolean shouldExecuteExtentions = false;
        if (!classLoaderName.equals("org.apache.openejb.core.TemporaryClassLoader")){
            synchronized(knownClassLoaders) {
                if (!knownClassLoaders.containsKey(loader)) {
                    knownClassLoaders.put(loader, true);
                    shouldExecuteExtentions = true;
                }
            }
        }

        if (shouldExecuteExtentions) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    executeExtentions(loader, Agent.getAgentArgs(), Agent.getInstrumentation());
                    return null;
                }
            });
        }

        return classfileBuffer;
    }

    private void executeExtentions(ClassLoader classLoader, String agentArgs, Instrumentation instrumentation) {
        Map<String, Class> extentions = null;

        try {
            ResourceFinder finder = new ResourceFinder("META-INF", classLoader);
            // todo don't load extension classess that have already been processed
            extentions = finder.mapAvailableImplementations(AgentExtention.class);
        } catch (IOException e) {
            new RuntimeException("Failed searching for AgentExtentions: "+e.getMessage(), e).printStackTrace();
        }

        for (Map.Entry<String, Class> entry : extentions.entrySet()) {
            if (!installedExtensions.contains(entry.getKey())) {
                // construct extension object
                AgentExtention extention = null;
                try {
                    extention = (AgentExtention) entry.getValue().newInstance();
                } catch (Throwable e) {
                    new RuntimeException("AgentExtention instantiation failed: AgentExtention(name="+entry.getKey()+", class="+entry.getValue().getName()+")", e).printStackTrace();
                }

                // call premain
                try {
                    extention.premain(agentArgs, instrumentation);
                } catch (Throwable e) {
                    new RuntimeException("AgentExtention premain failed: AgentExtention(name="+entry.getKey()+", class="+entry.getValue().getName()+")", e).printStackTrace();
                }

                // extension installed
                installedExtensions.add(entry.getKey());
            }
        }
    }
}
