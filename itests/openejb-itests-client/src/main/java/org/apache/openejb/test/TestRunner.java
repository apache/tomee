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
package org.apache.openejb.test;

import junit.framework.TestResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 *
 * @version $Rev$ $Date$
 */
public class TestRunner extends junit.textui.TestRunner {
    private static final String helpBase = "/META-INF/org.apache.openejb.cli/";

    /**
     * Constructs a TestRunner.
     */
    public TestRunner() {
        this(System.out);
    }

    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    public TestRunner(PrintStream writer) {
        this(new ResultPrinter(writer));
    }

    /**
     * Constructs a TestRunner using the given ResultPrinter all the output
     */
    public TestRunner(ResultPrinter printer) {
        super(printer);
    }

    /**
     * main entry point.
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            printHelp();
        } else {
            if (args[0].equals("--help")) {
                printHelp();

                return;
            } else if (args[0].equals("local")) {
                runLocalTests();
            } else if (args[0].equals("remote")) {
                runRemoteTests();
            } else if (args[0].equals("http")) {
                runRemoteHttpTests();
            } else if (args[0].equals("tomcat")) {
                runTomcatRemoteHttpTests();
            } else {
                printHelp();

                return;
            }

            try {
                TestRunner aTestRunner = new TestRunner();
                TestResult r = aTestRunner.start(new String[]{"org.apache.openejb.test.ClientTestSuite"});

                System.out.println("");
                System.out.println("_________________________________________________");
                System.out.println("CLIENT JNDI PROPERTIES");
                Properties env = TestManager.getServer().getContextEnvironment();
                for (Iterator iterator = env.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String key = (String) entry.getKey();
                    Object value = entry.getValue();
                    System.out.println(key+" = "+value);
                }
                System.out.println("_________________________________________________");

                if (!r.wasSuccessful())
                    System.exit(FAILURE_EXIT);
                System.exit(SUCCESS_EXIT);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(EXCEPTION_EXIT);
            }
        }
    }

    private static void runLocalTests() {
        setDefault("openejb.test.server", "org.apache.openejb.test.IvmTestServer");
        setDefault("openejb.test.database", "org.apache.openejb.test.HsqldbTestDatabase");

        System.out.println("_________________________________________________");
        System.out
                .println("|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|\n");
        System.out.println("Running EJB compliance tests on IntraVM Server");
        System.out.println("_________________________________________________");
    }

    private static void setDefault(String key, String value) {
        value = System.getProperty(key, value);
        System.setProperty(key, value);
    }

    private static void runRemoteTests() {
        setDefault("openejb.test.server", "org.apache.openejb.test.RemoteTestServer");
        setDefault("openejb.test.database", "org.apache.openejb.test.HsqldbTestDatabase");

        System.out.println("_________________________________________________");
        System.out
                .println("|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|\n");
        System.out.println("Running EJB compliance tests on Remote Server");
        System.out.println("_________________________________________________");
    }

    private static void runRemoteHttpTests() {
        setDefault("openejb.test.server", "org.apache.openejb.test.RemoteHttpTestServer");
        setDefault("openejb.test.database", "org.apache.openejb.test.HsqldbTestDatabase");

        System.out.println("_________________________________________________");
        System.out
                .println("|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|\n");
        System.out.println("Running EJB compliance tests on HTTP/Remote Server");
        System.out.println("_________________________________________________");
    }

    private static void runTomcatRemoteHttpTests() {
        setDefault("openejb.test.server", TomcatRemoteTestServer.class.getName());
        setDefault("openejb.test.database", "org.apache.openejb.test.HsqldbTestDatabase");

        System.out.println("_________________________________________________");
        System.out
                .println("|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|\n");
        System.out.println("Running EJB compliance tests on HTTP/Tomcat Server");
        System.out.println("_________________________________________________");
    }

    private static void printHelp() {
        String header = "OpenEJB Compliance Tests ";
        try {
            InputStream is = TestRunner.class.getResourceAsStream("/META-INF/openejb-version.properties");
            Properties versionInfo = new Properties();
            versionInfo.load(is);
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        // Internationalize this
        try {
            InputStream in = TestRunner.class.getResourceAsStream(helpBase + "test.help");
            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    public TestResult start(String args[]) throws Exception {
        TestResult result = null;
        try {

            TestManager.init(null);
            TestManager.start();
        } catch (Exception e) {
            System.out.println("Cannot initialize the test environment: "
                    + e.getClass().getName() + " " + e.getMessage());
             e.printStackTrace();
            // System.exit(-1);
            throw e;
        }

        try {
            result = super.start(args);
        } catch (Exception ex) {
        } finally {
            try {
                TestManager.stop();
            } catch (Exception e) {
                ; // ignore it
            }
        }
        // System.exit(0);
        return result;
    }

    private static final class Pipe implements Runnable {

        private final InputStream is;

        private final OutputStream out;

        private Pipe(InputStream is, OutputStream out) {

            super();

            this.is = is;

            this.out = out;

        }

        public void run() {

            try {

                int i = is.read();

                out.write(i);

                while (i != -1) {

                    i = is.read();

                    out.write(i);

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }
}
