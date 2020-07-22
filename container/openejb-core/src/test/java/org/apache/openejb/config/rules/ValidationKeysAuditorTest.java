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
package org.apache.openejb.config.rules;

import org.apache.openejb.config.rules.KeysAnnotationVisitor.ClassInfo;
import org.apache.openejb.config.rules.KeysAnnotationVisitor.MethodInfo;
import org.apache.openejb.loader.IO;
import org.apache.xbean.asm8.ClassReader;
import org.apache.xbean.asm8.ClassWriter;
import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.Page;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.apache.openejb.util.URLs.toFile;

/**
 * This class is not meant to be a container for tests. Its an audit tool which generates a report of how many of the keys listed in
 * org.apache.openejb.config.rules.Messages.properties have tests written for them
 */
public class ValidationKeysAuditorTest {
    private static Set<String> allKeys;

    @BeforeClass
    public static void init() {
        final ResourceBundle bundle = ResourceBundle.getBundle("org.apache.openejb.config.rules.Messages");
        allKeys = bundle.keySet();
        allKeys = stripPrefixes(allKeys);
    }

    @Test
    public void audit() {
        final URL resource = ValidationKeysAuditorTest.class.getResource("/org/apache/openejb/config/rules/Keys.class");
        final File file = toFile(resource);
        final KeysAnnotationVisitor visitor = new KeysAnnotationVisitor();
        dir(file.getParentFile(), visitor);
        try {
            generateReport(file, visitor);
            final String confluenceOutput = generateConfluenceReport(file, visitor);
            writeToConfluence(confluenceOutput);
        } catch (final IOException e) {
            // ignore it
            e.printStackTrace();
        }
    }

