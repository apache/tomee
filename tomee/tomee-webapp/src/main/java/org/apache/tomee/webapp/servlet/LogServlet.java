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

package org.apache.tomee.webapp.servlet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tomee.webapp.JsonExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class LogServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        JsonExecutor.execute(req, resp, new JsonExecutor.Executor() {

            @Override
            public void call(Map<String, Object> json) throws Exception {
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

                final String loadFileName = req.getParameter("file");
                if (loadFileName != null) {
                    Map<String, Object> log = new HashMap<String, Object>();
                    log.put("name", loadFileName);

                    Integer tail;
                    try {
                        tail = Integer.valueOf(req.getParameter("tail"));
                    } catch (Exception e) {
                        tail = null;
                    }

                    log.put("lines", read(
                            Boolean.valueOf(req.getParameter("escapeHtml")),
                            new File(logFolder, loadFileName),
                            tail
                    ));

                    json.put("log", log);
                }
            }
        });

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
