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
package org.apache.openejb.server;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JarUtils;

public class Main {

    private static final String helpBase = "META-INF/org.apache.openejb.cli/";

    public static void main(String args[]) {

        try {
            Properties props = parseArguments(args);
            SystemInstance.init(props);
            SystemInstance system = SystemInstance.get();
            File libs = system.getHome().getDirectory("lib");
            system.getClassPath().addJarsToPath(libs);
            initServer(props);
        } catch (DontStartServerException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties parseArguments(String args[]) throws DontStartServerException {
        Properties props = new Properties();
        props.putAll(System.getProperties());

        props.put("openejb.server.ip", "127.0.0.1");
        props.put("openejb.server.port", "4201");
        props.put("openejb.server.threads", "20");

        JarUtils.setHandlerSystemProperty();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                if (args.length > i + 1) {
                    props.setProperty("openejb.server.ip", args[++i]);
                }
            } else if (args[i].equals("-p")) {
                if (args.length > i + 1) {
                    props.setProperty("openejb.server.port", args[++i]);
                }
            } else if (args[i].equals("-t")) {
                if (args.length > i + 1) {
                    props.setProperty("openejb.server.threads", args[++i]);
                }
            } else if (args[i].equals("-conf")) {
                if (args.length > i + 1) {
                    props.setProperty("openejb.configuration", args[++i]);
                }
            } else if (args[i].equals("-l")) {
                if (args.length > i + 1) {
                    props.setProperty("log4j.configuration", args[++i]);
                }
            } else if (args[i].equals("-d")) {
                if (args.length > i + 1) {
                    props.setProperty("openejb.home", args[++i]);
                }
            } else if (args[i].equals("--admin-ip")) {
                if (args.length > i + 1) {
                    props.setProperty("openejb.server.admin-ip", args[++i]);
                }
            } else if (args[i].startsWith("--local-copy")) {
                if (args[i].endsWith("false") ||
                        args[i].endsWith("FALSE") ||
                        args[i].endsWith("no") ||
                        args[i].endsWith("NO")) {
                    props.setProperty("openejb.localcopy", "false");
                } else {
                    props.setProperty("openejb.localcopy", "true");
                }
            } else if (args[i].equals("--help")) {
                printHelp();
                throw new DontStartServerException();
            } else if (args[i].equals("-version")) {
                printVersion();
                throw new DontStartServerException();
            } else if (args[i].equals("-examples")) {
                printExamples();
                throw new DontStartServerException();
            }
        }

        props.setProperty("org/openejb/configuration_factory", "org.apache.openejb.alt.config.ConfigurationFactory");

        return props;
    }

    private static void printVersion() {
        /*
         * Output startup message
         */
        Properties versionInfo = new Properties();

        try {
            JarUtils.setHandlerSystemProperty();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
        } catch (java.io.IOException e) {
        }
        System.out.println("OpenEJB Remote Server " + versionInfo.get("version") + "    build: " + versionInfo.get("date") + "-" + versionInfo.get("time"));
        System.out.println("" + versionInfo.get("url"));
    }

    private static void printHelp() {
        String header = "OpenEJB Remote Server ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResource(helpBase + "start.help").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void printExamples() {
        String header = "OpenEJB Remote Server ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResource(helpBase + "start.examples").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void initServer(Properties props) throws Exception {
        Server server = new Server();
        server.init(props);
        server.start();
    }
}

class DontStartServerException extends Exception {
}

