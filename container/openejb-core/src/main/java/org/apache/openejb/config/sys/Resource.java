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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string"&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="jar" type="{http://www.openejb.org/System/Configuration}JarFileLocation" /&gt;
 *       &lt;attribute name="jndi" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="provider" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Resource")
public class Resource extends AbstractService {
    @XmlAttribute
    protected String jndi;

    @XmlAttribute(name = "post-construct")
    protected String postConstruct;

    @XmlAttribute(name = "pre-destroy")
    protected String preDestroy;

    @XmlAttribute(name = "aliases")
    @XmlJavaTypeAdapter(ListAdapter.class)
    protected List<String> aliases = new ArrayList<String>();

    @XmlAttribute(name = "depends-on")
    @XmlJavaTypeAdapter(ListAdapter.class)
    protected List<String> dependsOn = new ArrayList<>();

    public Resource(final String id) {
        super(id);
    }

    public Resource(final String id, final String type) {
        super(id, type);
    }

    public Resource(final String id, final String type, final String provider) {
        super(id, type, provider);
    }

    public Resource() {
    }

    /**
     * Gets the value of the jndi property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getJndi() {
        return jndi;
    }

    /**
     * Sets the value of the jndi property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJndi(final String value) {
        this.jndi = value;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public String getPostConstruct() {
        return postConstruct;
    }

    public void setPostConstruct(final String postConstruct) {
        this.postConstruct = postConstruct;
    }

    public String getPreDestroy() {
        return preDestroy;
    }

    public void setPreDestroy(final String preDestroy) {
        this.preDestroy = preDestroy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Resource resource = (Resource) o;

        if (!Objects.equals(jndi, resource.jndi)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (jndi != null ? jndi.hashCode() : 0);
        return result;
    }
}
