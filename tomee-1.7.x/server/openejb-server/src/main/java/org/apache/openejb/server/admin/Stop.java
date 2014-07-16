/**
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
package org.apache.openejb.server.admin;

import org.apache.openejb.client.RequestType;
import org.apache.openejb.loader.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class Stop {

    private static final String helpBase = "META-INF/org.apache.openejb.cli/";

    public static void stop(final String host, final int port) {

        Socket socket = null;
        OutputStream out = null;

        try {

            socket = new Socket(host, port);
            out = socket.getOutputStream();

            out.write(RequestType.STOP_REQUEST_Stop.getCode());

        } catch (ConnectException e) {
            System.out.println(":: server not running ::");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
    }

    public void stop() {
        stop("localhost", 4200);
    }

    public static void main(final String[] args) {
        try {

            String host = "localhost";

            int port = 4200;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-h")) {
                    if (args.length > i + 1) {
                        host = args[++i];
                    }
                } else if (args[i].equals("-p")) {
                    if (args.length > i + 1) {
                        port = Integer.parseInt(args[++i]);
                    }
                } else if (args[i].equals("--help")) {
                    printHelp();
                    return;
                } else if (args[i].equals("-examples")) {
                    printExamples();
                    return;
                }
            }

            stop(host, port);
        } catch (Exception re) {
            System.err.println("[EJB Server] FATAL ERROR: " + re.getMessage());
            re.printStackTrace();
        }
    }

    private static void printHelp() {
        String header = "OpenEJB Remote Server ";
        try {
            final Properties versionInfo = loadVersionProperties();
            header += versionInfo.get("version");
        } catch (Exception e) {
            //Ignore
        }

        System.out.println(header);

        InputStream in = null;
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(helpBase + "stop.help");
            if (resource != null) {
                in = resource.openConnection().getInputStream();

                int b = in.read();
                while (b != -1) {
                    System.out.write(b);
                    b = in.read();
                }
            }
        } catch (Exception e) {
            //Ignore
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
    }

    private static void printExamples() {
        String header = "OpenEJB Remote Server ";
        try {
            final Properties versionInfo = loadVersionProperties();
            header += versionInfo.get("version");
        } catch (Exception e) {
            //Ignore
        }

        System.out.println(header);
        InputStream in = null;
        try {
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(helpBase + "stop.examples");
            if (resource != null) {
                in = resource.openConnection().getInputStream();

                int b = in.read();
                while (b != -1) {
                    System.out.write(b);
                    b = in.read();
                }
            }
        } catch (Exception e) {
            //Ignore
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
    }

    private static Properties loadVersionProperties() throws IOException {
        return IO.readProperties(new URL("resource:/openejb-version.properties"));
    }
}
