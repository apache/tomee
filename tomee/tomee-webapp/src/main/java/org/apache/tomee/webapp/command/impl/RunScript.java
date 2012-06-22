package org.apache.tomee.webapp.command.impl;

import org.apache.openejb.util.OpenEJBScripter;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.Params;
import org.apache.tomee.webapp.listener.UserSessionListener;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

public class RunScript implements Command {
    public static final OpenEJBScripter SCRIPTER = new OpenEJBScripter();

    @Override
    public Object execute(final Params params) throws Exception {
        final String scriptCode = params.getString("scriptCode");
        if (scriptCode == null) {
            return null; //nothing to do
        }

        String engineName = params.getString("engineName");
        if (engineName == null) {
            engineName = "js";
        }

        //new context for the execution of this script
        final ScriptContext newContext = new SimpleScriptContext();

        //creating the bidings object for the current execution
        final Bindings bindings = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

        bindings.put("util", new Utility() {
            @Override
            public void save(String key, Object obj) {
                UserSessionListener.getServiceContext(params.getReq().getSession()).getSaved().put(key, obj);
            }

            @Override
            public Object get(String key) {
                return UserSessionListener.getServiceContext(params.getReq().getSession()).getSaved().get(key);
            }
        });

        //note that "engine" does not know "bindings". It only knows the current context.
        //Eventual exceptions are handled by the ErrorServlet
        final Object result = SCRIPTER.evaluate(engineName, scriptCode, newContext);
        return result;
    }

    private interface Utility {
        void save(String key, Object obj);

        Object get(String key);
    }
}
