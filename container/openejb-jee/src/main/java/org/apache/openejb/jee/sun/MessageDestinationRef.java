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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"messageDestinationRefName", "jndiName"})
public class MessageDestinationRef {
    @XmlElement(name = "message-destination-ref-name", required = true)
    protected String messageDestinationRefName;
    @XmlElement(name = "jndi-name", required = true)
    protected String jndiName;

    public String getMessageDestinationRefName() {
        return messageDestinationRefName;
    }

    public void setMessageDestinationRefName(String value) {
        this.messageDestinationRefName = value;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String value) {
        this.jndiName = value;
    }
}
