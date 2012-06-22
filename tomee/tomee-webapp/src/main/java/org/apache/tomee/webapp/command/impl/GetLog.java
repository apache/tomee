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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.Params;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class GetLog implements Command {

    @Override
    public Object execute(Params params) throws Exception {
        final Map<String, Object> json = new HashMap<String, Object>();

        final File logFolder = new File(System.getProperty("catalina.base"), "logs");

        final File[] files = logFolder.listFiles();
        final Set<String> names = new TreeSet<String>();
        if (files != null) {
            for (File file : files) {
                if (file.length() > 0) {
                    names.add(file.getName());
                }
            }
        }

        json.put("files", names);

        final String loadFileName = params.getString("file");
        if (loadFileName != null) {
            Map<String, Object> log = new HashMap<String, Object>();
            log.put("name", loadFileName);

            log.put("lines", read(
                    Boolean.valueOf(params.getString("escapeHtml")),
                    new File(logFolder, loadFileName),
                    params.getInteger("tail")
            ));

            json.put("log", log);
        }

        return json;
    }


    private Collection<String> read(final boolean escapeHtml, final File file, final Integer tail) throws IOException {
        final Queue<String> lines = new LinkedList<String>();

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));

            final AddLine addLine = new AddLine(lines, tail);
            String line;

            if (escapeHtml) {
                while ((line = br.readLine()) != null) {
                    addLine.add(StringEscapeUtils.escapeHtml4(line));
                }
            } else {
                while ((line = br.readLine()) != null) {
                    addLine.add(line);
                }
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return lines;
    }

    private interface AddItemStrategy {
        void add(String newLine);
    }

    private class AddLine {
        final Queue<String> lines;
        AddItemStrategy strategy;
        final int tail;

        private AddLine(Queue<String> lines, Integer tail) {
            this.lines = lines;

            if (tail == null) {
                this.tail = -1;
                this.strategy = justAddIt;
            } else {
                this.tail = tail;
                this.strategy = addToEmptyList;
            }
        }

        final AddItemStrategy justAddIt = new AddItemStrategy() {

            @Override
            public void add(String newLine) {
                lines.add(newLine);
            }
        };

        final AddItemStrategy addToEmptyList = new AddItemStrategy() {

            @Override
            public void add(String newLine) {
                lines.add(newLine);
                if (lines.size() > tail) {
                    strategy = addToFullList;
                }
            }
        };

        final AddItemStrategy addToFullList = new AddItemStrategy() {

            @Override
            public void add(String newLine) {
                lines.add(newLine);
                lines.remove();
            }
        };

        public void add(String newLine) {
            strategy.add(newLine);
        }
    }
}
