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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Command(name = "script file", usage = "script file <path>", description = "execute script code contained in a file in the specifid language. ejb can be accessed through their ejb name in the script. The extension is used to detect the file format.")
public class ScriptFileCommand extends ScriptCommand {
    @Override
    public void execute(final String cmd) {
        try {
            parse(cmd);
        } catch (IllegalArgumentException iae) {
            streamManager.writeErr("script cmd " + cmd + " can't be parsed");
            return;
        }

        final File file = new File(script);
        if (!file.exists()) {
            streamManager.writeErr("script file " + file.getPath() + " doesn't exist");
            return;
        }

        final StringBuilder builder = new StringBuilder(1024);
        builder.append("script ").append(language).append(" "); // we will run the parent command

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                builder.append(readData);
                buf = new char[1024];
            }
        } catch (Exception e) {
            streamManager.writeErr(e);
            return;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }

        super.execute(builder.toString());
    }

    private void parse(final String cmd) {
        script = cmd;
        final int dotIdx = script.lastIndexOf(".");
        if (dotIdx < 0) {
            throw new IllegalArgumentException("bad syntax, see help");
        }
        language = script.substring(dotIdx + 1, script.length());
    }
}
