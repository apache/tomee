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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util;

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OpenEJBScripter {
    private static final Map<String, ScriptEngineFactory> ENGINE_FACTORIES = new ConcurrentHashMap<String, ScriptEngineFactory>();
    private static final ThreadLocal<Map<String, ScriptEngine>> ENGINES = new ThreadLocal<Map<String, ScriptEngine>>() {
        @Override
        protected Map<String, ScriptEngine> initialValue() {
            return new HashMap<>();
        }
    };

    static {
        final ScriptEngineManager mgr = new ScriptEngineManager();
        for (final ScriptEngineFactory factory : mgr.getEngineFactories()) {
            if (factory.getParameter("THREADING") != null) { // thread safe
                for (final String ext : factory.getExtensions()) {
                    ENGINE_FACTORIES.put(ext, factory);
                }
            }
        }
    }

    public static Set<String> getSupportedLanguages() {
        return ENGINE_FACTORIES.keySet();
    }

    public Object evaluate(final String language, final String script) throws ScriptException {
        return evaluate(language, script, null);
    }

    public Object evaluate(final String language, final String script, final ScriptContext context) throws ScriptException {
        if (!ENGINE_FACTORIES.containsKey(language)) {
            throw new IllegalArgumentException("can't find factory for language " + language + ". You probably need to add the jar to openejb libs.");
        }

        ScriptContext executionContext = context;
        if (executionContext == null) {
            executionContext = new SimpleScriptContext();
        }

        //we bind local variables (per execution) every time we execute a script
        bindLocal(executionContext);

        final ScriptEngine engine = engine(language);
        return engine.eval(script, executionContext);
    }

    private static ScriptEngine engine(final String language) {
        ScriptEngine engine = ENGINES.get().get(language);
        if (engine == null) {
            final ScriptEngineFactory factory = ENGINE_FACTORIES.get(language);
            engine = factory.getScriptEngine();
            ENGINES.get().put(language, engine);
        }
        return engine;
    }

    public static void clearEngines() {
        ENGINES.get().clear();
    }

    private static void bindLocal(final ScriptContext context) {
        final Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

        bindings.put("bm", new BeanManagerHelper());
    }

    public static class BeanManagerHelper {
        public Object beanFromClass(final String appName, final String classname) {
            final AppContext appContext = appContext(appName);
            final BeanManager bm = appContext.getBeanManager();
            final Class<?> clazz;
            try {
                clazz = appContext.getClassLoader().loadClass(classname);
            } catch (final ClassNotFoundException e) {
                throw new OpenEJBRuntimeException(e);
            }
            final Set<Bean<?>> beans = bm.getBeans(clazz);
            return instance(bm, beans, clazz);
        }

        public Object beanFromName(final String appName, final String name) {
            final BeanManager bm = beanManager(appName);
            final Set<Bean<?>> beans = bm.getBeans(name);
            return instance(bm, beans, Object.class);
        }

        private <T> T instance(final BeanManager bm, final Set<Bean<?>> beans, final Class<T> clazz) {
            final Bean<?> bean = bm.resolve(beans);
            return (T) bm.getReference(bean, clazz, bm.createCreationalContext(bean));
        }

        private BeanManager beanManager(final String appName) {
            return appContext(appName).getBeanManager();
        }

        private AppContext appContext(final String appName) {
            final ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
            final AppContext appContext = cs.getAppContext(appName);
            if (appContext == null) {
                throw new OpenEJBRuntimeException("can't find application " + appName);
            }
            return appContext;
        }
    }
}

