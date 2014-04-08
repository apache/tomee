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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(name = "part", usage = "part <first line>-<last line> <path>", description = "print the specified line range of a file (in tomee directories only)")
public class PartCommand extends PathCommand {
    private static final Pattern PATTERN = Pattern.compile("([0-9]*)-([0-9]*) (.*)");
    @Override
    public void execute(final String cmd) {
        final Matcher matcher = PATTERN.matcher(cmd);
        if (!matcher.matches()) {
            streamManager.writeErr("you have to specify a numbe of line, please see help for more information");
            return;
        }

        int firstLine;
        int secondLine;
        try {
            firstLine = getInt(matcher.group(1));
            secondLine = getInt(matcher.group(2));
        } catch (NumberFormatException nfe) {
            streamManager.writeErr("line number was not parsable");
            return;
        }

        final String path = matcher.group(3);
        final File file;
        try {
            file = resolve(path);
        } catch (IllegalArgumentException iae) {
            streamManager.writeErr(iae.getMessage());
            return;
        }

        if (file.isDirectory() && file.exists()) {
            streamManager.writeOut("file " + file.getPath() + " is a directory, please specify a regular file");
        } else if (file.exists()) {
            try {
                part(file, firstLine, secondLine);
            } catch (IOException e) {
                streamManager.writeErr(e);
            }
        } else {
            streamManager.writeOut("file " + file.getPath() + " doesn't exist");
        }
    }

    private int getInt(String group) {
        try {
            return Integer.parseInt(group);
        } catch (NumberFormatException nfe) {
            streamManager.writeErr("line number should be an integer");
            throw nfe;
        }
    }

    private void part(final File file, int firstLine, int secondLine) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int totalLineNumber = 0;
        try {
            while (br.readLine() != null) {
                totalLineNumber++;
            }
        } finally {
            IO.close(br);
        }

        br = new BufferedReader(new FileReader(file));
        try {
            int firstLineToPrint = Math.max(1, firstLine);
            int lastLine = Math.min(totalLineNumber, secondLine);
            totalLineNumber = 0;
            while ((line = br.readLine()) != null) {
                totalLineNumber++;
                if (totalLineNumber >= firstLineToPrint && totalLineNumber <= lastLine) {
                    streamManager.writeOut(String.format("%3d. %s", totalLineNumber, line));
                }
                if (totalLineNumber > lastLine) {
                    break;
                }
            }
        } finally {
            IO.close(br);
        }
    }
}
