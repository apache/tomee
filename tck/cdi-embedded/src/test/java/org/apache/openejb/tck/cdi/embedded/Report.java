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
import java.util.Collections;
import java.util.LinkedList;

/**
 * @version $Rev$ $Date$
 */
public class Report {

    public static void main(String[] args) throws Exception {
        new Report().main();
    }

    private final LinkedList<TestClass> classes = new LinkedList<TestClass>();

    private void main() throws Exception {
        final File file = new File("/Users/dblevins/work/uber/openejb/tck/cdi-embedded/target/surefire-reports/testng-results.xml");

        final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        parser.parse(file, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                final String name = qName;
                if ("class".equals(name)) {
                    classes.add(new TestClass(attributes.getValue("name")));
                }

                if ("test-method".equals(name)) {
                    classes.getLast().addStatus(attributes.getValue("status"));
                }
            }
        });

        Collections.sort(classes);

        int i = 0;
        for (TestClass testClass : classes) {
            if (!testClass.hasFailures()) continue;
            System.out.printf("<class name=\"%s\"/>\n", testClass.name);
            i++;
        }

        System.out.println(i);
    }

    public static class TestClass implements Comparable<TestClass>{

        private final String name;
        private int failed;
        private int passed;
        private int error;

        public TestClass(String name) {
            this.name = name;
        }

        public void addStatus(String status) {
            if ("PASS".equals(status)) passed++;
            if ("FAIL".equals(status)) failed++;
            if ("ERROR".equals(status)) error++;
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
