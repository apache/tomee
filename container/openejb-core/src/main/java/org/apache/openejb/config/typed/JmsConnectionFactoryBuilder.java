/*
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
package org.apache.openejb.config.typed;

import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.typed.util.Builders;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;

@XmlRootElement(name = "JmsConnectionFactory")
public class JmsConnectionFactoryBuilder extends Resource {

    private String resourceAdapter = "Default JMS Resource Adapter";
    private String transactionSupport = "xa";
    private int poolMaxSize = 10;
    private int poolMinSize = 0;
    private int connectionMaxWaitMilliseconds = 5000;
    private int connectionMaxIdleMinutes = 15;

    public JmsConnectionFactoryBuilder() {
        setClassName("org.apache.activemq.ra.ActiveMQManagedConnectionFactory");
        setType("javax.jms.ConnectionFactory");
        setId("JmsConnectionFactory");

    }

    public JmsConnectionFactoryBuilder id(String id) {
        setId(id);
        return this;
    }

    public JmsConnectionFactoryBuilder withResourceAdapter(String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
        return this;
    }

    public void setResourceAdapter(String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    @XmlAttribute
    public String getResourceAdapter() {
        return resourceAdapter;
    }

    public JmsConnectionFactoryBuilder withTransactionSupport(String transactionSupport) {
        this.transactionSupport = transactionSupport;
        return this;
    }

    public void setTransactionSupport(String transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

    @XmlAttribute
    public String getTransactionSupport() {
        return transactionSupport;
    }

    public JmsConnectionFactoryBuilder withPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
        return this;
    }

    public void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    @XmlAttribute
    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public JmsConnectionFactoryBuilder withPoolMinSize(int poolMinSize) {
        this.poolMinSize = poolMinSize;
        return this;
    }

    public void setPoolMinSize(int poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    @XmlAttribute
    public int getPoolMinSize() {
        return poolMinSize;
    }

    public JmsConnectionFactoryBuilder withConnectionMaxWaitMilliseconds(int connectionMaxWaitMilliseconds) {
        this.connectionMaxWaitMilliseconds = connectionMaxWaitMilliseconds;
        return this;
    }

    public void setConnectionMaxWaitMilliseconds(int connectionMaxWaitMilliseconds) {
        this.connectionMaxWaitMilliseconds = connectionMaxWaitMilliseconds;
    }

    @XmlAttribute
    public int getConnectionMaxWaitMilliseconds() {
        return connectionMaxWaitMilliseconds;
    }

    public JmsConnectionFactoryBuilder withConnectionMaxIdleMinutes(int connectionMaxIdleMinutes) {
        this.connectionMaxIdleMinutes = connectionMaxIdleMinutes;
        return this;
    }

    public void setConnectionMaxIdleMinutes(int connectionMaxIdleMinutes) {
        this.connectionMaxIdleMinutes = connectionMaxIdleMinutes;
    }

    @XmlAttribute
    public int getConnectionMaxIdleMinutes() {
        return connectionMaxIdleMinutes;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
