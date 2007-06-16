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
package org.apache.openejb.jee.oejb2;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Stack;

/**
 * @version $Rev$ $Date$
 */
public class NamespaceFilter extends XMLFilterImpl {

    static Map<String, String> ns = new HashMap();
    private String previousNs;

    static {
        ns.put("abstract-naming-entry", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("admin-object-link", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("admin-object-module", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("alt-dd", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("application", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("attribute", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("auto-increment-table", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("batch-size", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("binding-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("client-environment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("clustering", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("cmp-connection-factory", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("connector", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("credentials-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("css", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("css-link", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("custom-generator", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("dependencies", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("dependency", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("ejb", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("ejb-link", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("ejb-local-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("ejb-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("environment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("ext-module", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("external-path", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("filter", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("gbean", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("gbean-link", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("gbean-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("generator-name", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("hidden-classes", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("host", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("identity-column", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("import", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("internal-path", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("inverse-classloading", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("java", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("key", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("key-generator", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("message-destination", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("message-destination-link", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("message-destination-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("moduleId", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("non-overridable-classes", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("ns-corbaloc", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("persistence-context-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("persistence-context-ref-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("persistence-context-type", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("persistence-unit-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("persistence-unit-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("persistence-unit-ref-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("port", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("port-completion", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("port-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("primary-key-class", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("property", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("protocol", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("ref-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("ref-type", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("reference", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("references", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("resource-adapter", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("resource-env-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("resource-link", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("resource-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("return-type", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("security", "http://geronimo.apache.org/xml/ns/security-2.0");
        ns.put("sequence-name", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("sequence-table", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("server-environment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("service", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("service-completion", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("service-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("service-ref", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("service-ref-name", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("sql", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("sql-generator", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("suppress-default-environment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("type", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("uri", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("url", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("uuid", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        ns.put("value", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("web", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2");
        ns.put("web-container", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("workmanager", "http://geronimo.apache.org/xml/ns/naming-1.2");
        ns.put("xml-attribute", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        ns.put("xml-reference", "http://geronimo.apache.org/xml/ns/deployment-1.2");
    }

    static final Map<String,List<String>> duplicates = new HashMap<String,List<String>>();

    static {
        duplicates.put("artifactId", Arrays.asList("http://geronimo.apache.org/xml/ns/deployment-1.2", "http://geronimo.apache.org/xml/ns/naming-1.2"));
        duplicates.put("groupId", Arrays.asList("http://geronimo.apache.org/xml/ns/deployment-1.2", "http://geronimo.apache.org/xml/ns/naming-1.2"));
        duplicates.put("module", Arrays.asList("http://geronimo.apache.org/xml/ns/j2ee/application-1.2", "http://geronimo.apache.org/xml/ns/deployment-1.2", "http://geronimo.apache.org/xml/ns/naming-1.2"));
        duplicates.put("name", Arrays.asList("http://geronimo.apache.org/xml/ns/deployment-1.2", "http://geronimo.apache.org/xml/ns/naming-1.2", "http://geronimo.apache.org/xml/ns/security-2.0"));
        duplicates.put("pattern", Arrays.asList("http://geronimo.apache.org/xml/ns/deployment-1.2", "http://geronimo.apache.org/xml/ns/naming-1.2"));
        duplicates.put("version", Arrays.asList("http://geronimo.apache.org/xml/ns/deployment-1.2", "http://geronimo.apache.org/xml/ns/naming-1.2"));
        duplicates.put("table-name", Arrays.asList("http://openejb.apache.org/xml/ns/openejb-jar-2.2", "http://openejb.apache.org/xml/ns/pkgen-2.1"));
    }

    public NamespaceFilter(XMLReader xmlReader) {
        super(xmlReader);
    }

    private final Stack<String> visibleNamespaces = new Stack<String>();

    public void startDocument() throws SAXException {
        visibleNamespaces.push("");
        super.startDocument();
    }

    //String uri, String localName, String qName, Attributes atts
    public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
        if (uri.startsWith("http://www.openejb.org/xml/ns/openejb-jar-2")){
            uri = "http://openejb.apache.org/xml/ns/openejb-jar-2.2";
        }

        String previousNs = visibleNamespaces.peek();

        String correctNamespace = ns.get(localName);
        boolean correctable = (uri.equals("http://openejb.apache.org/xml/ns/openejb-jar-2.2") || uri.equals("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0"));
        if (correctable && correctNamespace != null) {
            uri = correctNamespace;
        } else if (duplicates.containsKey(localName)){
            List<String> possibleNamespaces = duplicates.get(localName);
            if (possibleNamespaces.contains(uri)){
                // nothing to modify
            } else if (possibleNamespaces.contains(previousNs)){
                uri = previousNs;
            } else {
                uri = possibleNamespaces.get(0);
            }
        } else if (correctable && !previousNs.equals(uri) && !previousNs.equals("")) {
            uri = previousNs;
        }

        visibleNamespaces.push(uri);
        super.startElement(uri, localName, qname, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        visibleNamespaces.pop();
        super.endElement(uri, localName, qName);
    }
}
