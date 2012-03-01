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

import static org.apache.openejb.util.URLs.toFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.openejb.config.rules.KeysAnnotationVisitor.ClassInfo;
import org.apache.openejb.config.rules.KeysAnnotationVisitor.MethodInfo;
import org.apache.openejb.loader.IO;
import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.ClassWriter;
import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.Page;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is not meant to be a container for tests. Its an audit tool which generates a report of how many of the keys listed in
 * org.apache.openejb.config.rules.Messages.properties have tests written for them
 */
public class ValidationKeysAuditorTest {
    private static Set<String> allKeys;

    @BeforeClass
    public static void init() {
        ResourceBundle bundle = ResourceBundle.getBundle("org.apache.openejb.config.rules.Messages");
        allKeys = bundle.keySet();
        allKeys = stripPrefixes(allKeys);
    }

    @Test
    public void audit() {
        URL resource = ValidationKeysAuditorTest.class.getResource("/org/apache/openejb/config/rules/Keys.class");
        File file = toFile(resource);
        KeysAnnotationVisitor visitor = new KeysAnnotationVisitor();
        dir(file.getParentFile(), visitor);
        try {
            generateReport(file, visitor);
            String confluenceOutput = generateConfluenceReport(file, visitor);
            writeToConfluence(confluenceOutput);
        } catch (IOException e) {
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
    private void writeToConfluence(String confluenceOutput) {
        String username = System.getProperty("confluenceUsername");
        String password = System.getProperty("confluencePassword");
        if (validate(username) && validate(password)) {
            try {
                String endpoint = "https://cwiki.apache.org/confluence/rpc/xmlrpc";
                Confluence confluence = new Confluence(endpoint);
                confluence.login(username, password);
                Page page = confluence.getPage("OPENEJB", "Validation Keys Audit Report");
                page.setContent(confluenceOutput);
                confluence.storePage(page);
                confluence.logout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validate(String str) {
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

    private String generateConfluenceReport(File file, KeysAnnotationVisitor visitor) throws IOException {
        StringBuilder output = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        Set<String> testedKeys = getTestedKeys(visitor.classInfos);
        Set<String> untestedKeys = getUntestedKeys(testedKeys);
        untestedKeys = new TreeSet<String>(untestedKeys);// sort the keys
        prepareConfluenceSummary(untestedKeys, output, newLine);
        prepareConfluenceUntestedKeyList(untestedKeys, output, newLine);
        prepareConfluenceTestedKeysDetailedReport(visitor.classInfos, output, newLine);
        writeToFile(file, output, "ValidationKeyAuditReportConfluence.txt");
        return output.toString();
    }

    private void prepareConfluenceTestedKeysDetailedReport(HashSet<ClassInfo> classInfos, StringBuilder output, String newLine) {
        TreeMap<String, TreeSet<String>> info = new TreeMap<String, TreeSet<String>>();
        output.append("h2.List of keys which have been tested.").append(newLine);
        output.append("{table-plus:autoNumber=true}").append(newLine);
        output.append("|| Key | Method which tests the key ||").append(newLine);
        for (ClassInfo classInfo : classInfos) {
            HashSet<MethodInfo> methuds = classInfo.methuds;
            for (MethodInfo methodInfo : methuds) {
                HashSet<String> keys = methodInfo.keys;
                for (String key : keys) {
                    if (!info.containsKey(key)) {
                        TreeSet<String> set = new TreeSet<String>();
                        set.add(createConfluenceLink(classInfo.clazz + "." + methodInfo.methud + "()"));
                        info.put(key, set);
                    } else {
                        TreeSet<String> set = info.get(key);
                        set.add(createConfluenceLink(classInfo.clazz + "." + methodInfo.methud + "()"));
                    }
                }
            }
        }
        Set<Entry<String, TreeSet<String>>> entrySet = info.entrySet();
        for (Entry<String, TreeSet<String>> entry : entrySet) {
            String key = entry.getKey();
            output.append("| ").append(key).append(" | ");
            int count = 0;
            TreeSet<String> values = entry.getValue();
            for (String value : values) {
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

    private String createConfluenceLink(String string) {
        String link = "[" + string + " | ";
        String temp = string.substring(0, string.lastIndexOf("."));
        String location = "https://svn.apache.org/viewvc/openejb/trunk/openejb3/container/openejb-core/src/test/java/" + temp + ".java?revision=HEAD&view=markup ]";
        link = link + location;
        return link;
    }

    private void prepareConfluenceUntestedKeyList(Set<String> untestedKeys, StringBuilder output, String newLine) {
        output.append("{table-plus:autoNumber=true}").append(newLine);
        output.append("|| h2.List of untested keys \\\\ ||").append(newLine);
        for (String key : untestedKeys) {
            output.append(" | ").append(key).append(" | ").append(newLine);
        }
        output.append("{table-plus}").append(newLine);
    }

    private void prepareConfluenceSummary(Set<String> untestedKeys, StringBuilder output, String newLine) {
        int total = allKeys.size();
        int untested = untestedKeys.size();
        int tested = total - untested;
        double coverage = (((tested + 0.0) / (total + 0.0)) * 100);
        output.append("{warning:title=Warning}This page is auto-generated. Any manual changes would be over-written the next time this page is regenerated{warning}").append(
                newLine);
        output.append("{info:title=Audit Result}h2.Out of a total of " + total + " keys, " + tested + " have been tested. Test coverage for keys is " + coverage + " %.{info}")
                .append(newLine);
    }

    private void generateReport(File file, KeysAnnotationVisitor visitor) throws IOException {
        StringBuilder output = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        Set<String> testedKeys = getTestedKeys(visitor.classInfos);
        Set<String> untestedKeys = getUntestedKeys(testedKeys);
        untestedKeys = new TreeSet<String>(untestedKeys);// sort the keys
        prepareSummary(untestedKeys, output, newLine);
        prepareUntestedKeyList(untestedKeys, output, newLine);
        prepareTestedKeysDetailedReport(visitor.classInfos, output, newLine);
        writeToFile(file, output, "ValidationKeyAuditReport.txt");
    }

    private void prepareTestedKeysDetailedReport(HashSet<ClassInfo> classInfos, StringBuilder output, String newLine) throws IOException {
        output.append("================================================================================================").append(newLine);
        output.append("List of all keys tested. Next to each is the the test method which tests the key").append(newLine);
        output.append("================================================================================================").append(newLine);
        TreeMap<String, TreeSet<String>> info = new TreeMap<String, TreeSet<String>>();
        for (ClassInfo classInfo : classInfos) {
            HashSet<MethodInfo> methuds = classInfo.methuds;
            for (MethodInfo methodInfo : methuds) {
                HashSet<String> keys = methodInfo.keys;
                for (String key : keys) {
                    if (!info.containsKey(key)) {
                        TreeSet<String> set = new TreeSet<String>();
                        set.add(classInfo.clazz + "." + methodInfo.methud + "()");
                        info.put(key, set);
                    } else {
                        TreeSet<String> set = info.get(key);
                        set.add(classInfo.clazz + "." + methodInfo.methud + "()");
                    }
                }
            }
        }
        Set<Entry<String, TreeSet<String>>> entrySet = info.entrySet();
        for (Entry<String, TreeSet<String>> entry : entrySet) {
            String key = entry.getKey();
            output.append(key).append(" --> ");
            int count = 0;
            TreeSet<String> values = entry.getValue();
            for (String value : values) {
                if (count > 0) {
                    // put as many spaces as there are characters in the key to indent and align multiple values for the same key
                    StringBuilder spaces = new StringBuilder();
                    for(int i=0;i<key.length();i++){
                        spaces.append(" ");
                    }
                    output.append(spaces).append(" --> ");
                }
                output.append(value).append(newLine);
                ++count;
            }
        }
    }

    private void writeToFile(File file, StringBuilder output, String fileName) throws IOException {
        File surefireReports = new File(dir(file), "surefire-reports");
        if (!surefireReports.exists()) {
            surefireReports.mkdir();
        }
        File auditResults = new File(surefireReports, fileName);
        BufferedWriter bw = null;
        try {
            FileWriter writer = new FileWriter(auditResults);
            bw = new BufferedWriter(writer);
            bw.append(output.toString());
        } finally {
            bw.flush();
            bw.close();
        }
    }

    private void prepareUntestedKeyList(Set<String> untestedKeys, StringBuilder output, String newLine) {
        output.append("================================================================================================").append(newLine);
        output.append("List of all keys which have not been tested yet.").append(newLine);
        output.append("================================================================================================").append(newLine);
        for (String key : untestedKeys) {
            output.append(key).append(newLine);
        }
    }

    private Set<String> getUntestedKeys(Set<String> testedKeys) {
        Set<String> untestedKeys = new HashSet<String>();
        for (String key : allKeys) {
            if (!testedKeys.contains(key))
                untestedKeys.add(key);
        }
        return untestedKeys;
    }

    private Set<String> getTestedKeys(HashSet<ClassInfo> classInfos) {
        Set<String> testedKeys = new HashSet<String>();
        for (ClassInfo classInfo : classInfos) {
            HashSet<MethodInfo> methuds = classInfo.methuds;
            for (MethodInfo methodInfo : methuds) {
                testedKeys.addAll(methodInfo.keys);
            }
        }
        return testedKeys;
    }

    private static Set<String> stripPrefixes(Set<String> allKeys) {
        Set<String> keys = new HashSet<String>();
        for (String key : allKeys) {
            key = key.substring(key.indexOf(".") + 1);
            if (!keys.contains(key))
                keys.add(key);
        }
        return keys;
    }

    private void prepareSummary(Set<String> untestedKeys, StringBuilder output, String newLine) {
        int total = allKeys.size();
        int untested = untestedKeys.size();
        int tested = total - untested;
        double coverage = (((tested + 0.0) / (total + 0.0)) * 100);
        output.append("================================================================================================").append(newLine);
        output.append("Out of a total of " + total + " keys, " + tested + " have been tested. Test coverage for keys is " + coverage + " %.").append(newLine);
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

    private static void dir(File dir, KeysAnnotationVisitor visitor) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                dir(file, visitor);
            } else if (file.getName().endsWith(".class")) {
                file(file, visitor);
            }
        }
    }

    private static void file(File file, KeysAnnotationVisitor visitor) {
        try {
            InputStream in = IO.read(file);
            try {
                ClassReader classReader = new ClassReader(in);
                classReader.accept(visitor, ClassWriter.COMPUTE_MAXS);
            } finally {
                IO.close(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
