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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * javaee6.xsd
 *
 * <p>Java class for data-sourceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="data-sourceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" minOccurs="0"/&gt;
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="class-name" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/&gt;
 *         &lt;element name="server-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="port-number" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *         &lt;element name="database-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="url" type="{http://java.sun.com/xml/ns/javaee}jdbc-urlType" minOccurs="0"/&gt;
 *         &lt;element name="user" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="password" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="property" type="{http://java.sun.com/xml/ns/javaee}propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="login-timeout" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *         &lt;element name="transactional" type="{http://java.sun.com/xml/ns/javaee}xsdBooleanType" minOccurs="0"/&gt;
 *         &lt;element name="isolation-level" type="{http://java.sun.com/xml/ns/javaee}isolation-levelType" minOccurs="0"/&gt;
 *         &lt;element name="initial-pool-size" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *         &lt;element name="max-pool-size" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *         &lt;element name="min-pool-size" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *         &lt;element name="max-idle-time" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *         &lt;element name="max-statements" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

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
public class DataSource implements Keyable<String> {

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
    protected IsolationLevel isolationLevel;
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

    public DataSource() {
    }

    public DataSource(final String name, final String className) {
        this.name = name;
        this.className = className;
    }

//    pbpaste | grep protected | perl -pe 's/.*protected ([^ ]+) ([^ ]+);/public DataSource $2($1 $2) { this.$2 = $2; return this; }/'

    public DataSource name(final String name) {
        this.name = name;
        return this;
    }

    public DataSource className(final String className) {
        this.className = className;
        return this;
    }

    public DataSource serverName(final String serverName) {
        this.serverName = serverName;
        return this;
    }

    public DataSource portNumber(final Integer portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public DataSource databaseName(final String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public DataSource url(final String url) {
        this.url = url;
        return this;
    }

    public DataSource user(final String user) {
        this.user = user;
        return this;
    }

    public DataSource password(final String password) {
        this.password = password;
        return this;
    }

    public DataSource property(final String name, final String value) {
        getProperty().add(new Property(name, value));
        return this;
    }

    public DataSource loginTimeout(final Integer loginTimeout) {
        this.loginTimeout = loginTimeout;
        return this;
    }

    public DataSource transactional(final Boolean transactional) {
        this.transactional = transactional;
        return this;
    }

    public DataSource isolationLevel(final IsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }

    public DataSource initialPoolSize(final Integer initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
        return this;
    }

    public DataSource maxPoolSize(final Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public DataSource minPoolSize(final Integer minPoolSize) {
        this.minPoolSize = minPoolSize;
        return this;
    }

    public DataSource maxIdleTime(final Integer maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    public DataSource maxStatements(final Integer maxStatements) {
        this.maxStatements = maxStatements;
        return this;
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }


    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String value) {
        this.className = value;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String value) {
        this.serverName = value;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(final Integer value) {
        this.portNumber = value;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(final String value) {
        this.databaseName = value;
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(final String value) {
        this.url = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String value) {
        this.user = value;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String value) {
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

    public void setLoginTimeout(final Integer value) {
        this.loginTimeout = value;
    }

    public Boolean getTransactional() {
        return transactional;
    }

    public void setTransactional(final Boolean value) {
        this.transactional = value;
    }

    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(final IsolationLevel value) {
        this.isolationLevel = value;
    }

    public Integer getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(final Integer value) {
        this.initialPoolSize = value;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final Integer value) {
        this.maxPoolSize = value;
    }

    public Integer getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(final Integer value) {
        this.minPoolSize = value;
    }

    public Integer getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(final Integer value) {
        this.maxIdleTime = value;
    }

    public Integer getMaxStatements() {
        return maxStatements;
    }

    public void setMaxStatements(final Integer value) {
        this.maxStatements = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(final java.lang.String value) {
        this.id = value;
    }

    @Override
    public String getKey() {
        final String name = getName();
        if (name.startsWith("java:comp/env/")) {
            return name.substring("java:comp/env/".length());
        }
        return name;
    }
}
