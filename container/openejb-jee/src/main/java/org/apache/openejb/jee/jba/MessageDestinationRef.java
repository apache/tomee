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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "messageDestinationRefName",
    "jndiName"
})
@XmlRootElement(name = "message-destination-ref")
public class MessageDestinationRef {

    @XmlElement(name = "message-destination-ref-name", required = true)
    protected String messageDestinationRefName;
    @XmlElement(name = "jndi-name", required = true)
    protected JndiName jndiName;

    /**
     * Gets the value of the messageDestinationRefName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMessageDestinationRefName() {
        return messageDestinationRefName;
    }

    /**
     * Sets the value of the messageDestinationRefName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMessageDestinationRefName(final String value) {
        this.messageDestinationRefName = value;
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

}
