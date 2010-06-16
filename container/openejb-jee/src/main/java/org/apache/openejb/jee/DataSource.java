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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "data-sourceType", propOrder = {
    "descriptions",
    "name",
    "className",
    "serverName",
    "portNumber",
    "databaseName",
    "url",
    "user",
    "password",
    "property",
    "loginTimeout",
    "transactional",
    "isolationLevel",
    "initialPoolSize",
    "maxPoolSize",
    "minPoolSize",
    "maxIdleTime",
    "maxStatements"
})
public class DataSource {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "class-name")
    protected String className;
    @XmlElement(name = "server-name")
    protected String serverName;
    @XmlElement(name = "port-number")
    protected Integer portNumber;
    @XmlElement(name = "database-name")
    protected String databaseName;
    protected String url;
    protected String user;
    protected String password;
    protected List<Property> property;
    @XmlElement(name = "login-timeout")
    protected Integer loginTimeout;
    protected Boolean transactional;
    @XmlElement(name = "isolation-level")
    protected TransportGuarantee isolationLevel;
    @XmlElement(name = "initial-pool-size")
    protected Integer initialPoolSize;
    @XmlElement(name = "max-pool-size")
    protected Integer maxPoolSize;
    @XmlElement(name = "min-pool-size")
    protected Integer minPoolSize;
    @XmlElement(name = "max-idle-time")
    protected Integer maxIdleTime;
    @XmlElement(name = "max-statements")
    protected Integer maxStatements;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    @XmlElement(name = "description")
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }


    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String value) {
        this.className = value;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String value) {
        this.serverName = value;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer value) {
        this.portNumber = value;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String value) {
        this.databaseName = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String value) {
        this.user = value;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    public Integer getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(Integer value) {
        this.loginTimeout = value;
    }

    public Boolean getTransactional() {
        return transactional;
    }

    public void setTransactional(Boolean value) {
        this.transactional = value;
    }

    public TransportGuarantee getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(TransportGuarantee value) {
        this.isolationLevel = value;
    }

    public Integer getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(Integer value) {
        this.initialPoolSize = value;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer value) {
        this.maxPoolSize = value;
    }

    public Integer getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(Integer value) {
        this.minPoolSize = value;
    }

    public Integer getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(Integer value) {
        this.maxIdleTime = value;
    }

    public Integer getMaxStatements() {
        return maxStatements;
    }

    public void setMaxStatements(Integer value) {
        this.maxStatements = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}
