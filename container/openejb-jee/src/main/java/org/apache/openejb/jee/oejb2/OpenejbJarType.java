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

package org.apache.openejb.jee.oejb2;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.oejb3.PropertiesAdapter;


/**
 * <p>Java class for openejb-jarType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="openejb-jarType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}environment" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/naming-1.2}cmp-connection-factory" minOccurs="0"/&gt;
 *         &lt;element name="ejb-ql-compiler-factory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="db-syntax-factory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="enforce-foreign-key-constraints" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="enterprise-beans"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                   &lt;element name="session" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}session-beanType"/&gt;
 *                   &lt;element name="entity" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}entity-beanType"/&gt;
 *                   &lt;element name="message-driven" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}message-driven-beanType"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="relationships" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}relationshipsType" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/naming-1.2}message-destination" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/j2ee/application-1.2}security" minOccurs="0"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}service" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "openejb-jarType", namespace = "http://tomee.apache.org/xml/ns/openejb-jar-2.2", propOrder = {
    "environment",
    "properties",
    "cmpConnectionFactory",
    "ejbQlCompilerFactory",
    "dbSyntaxFactory",
    "enforceForeignKeyConstraints",
    "enterpriseBeans",
    "ejbRelation",
    "messageDestination",
    "security",
    "service",
    "persistence"
})
public class OpenejbJarType {

    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;

    @XmlElement(name = "environment", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected EnvironmentType environment;

    @XmlElement(name = "cmp-connection-factory", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected ResourceLocatorType cmpConnectionFactory;

    @XmlElement(name = "ejb-ql-compiler-factory")
    protected String ejbQlCompilerFactory;

    @XmlElement(name = "db-syntax-factory")
    protected String dbSyntaxFactory;

    @XmlElement(name = "enforce-foreign-key-constraints")
    protected EmptyType enforceForeignKeyConstraints;

    @XmlElementWrapper(name = "enterprise-beans")
    @XmlElements({
        @XmlElement(name = "message-driven", required = true, type = MessageDrivenBeanType.class),
        @XmlElement(name = "session", required = true, type = SessionBeanType.class),
        @XmlElement(name = "entity", required = true, type = EntityBeanType.class)})
    protected List<EnterpriseBean> enterpriseBeans = new ArrayList<EnterpriseBean>();

    @XmlElementWrapper(name = "relationships")
    @XmlElement(name = "ejb-relation", required = true)
    protected List<EjbRelationType> ejbRelation;

    @XmlElement(name = "message-destination", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<MessageDestinationType> messageDestination;

    @XmlElementRef(name = "security", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractSecurityType> security;

    @XmlElementRef(name = "service", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractServiceType>> service;

    @XmlElementRef(name = "persistence", namespace = "http://java.sun.com/xml/ns/persistence", type = Persistence.class)
    protected List<Persistence> persistence;

    /**
     * Gets the value of the environment property.
     *
     * @return possible object is
     * {@link EnvironmentType }
     */
    public EnvironmentType getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     *
     * @param value allowed object is
     *              {@link EnvironmentType }
     */
    public void setEnvironment(final EnvironmentType value) {
        this.environment = value;
    }

    /**
     * Gets the value of the cmpConnectionFactory property.
     *
     * @return possible object is
     * {@link ResourceLocatorType }
     */
    public ResourceLocatorType getCmpConnectionFactory() {
        return cmpConnectionFactory;
    }

    /**
     * Sets the value of the cmpConnectionFactory property.
     *
     * @param value allowed object is
     *              {@link ResourceLocatorType }
     */
    public void setCmpConnectionFactory(final ResourceLocatorType value) {
        this.cmpConnectionFactory = value;
    }

    /**
     * Gets the value of the ejbQlCompilerFactory property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEjbQlCompilerFactory() {
        return ejbQlCompilerFactory;
    }

    /**
     * Sets the value of the ejbQlCompilerFactory property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbQlCompilerFactory(final String value) {
        this.ejbQlCompilerFactory = value;
    }

    /**
     * Gets the value of the dbSyntaxFactory property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDbSyntaxFactory() {
        return dbSyntaxFactory;
    }

    public void setDbSyntaxFactory(final String value) {
        this.dbSyntaxFactory = value;
    }

    public boolean isEnforceForeignKeyConstraints() {
        return enforceForeignKeyConstraints != null;
    }

    public void setEnforceForeignKeyConstraints(final boolean value) {
        this.enforceForeignKeyConstraints = value ? new EmptyType() : null;
    }

    public List<EnterpriseBean> getEnterpriseBeans() {
        return enterpriseBeans;
    }

    public List<EjbRelationType> getEjbRelation() {
        if (ejbRelation == null) {
            ejbRelation = new ArrayList<EjbRelationType>();
        }
        return ejbRelation;
    }

    public void setEjbRelation(final List<EjbRelationType> ejbRelation) {
        this.ejbRelation = ejbRelation;
    }

    public List<MessageDestinationType> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestinationType>();
        }
        return this.messageDestination;
    }

    public JAXBElement<? extends AbstractSecurityType> getSecurity() {
        return security;
    }

    public void setSecurity(final JAXBElement<? extends AbstractSecurityType> security) {
        this.security = security;
    }

    public List<JAXBElement<? extends AbstractServiceType>> getService() {
        if (service == null) {
            service = new ArrayList<JAXBElement<? extends AbstractServiceType>>();
        }
        return this.service;
    }

    public List<Persistence> getPersistence() {
        if (persistence == null) {
            persistence = new ArrayList<Persistence>();
        }
        return persistence;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }
}
