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
package org.apache.openejb.config.sys;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.SuperProperties;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WikiGenerator {

    private PrintWriter out;

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println();
        System.out.println();

        new WikiGenerator("org.apache.openejb", new PrintWriter(System.out)).generate();
//        new WikiGenerator("org.apache.openejb", new PrintWriter(new File("/Users/dblevins/work/all/website/content/containers-and-resources.mdtext"))).generate();

        System.out.println();
        System.out.println();
        System.out.println();
    }

    protected ServicesJar servicesJar;

    public WikiGenerator(String providerName, PrintWriter printWriter) throws OpenEJBException {
        this(JaxbOpenejb.readServicesJar(providerName), printWriter);
    }

    public WikiGenerator(ServicesJar servicesJar, PrintWriter out) {
        this.servicesJar = servicesJar;
        this.out = out;
    }

    public void generate() throws Exception {

        // generate containers
        List<ServiceProvider> serviceProvider = servicesJar.getServiceProvider();
        Collections.sort(serviceProvider, new Comparator<ServiceProvider>() {
            @Override
            public int compare(ServiceProvider o1, ServiceProvider o2) {
                return grade(o2) - grade(o1);
            }

            private int grade(ServiceProvider i) {
                String name = i.getClassName();
                if (name.contains("stateless")) return 10;
                if (name.contains("stateful")) return 9;
                if (name.contains("singleton")) return 8;
                if (name.contains("mdb")) return 7;
                if (name.contains("managed")) return 6;
                return 0;
            }
        });

        for (ServiceProvider provider : serviceProvider) {
            if ("Container".equals(provider.getService())) {
                generateService(provider, "Container");
            }
        }
        out.println();

        ArrayList<String> seen = new ArrayList<String>();
        for (ServiceProvider provider : servicesJar.getServiceProvider()) {
            if ("Resource".equals(provider.getService())) {

                if (seen.containsAll(provider.getTypes())) continue;

                generateService(provider, "Resource");

                seen.addAll(provider.getTypes());
            }
        }
        out.println();
        out.flush();
    }

    private void header(String... items) {
        out.print("<tr>");
        for (String item : items) {
            out.print("<th>");
            out.print(item);
            out.print("</th>");
        }
        out.println("</tr>");
    }

    private void row(String... items) {
        out.print("<tr>");
        for (String item : items) {
            out.print("<td>");
            out.print(item);
            out.print("</td>");
        }
        out.println("</tr>");
    }

    private void generateService(ServiceProvider provider, String serviceType) {
        String type = provider.getTypes().get(0);
        out.println("# " + type + " <small>" + serviceType + " </small>");
        out.println();
        out.println("Declarable in openejb.xml via");
        out.println();
        out.println("    <" + provider.getService() + " id=\"Foo\" type=\"" + type + "\">");
        out.println("    </" + provider.getService() + ">");
        out.println();
        out.println("Declarable in properties via");
        out.println();
        out.println("    Foo = new://" + provider.getService() + "?type=" + type + "");
        out.println();
        SuperProperties properties = (SuperProperties) provider.getProperties();

        Map<String, String> defaults = new LinkedHashMap<String, String>();

        if (properties.size() > 0) {
            out.println("## Properties");
            out.println();
            for (Object key : properties.keySet()) {
                if (key instanceof String) {
                    final String name = (String) key;

                    final Map<String, String> attributes = properties.getAttributes(name);

                    if (attributes.containsKey("hidden")) continue;

                    out.println("### " + key);
                    out.println();

                    final String value = properties.getProperty(name);

                    String comment = properties.getComment(name);

                    comment = scrubText(comment);

                    defaults.put(name, value + "");

                    if (comment.length() == 0) comment = "No description.";

                    out.println(comment);
                    out.println();
                }
            }

            out.println("## Default declaration");

            out.println("    <" + provider.getService() + " id=\"" + provider.getId() + "\" type=\"" + type + "\">");
            for (Map.Entry<String, String> entry : defaults.entrySet()) {
                out.print("        ");
                out.print(entry.getKey());
                out.print(" = ");
                out.println(entry.getValue());
            }
            out.println("    </" + provider.getService() + ">");
        } else {
            out.println("No properties.");
        }
        out.println();
    }

    private String scrubText(String text) {
        if (text == null) text = "";
        return text;
//        text = text.replaceAll("\r?\n", "\\\\\\\\ ");
//        text = text.replaceAll("\\*", "\\\\*");
//        text = text.replaceAll("\\_", "\\\\_");
//        text = text.replaceAll("\\?", "\\\\?");
//        text = text.replaceAll("\\-", "\\\\-");
//        text = text.replaceAll("\\^", "\\\\^");
//        text = text.replaceAll("\\~", "\\\\~");
//        text = text.replaceAll("\\#", "\\\\#");
//        text = text.replaceAll("\\[", "\\\\[");
//        text = text.replaceAll("\\]", "\\\\]");
//        text = text.replaceAll("\\{", "\\\\{");
//        text = text.replaceAll("\\}", "\\\\}");
//        text = text.replaceAll("\\(", "\\\\(");
//        text = text.replaceAll("\\)", "\\\\)");
//        text = text.replaceAll("http:", "{html}http:{html}");
//        text = text.replaceAll("file:", "{html}file:{html}");
//        text = text.replaceAll("    ", "{html}&nbsp;&nbsp;&nbsp;&nbsp;{html}");
//        text = text.replaceAll("   ", "{html}&nbsp;&nbsp;&nbsp;{html}");
//        text = text.replaceAll("  ", "{html}&nbsp;&nbsp;{html}");
//        return text;
    }
}
