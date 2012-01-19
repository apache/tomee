package org.apache.openejb.server.script;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenEJBScripter {
    private static final Map<String, ScriptEngineFactory> ENGINE_FACTORIES = new ConcurrentHashMap<String, ScriptEngineFactory>();

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

        final ScriptEngineFactory factory = ENGINE_FACTORIES.get(language);
        final ScriptEngine engine = factory.getScriptEngine();
        engine.setBindings(binding(), ScriptContext.ENGINE_SCOPE);
        return engine.eval(script);
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
