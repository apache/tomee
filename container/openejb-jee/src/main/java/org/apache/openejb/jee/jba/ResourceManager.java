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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "resName",
    "resJndiNameOrResUrl"
})
@XmlRootElement(name = "resource-manager")
public class ResourceManager {

    @XmlElement(name = "res-name", required = true)
    protected String resName;
    @XmlElements({
        @XmlElement(name = "res-jndi-name", required = true, type = ResJndiName.class),
        @XmlElement(name = "res-url", required = true, type = ResUrl.class)
    })
    protected List<Object> resJndiNameOrResUrl;

    /**
     * Gets the value of the resName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResName() {
        return resName;
    }

    /**
     * Sets the value of the resName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResName(String value) {
        this.resName = value;
    }

    /**
     * Gets the value of the resJndiNameOrResUrl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resJndiNameOrResUrl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResJndiNameOrResUrl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResJndiName }
     * {@link ResUrl }
     * 
     * 
     */
    public List<Object> getResJndiNameOrResUrl() {
        if (resJndiNameOrResUrl == null) {
            resJndiNameOrResUrl = new ArrayList<Object>();
        }
        return this.resJndiNameOrResUrl;
    }

}
