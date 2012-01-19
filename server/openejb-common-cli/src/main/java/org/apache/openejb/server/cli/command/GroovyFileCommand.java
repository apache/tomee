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

public class GroovyFileCommand extends GroovyCommand {
    @Override
    public String name() {
        return "groovy file";
    }

    @Override
    public String usage() {
        return name() + " <groovy file path>";
    }

    @Override
    public String description() {
        return "execute groovy code contained in a file. ejb can be accessed through their ejb name in the script.";
    }

    @Override
    public void execute(final String cmd) {
        if (initEvaluateMethod() == null) {
            streamManager.writeErr("groovy is not available, add groovy-all jar in openejb libs");
            return;
        }

        final File file = new File(cmd.substring(name().length() + 1).trim());
        if (!file.exists()) {
            streamManager.writeErr("groovy file " + file.getPath() + " doesn't exist");
            return;
        }

        final StringBuilder builder = new StringBuilder(1024);
        builder.append(super.name()).append(" "); // we will run the parent command

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[1024];
            int numRead;
            while((numRead = reader.read(buf)) != -1){
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
}
