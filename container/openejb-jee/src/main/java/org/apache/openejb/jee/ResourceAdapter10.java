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

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * The resourceadapterType specifies information about the
 * resource adapter. The information includes fully qualified
 * resource adapter Java class name, configuration properties,
 * information specific to the implementation of the resource
 * adapter library as specified through the
 * outbound-resourceadapter and inbound-resourceadapter
 * elements, and an optional set of administered objects.
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "resourceadapterType", propOrder = {
//        "managedConnectionFactoryClass",
//        "connectionFactoryInterface",
//        "connectionFactoryImplClass",
//        "connectionInterface",
//        "connectionImplClass",
//        "transactionSupport",
//        "configProperty",
//        "authenticationMechanism",
//        "reauthenticationSupport",
//        "securityPermission"
//})
public class ResourceAdapter10 extends ResourceAdapterBase {

    private ConnectionDefinition connectionDefinition = new ConnectionDefinition();

    public ResourceAdapter10() {
        setOutboundResourceAdapter(new OutboundResourceAdapter());
        getOutboundResourceAdapter().getConnectionDefinition().add(connectionDefinition);
    }

    @XmlElement(name = "config-property")
    public List<ConfigProperty> getConfigProperty() {
        return connectionDefinition.getConfigProperty();
    }

    @XmlElement(name = "managedconnectionfactory-class", required = true)
    public String getManagedConnectionFactoryClass() {
        return connectionDefinition.getManagedConnectionFactoryClass();
    }

    public void setManagedConnectionFactoryClass(String value) {
        connectionDefinition.setManagedConnectionFactoryClass(value);
    }

    @XmlElement(name = "connectionfactory-interface", required = true)
    public String getConnectionFactoryInterface() {
        return connectionDefinition.getConnectionFactoryInterface();
    }

    public void setConnectionFactoryInterface(String value) {
        connectionDefinition.setConnectionFactoryInterface(value);
    }

    @XmlElement(name = "connectionfactory-impl-class", required = true)
    public String getConnectionFactoryImplClass() {
        return connectionDefinition.getConnectionFactoryImplClass();
    }

    public void setConnectionFactoryImplClass(String value) {
        connectionDefinition.setConnectionFactoryImplClass(value);
    }

    @XmlElement(name = "connection-interface", required = true)
    public String getConnectionInterface() {
        return connectionDefinition.getConnectionInterface();
    }

    public void setConnectionInterface(String value) {
        connectionDefinition.setConnectionInterface(value);
    }

    @XmlElement(name = "connection-impl-class", required = true)
    public String getConnectionImplClass() {
        return connectionDefinition.getConnectionImplClass();
    }

    public void setConnectionImplClass(String value) {
        connectionDefinition.setConnectionImplClass(value);
    }

    @XmlElement(name = "transaction-support")
     public TransactionSupportType getTransactionSupport() {
         return getOutboundResourceAdapter().getTransactionSupport();
     }

     public void setTransactionSupport(TransactionSupportType value) {
         getOutboundResourceAdapter().setTransactionSupport(value);
     }

     @XmlElement(name = "authentication-mechanism")
     public List<AuthenticationMechanism> getAuthenticationMechanism() {
         return getOutboundResourceAdapter().getAuthenticationMechanism();
     }

     @XmlElement(name = "reauthentication-support")
     public Boolean isReauthenticationSupport() {
         return getOutboundResourceAdapter().isReauthenticationSupport();
     }

     public void setReauthenticationSupport(Boolean value) {
         getOutboundResourceAdapter().setReauthenticationSupport(value);
     }


//    public List<SecurityPermission> getSecurityPermission() {
//        if (securityPermission == null) {
//            securityPermission = new ArrayList<SecurityPermission>();
//        }
//        return this.securityPermission;
//    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String value) {
//        this.id = value;
//    }

}