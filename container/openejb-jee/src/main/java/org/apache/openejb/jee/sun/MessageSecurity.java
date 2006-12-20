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
    "message",
    "requestProtection",
    "responseProtection"
})
@XmlRootElement(name = "message-security")
public class MessageSecurity {

    @XmlElement(required = true)
    protected List<Message> message;
    @XmlElement(name = "request-protection")
    protected RequestProtection requestProtection;
    @XmlElement(name = "response-protection")
    protected ResponseProtection responseProtection;

    /**
     * Gets the value of the message property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the message property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Message }
     * 
     * 
     */
    public List<Message> getMessage() {
        if (message == null) {
            message = new ArrayList<Message>();
        }
        return this.message;
    }

    /**
     * Gets the value of the requestProtection property.
     * 
     * @return
     *     possible object is
     *     {@link RequestProtection }
     *     
     */
    public RequestProtection getRequestProtection() {
        return requestProtection;
    }

    /**
     * Sets the value of the requestProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestProtection }
     *     
     */
    public void setRequestProtection(RequestProtection value) {
        this.requestProtection = value;
    }

    /**
     * Gets the value of the responseProtection property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseProtection }
     *     
     */
    public ResponseProtection getResponseProtection() {
        return responseProtection;
    }

    /**
     * Sets the value of the responseProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseProtection }
     *     
     */
    public void setResponseProtection(ResponseProtection value) {
        this.responseProtection = value;
    }

}
