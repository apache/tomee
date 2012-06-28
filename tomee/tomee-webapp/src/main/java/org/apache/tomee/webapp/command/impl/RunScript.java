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

package org.apache.tomee.webapp.command.impl;

import org.apache.openejb.util.OpenEJBScripter;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.Params;
import org.apache.tomee.webapp.command.impl.script.Utility;

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

        bindings.put("util", new Utility(params));

        //note that "engine" does not know "bindings". It only knows the current context.
        //Eventual exceptions are handled by the ErrorServlet
        final Object result = SCRIPTER.evaluate(engineName, scriptCode, newContext);
        return result;
    }
}