    /**
     * This method will write to confluence only if you supply the system properties confluenceUsername and confluencePassword example usage mvn test
     * -Dtest=ValidationKeysAuditorTest -DconfluenceUsername=<<your username>> -DconfluencePassword=<<your password>>
     *
     * @param confluenceOutput
     */
    private void writeToConfluence(final String confluenceOutput) {
        final String username = System.getProperty("confluenceUsername");
        final String password = System.getProperty("confluencePassword");
        if (validate(username) && validate(password)) {
            try {
                final String endpoint = "https://cwiki.apache.org/confluence/rpc/xmlrpc";
                final Confluence confluence = new Confluence(endpoint);
                confluence.login(username, password);
                final Page page = confluence.getPage("OPENEJB", "Validation Keys Audit Report");
                page.setContent(confluenceOutput);
                confluence.storePage(page);
                confluence.logout();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validate(final String str) {
        if (str == null)
            return false;
        if ("true".equalsIgnoreCase(str))
            return false;
        if ("false".equalsIgnoreCase(str))
            return false;
        if (str.trim().length() == 0)
            return false;
        return true;
    }

    private String generateConfluenceReport(final File file, final KeysAnnotationVisitor visitor) throws IOException {
        final StringBuilder output = new StringBuilder();
        final String newLine = System.getProperty("line.separator");
        final Set<String> testedKeys = getTestedKeys(visitor.classInfos);
        Set<String> untestedKeys = getUntestedKeys(testedKeys);
        untestedKeys = new TreeSet<>(untestedKeys);// sort the keys
        prepareConfluenceSummary(untestedKeys, output, newLine);
        prepareConfluenceUntestedKeyList(untestedKeys, output, newLine);
        prepareConfluenceTestedKeysDetailedReport(visitor.classInfos, output, newLine);
        writeToFile(file, output, "ValidationKeyAuditReportConfluence.txt");
        return output.toString();
    }

    private void prepareConfluenceTestedKeysDetailedReport(final HashSet<ClassInfo> classInfos, final StringBuilder output, final String newLine) {
        final TreeMap<String, TreeSet<String>> info = new TreeMap<>();
        output.append("h2.List of keys which have been tested.").append(newLine);
        output.append("{table-plus:autoNumber=true}").append(newLine);
        output.append("|| Key | Method which tests the key ||").append(newLine);
        for (final ClassInfo classInfo : classInfos) {
            final HashSet<MethodInfo> methuds = classInfo.methuds;
            for (final MethodInfo methodInfo : methuds) {
                final HashSet<String> keys = methodInfo.keys;
                for (final String key : keys) {
                    if (!info.containsKey(key)) {
                        final TreeSet<String> set = new TreeSet<>();
                        set.add(createConfluenceLink(classInfo.clazz + "." + methodInfo.methud + "()"));
                        info.put(key, set);
                    } else {
                        final TreeSet<String> set = info.get(key);
                        set.add(createConfluenceLink(classInfo.clazz + "." + methodInfo.methud + "()"));
                    }
                }
            }
        }
        final Set<Entry<String, TreeSet<String>>> entrySet = info.entrySet();
        for (final Entry<String, TreeSet<String>> entry : entrySet) {
            final String key = entry.getKey();
            output.append("| ").append(key).append(" | ");
            int count = 0;
            final TreeSet<String> values = entry.getValue();
            for (final String value : values) {
                if (count > 0) {
                    output.append(" \\\\ ");
                }
                output.append(value);
                ++count;
            }
            output.append(newLine);
        }
        output.append("{table-plus}").append(newLine);
    }

    private String createConfluenceLink(final String string) {
        String link = "[" + string + " | ";
        final String temp = string.substring(0, string.lastIndexOf('.'));
        final String location = "https://svn.apache.org/viewvc/openejb/trunk/openejb3/container/openejb-core/src/test/java/" + temp + ".java?revision=HEAD&view=markup ]";
        link = link + location;
        return link;
    }

    private void prepareConfluenceUntestedKeyList(final Set<String> untestedKeys, final StringBuilder output, final String newLine) {
        output.append("{table-plus:autoNumber=true}").append(newLine);
        output.append("|| h2.List of untested keys \\\\ ||").append(newLine);
        for (final String key : untestedKeys) {
            output.append(" | ").append(key).append(" | ").append(newLine);
        }
        output.append("{table-plus}").append(newLine);
    }

    private void prepareConfluenceSummary(final Set<String> untestedKeys, final StringBuilder output, final String newLine) {
        final int total = allKeys.size();
        final int untested = untestedKeys.size();
        final int tested = total - untested;
        final double coverage = (((tested + 0.0) / (total + 0.0)) * 100);
        output.append("{warning:title=Warning}This page is auto-generated. Any manual changes would be over-written the next time this page is regenerated{warning}").append(
            newLine);
        output.append("{info:title=Audit Result}h2.Out of a total of ").append(total).append(" keys, ").append(tested).append(" have been tested. Test coverage for keys is ").append(coverage).append(" %.{info}")
            .append(newLine);
    }

    private void generateReport(final File file, final KeysAnnotationVisitor visitor) throws IOException {
        final StringBuilder output = new StringBuilder();
        final String newLine = System.getProperty("line.separator");
        final Set<String> testedKeys = getTestedKeys(KeysAnnotationVisitor.classInfos);
        Set<String> untestedKeys = getUntestedKeys(testedKeys);
        untestedKeys = new TreeSet<>(untestedKeys);// sort the keys
        prepareSummary(untestedKeys, output, newLine);
        prepareUntestedKeyList(untestedKeys, output, newLine);
        prepareTestedKeysDetailedReport(KeysAnnotationVisitor.classInfos, output, newLine);
        writeToFile(file, output, "ValidationKeyAuditReport.txt");
    }

    private void prepareTestedKeysDetailedReport(final HashSet<ClassInfo> classInfos, final StringBuilder output, final String newLine) throws IOException {
        output.append("================================================================================================").append(newLine);
        output.append("List of all keys tested. Next to each is the the test method which tests the key").append(newLine);
        output.append("================================================================================================").append(newLine);
        final TreeMap<String, TreeSet<String>> info = new TreeMap<>();
        for (final ClassInfo classInfo : classInfos) {
            final HashSet<MethodInfo> methuds = classInfo.methuds;
            for (final MethodInfo methodInfo : methuds) {
                final HashSet<String> keys = methodInfo.keys;
                for (final String key : keys) {
                    if (!info.containsKey(key)) {
                        final TreeSet<String> set = new TreeSet<>();
                        set.add(classInfo.clazz + "." + methodInfo.methud + "()");
                        info.put(key, set);
                    } else {
                        final TreeSet<String> set = info.get(key);
                        set.add(classInfo.clazz + "." + methodInfo.methud + "()");
                    }
                }
            }
        }
        final Set<Entry<String, TreeSet<String>>> entrySet = info.entrySet();
        for (final Entry<String, TreeSet<String>> entry : entrySet) {
            final String key = entry.getKey();
            output.append(key).append(" --> ");
            int count = 0;
            final TreeSet<String> values = entry.getValue();
            for (final String value : values) {
                if (count > 0) {
                    // put as many spaces as there are characters in the key to indent and align multiple values for the same key
                    final StringBuilder spaces = new StringBuilder();
                    for (int i = 0; i < key.length(); i++) {
                        spaces.append(" ");
                    }
                    output.append(spaces).append(" --> ");
                }
                output.append(value).append(newLine);
                ++count;
            }
        }
    }

    private void writeToFile(final File file, final StringBuilder output, final String fileName) throws IOException {
        final File surefireReports = new File(dir(file), "surefire-reports");
        if (!surefireReports.exists()) {
            surefireReports.mkdir();
        }
        final File auditResults = new File(surefireReports, fileName);
        BufferedWriter bw = null;
        try {
            final FileWriter writer = new FileWriter(auditResults);
            bw = new BufferedWriter(writer);
            bw.append(output.toString());
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                } catch (final Throwable e) {
                    //Ignore
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (final Throwable e) {
                    //Ignore
                }
            }
        }
    }

    private void prepareUntestedKeyList(final Set<String> untestedKeys, final StringBuilder output, final String newLine) {
        output.append("================================================================================================").append(newLine);
        output.append("List of all keys which have not been tested yet.").append(newLine);
        output.append("================================================================================================").append(newLine);
        for (final String key : untestedKeys) {
            output.append(key).append(newLine);
        }
    }

    private Set<String> getUntestedKeys(final Set<String> testedKeys) {
        final Set<String> untestedKeys = new HashSet<>();
        for (final String key : allKeys) {
            if (!testedKeys.contains(key))
                untestedKeys.add(key);
        }
        return untestedKeys;
    }

    private Set<String> getTestedKeys(final HashSet<ClassInfo> classInfos) {
        final Set<String> testedKeys = new HashSet<>();
        for (final ClassInfo classInfo : classInfos) {
            final HashSet<MethodInfo> methuds = classInfo.methuds;
            for (final MethodInfo methodInfo : methuds) {
                testedKeys.addAll(methodInfo.keys);
            }
        }
        return testedKeys;
    }

    private static Set<String> stripPrefixes(final Set<String> allKeys) {
        final Set<String> keys = new HashSet<>();
        for (String key : allKeys) {
            key = key.substring(key.indexOf('.') + 1);
            if (!keys.contains(key))
                keys.add(key);
        }
        return keys;
    }

    private void prepareSummary(final Set<String> untestedKeys, final StringBuilder output, final String newLine) {
        final int total = allKeys.size();
        final int untested = untestedKeys.size();
        final int tested = total - untested;
        final double coverage = (((tested + 0.0) / (total + 0.0)) * 100);
        output.append("================================================================================================").append(newLine);
        output.append("Out of a total of ").append(total).append(" keys, ").append(tested).append(" have been tested. Test coverage for keys is ").append(coverage).append(" %.").append(newLine);
        output.append("================================================================================================").append(newLine);
    }

    private File dir(File file) {
        file = file.getParentFile();
        while (file.isDirectory()) {
            if (!file.getName().equals("target")) {
                file = file.getParentFile();
                continue;
            }
            return file;
        }
        return null;
    }

    private static void dir(final File dir, final KeysAnnotationVisitor visitor) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    dir(file, visitor);
                } else if (file.getName().endsWith(".class")) {
                    file(file, visitor);
                }
            }
        }
    }

    private static void file(final File file, final KeysAnnotationVisitor visitor) {
        try {
            final InputStream in = IO.read(file);
            try {
                final ClassReader classReader = new ClassReader(in);
                classReader.accept(visitor, ClassWriter.COMPUTE_FRAMES);
            } finally {
                IO.close(in);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
