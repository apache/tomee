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
 * The outbound-resourceadapterType specifies information about
 * an outbound resource adapter. The information includes fully
 * qualified names of classes/interfaces required as part of
 * the connector architecture specified contracts for
 * connection management, level of transaction support
 * provided, one or more authentication mechanisms supported
 * and additional required security permissions.
 * <p/>
 * If there is no authentication-mechanism specified as part of
 * resource adapter element then the resource adapter does not
 * support any standard security authentication mechanisms as
 * part of security contract. The application server ignores
 * the security part of the system contracts in this case.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outbound-resourceadapterType", propOrder = {
        "connectionDefinition",
        "transactionSupport",
        "authenticationMechanism",
        "reauthenticationSupport"
})
public class OutboundResourceAdapter {

    @XmlElement(name = "connection-definition", required = true)
    protected List<ConnectionDefinition> connectionDefinition;
    @XmlElement(name = "transaction-support", required = true)
    protected TransactionSupportType transactionSupport;
    @XmlElement(name = "authentication-mechanism")
    protected List<AuthenticationMechanism> authenticationMechanism;
    @XmlElement(name = "reauthentication-support")
    protected boolean reauthenticationSupport;
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
    public boolean isReauthenticationSupport() {
        return reauthenticationSupport;
    }

    /**
     * Sets the value of the reauthenticationSupport property.
     */
    public void setReauthenticationSupport(boolean value) {
        this.reauthenticationSupport = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
