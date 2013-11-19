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
import org.apache.openejb.config.typed.util.DurationAdapter;
import org.apache.openejb.util.Duration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ActiveMQResourceAdapter")
public class ActiveMQResourceAdapterBuilder extends Resource {

    @XmlAttribute
    private String brokerXmlConfig = "broker:(tcp://localhost:61616)?useJmx=false";
    @XmlAttribute
    private URI serverUrl = URI.create("vm://localhost?waitForStart=20000&async=true");
    @XmlAttribute
    private String dataSource = "Default Unmanaged JDBC Database";
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration startupTimeout = Duration.parse("10 seconds");

    public ActiveMQResourceAdapterBuilder() {
        setClassName("org.apache.openejb.resource.activemq.ActiveMQResourceAdapter");
        setType("ActiveMQResourceAdapter");
        setId("ActiveMQResourceAdapter");

    }

    public ActiveMQResourceAdapterBuilder id(String id) {
        setId(id);
        return this;
    }

    public ActiveMQResourceAdapterBuilder withBrokerXmlConfig(String brokerXmlConfig) {
        this.brokerXmlConfig = brokerXmlConfig;
        return this;
    }

    public void setBrokerXmlConfig(String brokerXmlConfig) {
        this.brokerXmlConfig = brokerXmlConfig;
    }

    public String getBrokerXmlConfig() {
        return brokerXmlConfig;
    }

    public ActiveMQResourceAdapterBuilder withServerUrl(URI serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public void setServerUrl(URI serverUrl) {
        this.serverUrl = serverUrl;
    }

    public URI getServerUrl() {
        return serverUrl;
    }

    public ActiveMQResourceAdapterBuilder withDataSource(String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSource() {
        return dataSource;
    }

    public ActiveMQResourceAdapterBuilder withStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
        return this;
    }

    public void setStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
    }

    public Duration getStartupTimeout() {
        return startupTimeout;
    }

    public ActiveMQResourceAdapterBuilder withStartupTimeout(long time, TimeUnit unit) {
        return withStartupTimeout(new Duration(time, unit));
    }

    public void setStartupTimeout(long time, TimeUnit unit) {
        setStartupTimeout(new Duration(time, unit));
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
