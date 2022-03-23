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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config.typed;

import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.typed.util.Builders;
import org.apache.openejb.config.typed.util.DurationAdapter;
import org.apache.openejb.util.Duration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "JmsConnectionFactory")
public class JmsConnectionFactoryBuilder extends Resource {

    @XmlAttribute
    private String resourceAdapter = "Default JMS Resource Adapter";
    @XmlAttribute
    private String transactionSupport = "xa";
    @XmlAttribute
    private int poolMaxSize = 10;
    @XmlAttribute
    private int poolMinSize;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration connectionMaxWaitTime = Duration.parse("5 seconds");
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration connectionMaxIdleTime = Duration.parse("15 Minutes");

    public JmsConnectionFactoryBuilder() {
        setClassName("org.apache.activemq.ra.ActiveMQManagedConnectionFactory");
        setType("jakarta.jms.ConnectionFactory");
        setId("JmsConnectionFactory");

    }

    public JmsConnectionFactoryBuilder id(final String id) {
        setId(id);
        return this;
    }

    public JmsConnectionFactoryBuilder withResourceAdapter(final String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
        return this;
    }

    public void setResourceAdapter(final String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public String getResourceAdapter() {
        return resourceAdapter;
    }

    public JmsConnectionFactoryBuilder withTransactionSupport(final String transactionSupport) {
        this.transactionSupport = transactionSupport;
        return this;
    }

    public void setTransactionSupport(final String transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

    public String getTransactionSupport() {
        return transactionSupport;
    }

    public JmsConnectionFactoryBuilder withPoolMaxSize(final int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
        return this;
    }

    public void setPoolMaxSize(final int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public JmsConnectionFactoryBuilder withPoolMinSize(final int poolMinSize) {
        this.poolMinSize = poolMinSize;
        return this;
    }

    public void setPoolMinSize(final int poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    public int getPoolMinSize() {
        return poolMinSize;
    }

    public JmsConnectionFactoryBuilder withConnectionMaxWaitTime(final Duration connectionMaxWaitTime) {
        this.connectionMaxWaitTime = connectionMaxWaitTime;
        return this;
    }

    public void setConnectionMaxWaitTime(final Duration connectionMaxWaitTime) {
        this.connectionMaxWaitTime = connectionMaxWaitTime;
    }

    public Duration getConnectionMaxWaitTime() {
        return connectionMaxWaitTime;
    }

    public JmsConnectionFactoryBuilder withConnectionMaxWaitTime(final long time, final TimeUnit unit) {
        return withConnectionMaxWaitTime(new Duration(time, unit));
    }

    public void setConnectionMaxWaitTime(final long time, final TimeUnit unit) {
        setConnectionMaxWaitTime(new Duration(time, unit));
    }

    public JmsConnectionFactoryBuilder withConnectionMaxIdleTime(final Duration connectionMaxIdleTime) {
        this.connectionMaxIdleTime = connectionMaxIdleTime;
        return this;
    }

    public void setConnectionMaxIdleTime(final Duration connectionMaxIdleTime) {
        this.connectionMaxIdleTime = connectionMaxIdleTime;
    }

    public Duration getConnectionMaxIdleTime() {
        return connectionMaxIdleTime;
    }

    public JmsConnectionFactoryBuilder withConnectionMaxIdleTime(final long time, final TimeUnit unit) {
        return withConnectionMaxIdleTime(new Duration(time, unit));
    }

    public void setConnectionMaxIdleTime(final long time, final TimeUnit unit) {
        setConnectionMaxIdleTime(new Duration(time, unit));
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
