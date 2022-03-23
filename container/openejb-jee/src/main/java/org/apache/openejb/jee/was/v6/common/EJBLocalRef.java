/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @since J2EE1.3 The ejb-local-refType is used by ejb-local-ref elements for
 * the declaration of a reference to an enterprise bean's local home. The
 * declaration consists of:
 *
 * - an optional description - the EJB reference name used in the code of
 * the Deployment Component that's referencing the enterprise bean - the
 * expected type of the referenced enterprise bean - the expected local
 * home and local interfaces of the referenced enterprise bean - optional
 * ejb-link information, used to specify the referenced enterprise bean
 *
 *
 *
 * Java class for EJBLocalRef complex type.
 *
 *
 * The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name="EJBLocalRef"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{common.xmi}EjbRef"&gt;
 *       &lt;attribute name="local" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="localHome" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EJBLocalRef")
public class EJBLocalRef extends EjbRef {

    @XmlAttribute
    protected String local;
    @XmlAttribute
    protected String localHome;

    /**
     * Gets the value of the local property.
     *
     * @return possible object is {@link String }
     */
    public String getLocal() {
        return local;
    }

    /**
     * Sets the value of the local property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLocal(final String value) {
        this.local = value;
    }

    /**
     * Gets the value of the localHome property.
     *
     * @return possible object is {@link String }
     */
    public String getLocalHome() {
        return localHome;
    }

    /**
     * Sets the value of the localHome property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLocalHome(final String value) {
        this.localHome = value;
    }

}
