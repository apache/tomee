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

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.ArrayList;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.SuperProperties;

public class WikiGenerator {

    private PrintWriter out;

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println();
        System.out.println();
        
        new WikiGenerator("org.apache.openejb", new PrintWriter(new File("/Users/dblevins/work/all/website/content/containers-and-resources.mdtext"))).generate();

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
        out.println("# Containers");
        for (ServiceProvider provider : servicesJar.getServiceProvider()) {
            if ("Container".equals(provider.getService())) {
                generateService(provider, "container");
            }
        }
        out.println();

        out.println("# Resources");
        ArrayList<String> seen = new ArrayList<String>();
        for (ServiceProvider provider : servicesJar.getServiceProvider()) {
            if ("Resource".equals(provider.getService())) {

                if (seen.containsAll(provider.getTypes())) continue;

                generateService(provider, "resource");

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
        out.println("## " + type);
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
        if (properties.size() > 0) {
            out.println("Supports the following properties");
            out.println();
            out.println("<table>");
            header("Property Name", "Description");

            for (Object key : properties.keySet()) {
                if (key instanceof String) {
                    String name = (String) key;

                    Map<String, String> attributes = properties.getAttributes(name);
                    if (!attributes.containsKey("hidden")) {
                        String value = properties.getProperty(name);
                        String comment = properties.getComment(name);

                        comment = scrubText(comment);

                        if (value != null && value.length() > 0) {
                            comment += "\n\nDefault value is <code>" + scrubText(value) + "</code>";
                        }

                        if (comment.length() == 0) comment = "No description.";

                        row(name, comment);
                    }
                }
            }
            out.println("</table>");
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
