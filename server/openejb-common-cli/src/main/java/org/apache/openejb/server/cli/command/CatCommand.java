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

import org.apache.openejb.loader.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Command(name = "cat", usage = "cat <path>", description = "cat a file (in tomee directories only)")
public class CatCommand extends PathCommand {
    @Override
    public void execute(final String cmd) {
        final File file;
        try {
            file = resolve("cat", cmd);
        } catch (IllegalArgumentException iae) {
            streamManager.writeErr(iae.getMessage());
            return;
        }

        if (file.isDirectory() && file.exists()) {
            streamManager.writeOut("file " + file.getPath() + " is a directory, please specify a regular file");
        } else if (file.exists()) {
            try {
                cat(file);
            } catch (IOException e) {
                streamManager.writeErr(e);
            }
        } else {
            streamManager.writeOut("file " + file.getPath() + " doesn't exist");
        }
    }

    private void cat(final File file) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int lineNumber = 0;
        try {
            while ((line = br.readLine()) != null) {
                lineNumber++;
                streamManager.writeOut(String.format("%3d. %s", lineNumber, line));
            }
        } finally {
            IO.close(br);
        }
    }
}
