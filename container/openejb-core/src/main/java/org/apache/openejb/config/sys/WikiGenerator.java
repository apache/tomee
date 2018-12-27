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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WikiGenerator {

    private final PrintWriter out;

    public static void main(final String[] args) throws Exception {
        System.out.println();
        System.out.println();
        System.out.println();

        new WikiGenerator("org.apache.tomee", new PrintWriter(System.out)).generate();
//        new WikiGenerator("org.apache.tomee", new PrintWriter(new File("/Users/dblevins/work/all/website/content/containers-and-resources.mdtext"))).generate();

        System.out.println();
        System.out.println();
        System.out.println();
    }

    protected ServicesJar servicesJar;

    public WikiGenerator(final String providerName, final PrintWriter printWriter) throws OpenEJBException {
        this(JaxbOpenejb.readServicesJar(providerName), printWriter);
    }

    public WikiGenerator(final ServicesJar servicesJar, final PrintWriter out) {
        this.servicesJar = servicesJar;
        this.out = out;
    }

    public void generate() throws Exception {

        // generate containers
        final List<ServiceProvider> serviceProvider = servicesJar.getServiceProvider();
        serviceProvider.sort(new Comparator<ServiceProvider>() {
            @Override
            public int compare(final ServiceProvider o1, final ServiceProvider o2) {
                return grade(o2) - grade(o1);
            }

            private int grade(final ServiceProvider i) {
                final String name = i.getClassName();
                if (name.contains("stateless")) {
                    return 10;
                }
                if (name.contains("stateful")) {
                    return 9;
                }
                if (name.contains("singleton")) {
                    return 8;
                }
                if (name.contains("mdb")) {
                    return 7;
                }
                if (name.contains("managed")) {
                    return 6;
                }
                return 0;
            }
        });

        for (final ServiceProvider provider : serviceProvider) {
            if ("Container".equals(provider.getService())) {
                generateService(provider, "Container");
            }
        }
        out.println();

        final ArrayList<String> seen = new ArrayList<>();
        for (final ServiceProvider provider : servicesJar.getServiceProvider()) {
            if ("Resource".equals(provider.getService())) {

                if (seen.containsAll(provider.getTypes())) {
                    continue;
                }

                generateService(provider, "Resource");

                seen.addAll(provider.getTypes());
            }
        }
        out.println();
        out.flush();
    }

    private void generateService(final ServiceProvider provider, final String serviceType) {
        final String type = provider.getTypes().get(0);
        out.println("# " + type + " <small>" + serviceType + " </small>");
        out.println();
        out.println("Declarable in openejb.xml via");
        out.println();
        out.println("    <" + provider.getService() + " id=\"Foo\" type=\"" + type + "\">");
        out.println("    </" + provider.getService() + ">");
        out.println();
        out.println("Declarable in properties via");
        out.println();
        out.println("    Foo = new://" + provider.getService() + "?type=" + type);
        out.println();
        final SuperProperties properties = (SuperProperties) provider.getProperties();

        final Map<String, String> defaults = new LinkedHashMap<>();

        if (properties.size() > 0) {
            out.println("## Properties");
            out.println();
            for (final Object key : properties.keySet()) {
                if (key instanceof String) {
                    final String name = (String) key;

                    final Map<String, String> attributes = properties.getAttributes(name);

                    if (attributes.containsKey("hidden")) {
                        continue;
                    }

                    out.println("### " + key);
                    out.println();

                    final String value = properties.getProperty(name);

                    String comment = properties.getComment(name);

                    comment = scrubText(comment);

                    defaults.put(name, String.valueOf(value));

                    if (comment.length() == 0) {
                        comment = "No description.";
                    }

                    out.println(comment);
                    out.println();
                }
            }

            out.println("## Default declaration");

            out.println("    <" + provider.getService() + " id=\"" + provider.getId() + "\" type=\"" + type + "\">");
            for (final Map.Entry<String, String> entry : defaults.entrySet()) {
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
        if (text == null) {
            text = "";
        }
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
