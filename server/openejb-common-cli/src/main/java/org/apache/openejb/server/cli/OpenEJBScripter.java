/*
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
package org.apache.openejb.server.cli;

import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.proxy.ProxyEJB;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenEJBScripter {
    private static final Map<String, ScriptEngineFactory> ENGINE_FACTORIES = new ConcurrentHashMap<String, ScriptEngineFactory>();
    private static final ThreadLocal<Map<String, ScriptEngine>> ENGINES = new ThreadLocal<Map<String, ScriptEngine>>() {
        @Override
        protected Map<String, ScriptEngine> initialValue() {
            return new HashMap<String, ScriptEngine>();
        }
    };

    static {
        final ScriptEngineManager mgr = new ScriptEngineManager();
        for (ScriptEngineFactory factory : mgr.getEngineFactories()) {
            if (factory.getParameter("THREADING") != null) { // thread safe
                for (String ext : factory.getExtensions()) {
                    ENGINE_FACTORIES.put(ext, factory);
                }
            }
        }
    }

    public Object evaluate(final String language, final String script) throws ScriptException {
        if (!ENGINE_FACTORIES.containsKey(language)) {
            throw new IllegalArgumentException("can't find factory for language " + language + ". You probably need to add the jar to openejb libs.");
        }

        final ScriptEngine engine = engine(language);
        return engine.eval(script);
    }

    private static ScriptEngine engine(String language) {
        ScriptEngine engine = ENGINES.get().get(language);
        if (engine == null) {
            final ScriptEngineFactory factory = ENGINE_FACTORIES.get(language);
            engine = factory.getScriptEngine();
            engine.setBindings(binding(), ScriptContext.ENGINE_SCOPE);
            ENGINES.get().put(language, engine);
        }
        return engine;
    }

    public static void clearEngines() {
        ENGINES.get().clear();
    }

    private static Bindings binding() {
        final Bindings bindings = new SimpleBindings();
        final ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        for (BeanContext beanContext : cs.deployments()) {
            if (BeanContext.Comp.class.equals(beanContext.getBeanClass())) {
                continue;
            }

            Object service = null;
            if (beanContext.getBusinessLocalInterface() != null) {
                service = ProxyEJB.proxy(beanContext, beanContext.getBusinessLocalInterfaces().toArray(new Class<?>[beanContext.getBusinessLocalInterfaces().size()]));
            } else if (beanContext.isLocalbean()) {
                service = ProxyEJB.proxy(beanContext, new Class<?>[] { beanContext.getBusinessLocalBeanInterface() });
            } else if (beanContext.getBusinessRemoteInterface() != null) {
                service = ProxyEJB.proxy(beanContext, beanContext.getBusinessRemoteInterfaces().toArray(new Class<?>[beanContext.getBusinessRemoteInterfaces().size()]));
            }

            if (service != null) {
                // replace all non alphanumeric characters in the ejb name by an underscore (to be a groovy variable)
                bindings.put(beanContext.getEjbName().replaceAll("[^a-zA-Z0-9]", "_"), service);
            }
        }
        return bindings;
    }
}
