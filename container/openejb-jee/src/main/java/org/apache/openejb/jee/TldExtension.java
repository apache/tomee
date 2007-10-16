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
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The tld-extensionType is used to indicate
 * extensions to a specific TLD element.
 * <p/>
 * It is used by elements to designate an extension block
 * that is targeted to a specific extension designated by
 * a set of extension elements that are declared by a
 * namespace. The namespace identifies the extension to
 * the tool that processes the extension.
 * <p/>
 * The type of the extension-element is abstract. Therefore,
 * a concrete type must be specified by the TLD using
 * xsi:type attribute for each extension-element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tld-extensionType", propOrder = {"extensionElement"})
public class TldExtension {
    @XmlElement(name = "extension-element", required = true)
    protected List<String> extensionElement;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(required = true)
    protected String namespace;

    public List<String> getExtensionElement() {
        if (extensionElement == null) {
            extensionElement = new ArrayList<String>();
        }
        return this.extensionElement;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String value) {
        this.namespace = value;
    }
}
