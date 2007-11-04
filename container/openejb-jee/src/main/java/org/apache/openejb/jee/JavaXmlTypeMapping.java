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
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * The java-xml-type-mapping element contains a java-type that is the
 * fully qualified name of the Java class, primitive type, or array
 * type, QName of the XML root type or anonymous type, the WSDL type
 * scope the QName applies to and the set of variable mappings for
 * each public variable within the Java class.
 * <p/>
 * Used in: java-wsdl-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "java-xml-type-mappingType", propOrder = {
    "javaType",
    "rootTypeQname",
    "anonymousTypeQname",
    "qnameScope",
    "variableMapping"
})
public class JavaXmlTypeMapping {
    @XmlElement(name = "java-type", required = true)
    protected String javaType;
    @XmlElement(name = "root-type-qname")
    protected QName rootTypeQname;
    @XmlElement(name = "anonymous-type-qname")
    protected String anonymousTypeQname;
    @XmlElement(name = "qname-scope", required = true)
    protected String qnameScope;
    @XmlElement(name = "variable-mapping")
    protected List<VariableMapping> variableMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String value) {
        this.javaType = value;
    }

    public QName getRootTypeQname() {
        return rootTypeQname;
    }

    public void setRootTypeQname(QName value) {
        this.rootTypeQname = value;
    }

    public String getAnonymousTypeQname() {
        return anonymousTypeQname;
    }

    public void setAnonymousTypeQname(String value) {
        this.anonymousTypeQname = value;
    }

    public String getQnameScope() {
        return qnameScope;
    }

    public void setQnameScope(String value) {
        this.qnameScope = value;
    }

    public List<VariableMapping> getVariableMapping() {
        if (variableMapping == null) {
            variableMapping = new ArrayList<VariableMapping>();
        }
        return this.variableMapping;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
