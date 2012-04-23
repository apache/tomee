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

import java.io.File;

@Command(name = "ls", usage = "ls [<path>]", description = "list files (in tomee directories only)")
public class LsCommand extends PathCommand {
    @Override
    public void execute(final String cmd) {
        final File file;
        try {
            file = resolve("ls", cmd);
        } catch (IllegalArgumentException iae) {
            streamManager.writeErr(iae.getMessage());
            return;
        }

        if (file.isDirectory() && file.exists()) {
            list(file.getAbsolutePath(), file);
        } else if (file.exists()) {
            streamManager.writeOut("file " + file.getPath() + " exists");
        } else {
            streamManager.writeOut("file " + file.getPath() + " doesn't exist");
        }
    }

    private void list(final String base, final File dir) {
        String removed = base;
        if (!base.endsWith("/")) {
            removed = removed + "/";
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) { // not recursive otherwise it starts to be complicated
                streamManager.writeOut(type(file) + " " + file.getAbsolutePath().replace(removed, ""));
            }
        }
    }

    private static String type(final File file) {
        if (file.isDirectory()) {
            return "D";
        }
        return "F";
    }
}
