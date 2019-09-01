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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="dir" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="jar" type="{http://www.openejb.org/System/Configuration}JarFileLocation" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Deployments")
public class Deployments {

    @XmlAttribute
    protected String dir;
    @XmlAttribute
    protected String file;
    @XmlAttribute
    protected boolean autoDeploy;

    @XmlTransient
    protected ClassLoader classpath;

    public boolean isAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(final boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    /**
     * Gets the value of the dir property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDir(final String value) {
        this.dir = value;
    }


    public Deployments dir(final String name) {
        setDir(name);
        return this;
    }

    /**
     * Gets the value of the jar property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the jar property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFile(final String value) {
        this.file = value;
    }

    public Deployments file(final String name) {
        setFile(name);
        return this;
    }


    public ClassLoader getClasspath() {
        return classpath;
    }

    public void setClasspath(final ClassLoader classpath) {
        this.classpath = classpath;
    }
}
