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
package org.apache.openejb.config.sys;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.apache.openejb.config.sys package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.openejb.config.sys
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.Container }
     */
    public Container createContainer() {
        return new Container();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.ConnectionManager }
     */
    public ConnectionManager createConnectionManager() {
        return new ConnectionManager();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.Resource }
     */
    public Resource createResource() {
        return new Resource();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.SecurityService }
     */
    public SecurityService createSecurityService() {
        return new SecurityService();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.JndiProvider }
     */
    public JndiProvider createJndiProvider() {
        return new JndiProvider();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.Deployments }
     */
    public Deployments createDeployments() {
        return new Deployments();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.Connector }
     */
    public Connector createConnector() {
        return new Connector();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.ProxyFactory }
     */
    public ProxyFactory createProxyFactory() {
        return new ProxyFactory();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.Openejb }
     */
    public Openejb createOpenejb() {
        return new Openejb();
    }

    /**
     * Create an instance of {@link org.apache.openejb.config.sys.TransactionManager }
     */
    public TransactionManager createTransactionManager() {
        return new TransactionManager();
    }

    /**
     * Create an instance of {@link ServiceProvider }
     */
    public ServiceProvider createServiceProvider() {
        return new ServiceProvider();
    }

    /**
     * Create an instance of {@link ServicesJar }
     */
    public ServicesJar createServicesJar() {
        return new ServicesJar();
    }

}
