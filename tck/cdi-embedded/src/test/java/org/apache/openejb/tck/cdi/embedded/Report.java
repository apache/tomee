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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tck.cdi.embedded;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class Report {

    public static void main(String[] args) throws Exception {
        new Report().main();
    }

    private final LinkedList<TestClass> classes = new LinkedList<TestClass>();

    private void main() throws Exception {
//        final File file = new File("/Users/dblevins/work/uber/geronimo-tck-public-trunk/jcdi-tck-runner/target/surefire-reports/testng-results.xml");
        final File file = new File("/Users/dblevins/work/uber/openejb/tck/cdi-embedded/target/surefire-reports/testng-results.xml");
//        final File file = new File("/Users/dblevins/work/uber/testng-results.xml");
//        final File file = new File("/Users/dblevins/work/uber/openejb/tck/cdi-tomee/target/failsafe-reports/testng-results.xml");

        final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        parser.parse(file, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                final String name = qName;
                if ("class".equals(name)) {
                    classes.add(new TestClass(attributes.getValue("name")));
                }

                if ("test-method".equals(name)) {
                    classes.getLast().addStatus(attributes.getValue("status"), attributes.getValue("name"));
                }
            }
        });

        Collections.sort(classes);

//        textReport(file);
        passingXml(file);
//        passingXml2(file);
//        failingXml(file);
//        printResults(System.out);

    }

    private void textReport(File file) throws FileNotFoundException {
        final File report = new File(file.getParentFile(), file.getName().replaceAll(".xml$", ".txt"));
        final PrintStream out = new PrintStream(new FileOutputStream(report));
        printResults(out);
        out.close();
    }

    private void passingXml2(File file) throws FileNotFoundException {
        final File report = new File(file.getParentFile(), file.getName().replaceAll(".xml$", "-passing.xml"));
        final PrintStream out = new PrintStream(new FileOutputStream(report));

        out.println("" +
                "<suite name=\"CDI TCK\" verbose=\"0\">\n" +
                "  <listeners>\n" +
                "    <listener class-name=\"org.apache.openejb.tck.cdi.embedded.RequestScopeTestListener\" />\n" +
                "  </listeners>\n" +
                "  <test name=\"CDI TCK\">\n" +
                "    <!--<packages>-->\n" +
                "        <!--<package name=\"org.jboss.jsr299.tck.tests.*\"/>-->\n" +
                "        <!--<package name=\"org.jboss.jsr299.tck.interceptors.tests.*\"/>-->\n" +
                "    <!--</packages>-->\n" +
                "    <classes>");

        for (TestClass testClass : classes) {

            if (!contains(testClass, Status.FAIL)) {
                out.printf("      <class name=\"%s\"/>\n", testClass.name);
            }
        }
        out.println("    </classes>");
        out.println("  </test>");
        out.println("</suite>");

        out.close();
    }

    private void passingXml(File file) throws FileNotFoundException {
        final File report = new File(file.getParentFile(), file.getName().replaceAll(".xml$", "-passing.xml"));
        final PrintStream out = new PrintStream(new FileOutputStream(report));

        out.println("" +
                "<suite name=\"CDI TCK\" verbose=\"0\">\n" +
                "  <listeners>\n" +
                "    <listener class-name=\"org.apache.openejb.tck.cdi.embedded.RequestScopeTestListener\" />\n" +
                "  </listeners>\n" +
                "  <test name=\"CDI TCK\">\n" +
                "    <packages>\n" +
                "        <package name=\"org.jboss.jsr299.tck.tests.*\"/>\n" +
                "        <package name=\"org.jboss.jsr299.tck.interceptors.tests.*\"/>\n" +
                "    </packages>\n" +
                "    <classes>");

        for (TestClass testClass : classes) {

            if (contains(testClass, Status.FAIL)) {
                out.printf("      <class name=\"%s\">\n", testClass.name);
                out.printf("        <methods>\n");

                for (TestResult result : testClass.getResults()) {
                    if (result.status == Status.FAIL) {
                        out.printf("          <exclude name=\"%s\"/>\n", result.name);
                    }
                }

                out.printf("        </methods>\n");
                out.printf("      </class>\n");
            }
        }
        out.println("    </classes>");
        out.println("  </test>");
        out.println("</suite>");

        out.close();
    }

    private void failingXml(File file) throws FileNotFoundException {
        final File report = new File(file.getParentFile(), file.getName().replaceAll(".xml$", "-failing.xml"));
        final PrintStream out = new PrintStream(new FileOutputStream(report));

        out.println("<suite name=\"CDI TCK\" verbose=\"0\">\n"+
        "  <listeners>\n" +
        "    <listener class-name=\"org.apache.openejb.tck.cdi.embedded.RequestScopeTestListener\" />\n" +
        "  </listeners>");

        out.println("  <test name=\"CDI TCK\">");
        out.println("    <!--<packages>-->\n" +
                "        <!--<package name=\"org.jboss.jsr299.tck.tests.*\"/>-->\n" +
                "        <!--<package name=\"org.jboss.jsr299.tck.interceptors.tests.*\"/>-->\n" +
                "    <!--</packages>-->");
        out.println("    <classes>");

        for (TestClass testClass : classes) {

            if (contains(testClass, Status.FAIL)) {
                out.printf("      <class name=\"%s\"/>\n", testClass.name);
            }
        }
        out.println("    </classes>");
        out.println("  </test>");
        out.println("</suite>");

        out.close();
    }

    private boolean contains(TestClass testClass, Status status) {

        for (TestResult result : testClass.getResults()) {
            if (result.name.equals("beforeClass")) continue;
            if (result.name.equals("afterClass")) continue;
            if (result.name.equals("afterSuite")) continue;
            if (result.name.equals("beforeSuite")) continue;

            if (result.status == status)  {
                return true;
            }
        }
        return false;
    }

    private void printResults(PrintStream out) {

        Map<Status, AtomicInteger> totals = new HashMap<Status, AtomicInteger>();
        for (Status status : Status.values()) {
            totals.put(status, new AtomicInteger());
        }

        for (TestClass testClass : classes) {

            for (TestResult result : testClass.getResults()) {
                if (result.name.equals("beforeClass")) continue;
                if (result.name.equals("afterClass")) continue;
                if (result.name.equals("afterSuite")) continue;
                if (result.name.equals("beforeSuite")) continue;
//                if (result.status == Status.PASS) continue;
                totals.get(result.status).getAndIncrement();

                out.printf("%s - %s(%s)\n", result.status, result.name, testClass.name);
            }
        }

        out.println("\n\n");

        int total = 0;

        for (Map.Entry<Status, AtomicInteger> entry : totals.entrySet()) {
            final int i = entry.getValue().get();
            total += i;
            out.printf("%5s %s\n", i, entry.getKey());
        }

        out.printf("%5s %s\n", total, "Total");

    }

    public static enum Status {
        PASS, FAIL, ERROR;
    }
    public static class TestResult implements Comparable<TestResult> {
        private final String name;
        private final Status status;

        public TestResult(String name, Status status) {
            this.name = name;
            this.status = status;
        }

        @Override
        public int compareTo(TestResult testResult) {
            return this.name.compareTo(testResult.name);
        }
    }

    public static class TestClass implements Comparable<TestClass>{

        private final String name;
        private int failed;
        private int passed;
        private int error;
        private final List<TestResult> results = new ArrayList<TestResult>();

        public TestClass(String name) {
            this.name = name;
        }

        public void addStatus(String status, String testName) {
            results.add(new TestResult(testName, Status.valueOf(status)));
            if ("PASS".equals(status)) passed++;
            if ("FAIL".equals(status)) failed++;
            if ("ERROR".equals(status)) error++;
        }

        public List<TestResult> getResults() {
            Collections.sort(results);
            return results;
        }

        public boolean hasFailures() {
            return failed > 0 || error > 0;
        }

        @Override
        public int compareTo(TestClass o) {
            return this.name.compareTo(o.name);
        }
    }
}
