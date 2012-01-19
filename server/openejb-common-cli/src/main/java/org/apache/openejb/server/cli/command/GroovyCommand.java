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
package org.apache.openejb.server.cli.command;

import java.lang.reflect.Method;

public class GroovyCommand extends AbstractCommand {
    public static final String GROOVY_NAME = "groovy"; // use it instead of name() because of inheritance

    protected Object shell;
    private Method evaluateMethod = null;

    @Override
    public String name() {
        return GROOVY_NAME;
    }

    @Override
    public String usage() {
        return name() + " <groovy code>";
    }

    @Override
    public String description() {
        return "execute groovy code. ejb can be accessed through their ejb name in the script.";
    }

    @Override
    public void execute(final String cmd) {
        if (initEvaluateMethod() == null) {
            streamManager.writeErr("groovy is not available, add groovy-all jar in openejb libs");
            return;
        }

        final String toExec = cmd.substring(GROOVY_NAME.length() + 1).trim();
        try {
            final Object result = evaluateMethod.invoke(shell, toExec);
            streamManager.writeOut(streamManager.asString(result));
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }

    public void setShell(Object shell) {
        this.shell = shell;
    }

    public Method initEvaluateMethod() {
        if (shell != null) {
            try {
                evaluateMethod = shell.getClass().getMethod("evaluate", String.class);
            } catch (Exception e) {
                // ignored
            }
        }
        return evaluateMethod;
    }
}
