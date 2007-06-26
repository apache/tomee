/**
 *
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
package org.apache.openejb.config.sys;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="dir" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jar" type="{http://www.openejb.org/System/Configuration}JarFileLocation" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Deployments")
public class Deployments {

    @XmlAttribute
    protected String dir;
    @XmlAttribute
    protected String jar;
    @XmlTransient
    protected ClassLoader classpath;

    /**
     * Gets the value of the dir property.
     *
     * @return possible object is
     *         {@link String }
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
    public void setDir(String value) {
        this.dir = value;
    }

    /**
     * Gets the value of the jar property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getJar() {
        return jar;
    }

    /**
     * Sets the value of the jar property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJar(String value) {
        this.jar = value;
    }


    public ClassLoader getClasspath() {
        return classpath;
    }

    public void setClasspath(ClassLoader classpath) {
        this.classpath = classpath;
    }
}
