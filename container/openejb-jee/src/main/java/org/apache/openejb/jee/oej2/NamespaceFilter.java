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
package org.apache.openejb.jee.oej2;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.lang.String;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class NamespaceFilter extends XMLFilterImpl {

    static Map<String,String> correctNamespaces = new HashMap();

    static {
        correctNamespaces.put("abstract-naming-entry", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("alt-dd", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("application", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("artifactId", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("attribute", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("auto-increment-table", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("batch-size", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("client-environment", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("clustering", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("cmp-connection-factory", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("connector", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("custom-generator", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("dependencies", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("dependency", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("ejb", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("ejb-local-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("ejb-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("entity-manager-factory-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("environment", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("ext-module", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("external-path", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("filter", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("gbean", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("gbean-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("generator-name", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("groupId", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("hidden-classes", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("identity-column", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("import", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("internal-path", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("inverse-classloading", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("java", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("key-generator", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("message-destination", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("module", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("module", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("moduleId", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("non-overridable-classes", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("openejb-jar", "http://openejb.apache.org/xml/ns/openejb-jar-2.2" );
        correctNamespaces.put("pattern", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("persistence-context-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("primary-key-class", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("reference", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("references", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("resource-adapter", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("resource-env-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("resource-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("return-type", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("security", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("sequence-name", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("sequence-table", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("server-environment", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("service", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("service-ref", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("sql", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("sql-generator", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("suppress-default-environment", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("table-name", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("tss", "http://openejb.apache.org/xml/ns/openejb-jar-2.2" );
        correctNamespaces.put("type", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("uuid", "http://openejb.apache.org/xml/ns/pkgen-2.1" );
        correctNamespaces.put("version", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("web", "http://geronimo.apache.org/xml/ns/j2ee/application-1.2" );
        correctNamespaces.put("web-container", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("workmanager", "http://geronimo.apache.org/xml/ns/naming-1.2" );
        correctNamespaces.put("xml-attribute", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
        correctNamespaces.put("xml-reference", "http://geronimo.apache.org/xml/ns/deployment-1.2" );
    }

    public NamespaceFilter(XMLReader xmlReader) {
        super(xmlReader);
    }

    //String uri, String localName, String qName, Attributes atts
    public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
        String correctNamespace = correctNamespaces.get(localName);
        if (uri.equals("http://openejb.apache.org/xml/ns/openejb-jar-2.2") && correctNamespace != null){
            super.startElement(correctNamespace, localName, qname, atts);
        } else {
            super.startElement(uri, localName, qname, atts);
        }
    }
}
