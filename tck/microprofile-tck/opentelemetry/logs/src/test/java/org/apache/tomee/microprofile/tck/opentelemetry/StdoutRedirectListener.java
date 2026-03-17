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
package org.apache.tomee.microprofile.tck.opentelemetry;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

@WebListener
public class StdoutRedirectListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String logFilePath = System.getProperty("mptelemetry.tck.log.file.path");
        if (logFilePath != null) {
            try {
                final PrintStream original = System.out;
                final OutputStream fileOut = new FileOutputStream(logFilePath, true);
                PrintStream tee = new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        original.write(b);
                        fileOut.write(b);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws java.io.IOException {
                        original.write(b, off, len);
                        fileOut.write(b, off, len);
                    }

                    @Override
                    public void flush() throws java.io.IOException {
                        original.flush();
                        fileOut.flush();
                    }
                }, true);
                System.setOut(tee);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
