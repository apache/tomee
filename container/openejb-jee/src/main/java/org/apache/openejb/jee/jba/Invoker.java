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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.jba;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "invokerProxyBindingName",
    "jndiName",
    "ejbRef"
})
@XmlRootElement(name = "invoker")
public class Invoker {

    @XmlElement(name = "invoker-proxy-binding-name", required = true)
    protected String invokerProxyBindingName;
    @XmlElement(name = "jndi-name")
    protected JndiName jndiName;
    @XmlElement(name = "ejb-ref")
    protected List<EjbRef> ejbRef;

    /**
     * Gets the value of the invokerProxyBindingName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInvokerProxyBindingName() {
        return invokerProxyBindingName;
    }

    /**
     * Sets the value of the invokerProxyBindingName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInvokerProxyBindingName(final String value) {
        this.invokerProxyBindingName = value;
    }

    /**
     * Gets the value of the jndiName property.
     *
     * @return possible object is
     * {@link JndiName }
     */
    public JndiName getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     *
     * @param value allowed object is
     *              {@link JndiName }
     */
    public void setJndiName(final JndiName value) {
        this.jndiName = value;
    }

    /**
     * Gets the value of the ejbRef property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRef().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRef }
     */
    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

}
