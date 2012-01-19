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

import org.apache.openejb.server.script.OpenEJBScripter;

public class ScriptCommand extends AbstractCommand {
    public static final String SCRIPT_CMD = "script"; // use it instead of name() because of inheritance

    protected OpenEJBScripter scripter;
    protected String language;
    protected String script;

    @Override
    public String name() {
        return SCRIPT_CMD;
    }

    @Override
    public String usage() {
        return name() + " <script code>";
    }

    @Override
    public String description() {
        return "execute script code in the specified language. ejb can be accessed through their ejb name in the script.";
    }

    @Override
    public void execute(final String cmd) {
        try {
            parse(cmd);
            final Object result = scripter.evaluate(language, script);
            streamManager.writeOut(streamManager.asString(result));
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }

    protected void parse(final String cmd) {
        final String parseableCmd = cmd.substring(name().length() + 1);
        final int spaceIdx = parseableCmd.indexOf(" ");
        if (spaceIdx < 0) {
            throw new IllegalArgumentException("bad syntax, see help");
        }
        language = parseableCmd.substring(0, spaceIdx);
        script = parseableCmd.substring(spaceIdx + 1, parseableCmd.length());
    }

    public void setScripter(OpenEJBScripter scripter) {
        this.scripter = scripter;
    }
}
