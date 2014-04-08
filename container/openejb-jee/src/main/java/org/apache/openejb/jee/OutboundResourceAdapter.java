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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * connector_1_6.xsd 
 *
 * <p>Java class for outbound-resourceadapterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="outbound-resourceadapterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connection-definition" type="{http://java.sun.com/xml/ns/javaee}connection-definitionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="transaction-support" type="{http://java.sun.com/xml/ns/javaee}transaction-supportType" minOccurs="0"/>
 *         &lt;element name="authentication-mechanism" type="{http://java.sun.com/xml/ns/javaee}authentication-mechanismType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="reauthentication-support" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outbound-resourceadapterType", propOrder = {
        "connectionDefinition",
        "transactionSupport",
        "authenticationMechanism",
        "reauthenticationSupport"
})
public class OutboundResourceAdapter {

    @XmlElement(name = "connection-definition")
    protected List<ConnectionDefinition> connectionDefinition;
    @XmlElement(name = "transaction-support")
    protected TransactionSupportType transactionSupport;
    @XmlElement(name = "authentication-mechanism")
    protected List<AuthenticationMechanism> authenticationMechanism;
    @XmlElement(name = "reauthentication-support")
    protected Boolean reauthenticationSupport;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<ConnectionDefinition> getConnectionDefinition() {
        if (connectionDefinition == null) {
            connectionDefinition = new ArrayList<ConnectionDefinition>();
        }
        return this.connectionDefinition;
    }

    public TransactionSupportType getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(TransactionSupportType value) {
        this.transactionSupport = value;
    }

    public List<AuthenticationMechanism> getAuthenticationMechanism() {
        if (authenticationMechanism == null) {
            authenticationMechanism = new ArrayList<AuthenticationMechanism>();
        }
        return this.authenticationMechanism;
    }

    /**
     * Gets the value of the reauthenticationSupport property.
     */
    public Boolean isReauthenticationSupport() {
        return reauthenticationSupport;
    }

    /**
     * Sets the value of the reauthenticationSupport property.
     */
    public void setReauthenticationSupport(Boolean value) {
        this.reauthenticationSupport = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
