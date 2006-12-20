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

package org.apache.openejb.jee.sun;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pmDescriptor",
    "pmInuse"
})
@XmlRootElement(name = "pm-descriptors")
public class PmDescriptors {

    @XmlElement(name = "pm-descriptor", required = true)
    protected List<PmDescriptor> pmDescriptor;
    @XmlElement(name = "pm-inuse", required = true)
    protected PmInuse pmInuse;

    /**
     * Gets the value of the pmDescriptor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pmDescriptor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPmDescriptor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PmDescriptor }
     * 
     * 
     */
    public List<PmDescriptor> getPmDescriptor() {
        if (pmDescriptor == null) {
            pmDescriptor = new ArrayList<PmDescriptor>();
        }
        return this.pmDescriptor;
    }

    /**
     * Gets the value of the pmInuse property.
     * 
     * @return
     *     possible object is
     *     {@link PmInuse }
     *     
     */
    public PmInuse getPmInuse() {
        return pmInuse;
    }

    /**
     * Sets the value of the pmInuse property.
     * 
     * @param value
     *     allowed object is
     *     {@link PmInuse }
     *     
     */
    public void setPmInuse(PmInuse value) {
        this.pmInuse = value;
    }

}
