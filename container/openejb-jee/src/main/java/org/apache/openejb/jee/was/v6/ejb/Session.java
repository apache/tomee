/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.ejb;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * The session-beanType declares an session bean. The declaration consists of:
 *
 * - an optional description - an optional display name - an optional icon
 * element that contains a small and a large icon file name - a name assigned to
 * the enterprise bean in the deployment description - the names of the session
 * bean's remote home and remote interfaces, if any - the names of the session
 * bean's local home and local interfaces, if any - the name of the session
 * bean's web service endpoint interface, if any - the session bean's
 * implementation class - the session bean's state management type - the session
 * bean's transaction management type - an optional declaration of the bean's
 * environment entries - an optional declaration of the bean's EJB references -
 * an optional declaration of the bean's local EJB references - an optional
 * declaration of the bean's web service references - an optional declaration of
 * the security role references - an optional declaration of the security
 * identity to be used for the execution of the bean's methods - an optional
 * declaration of the bean's resource manager connection factory references - an
 * optional declaration of the bean's resource environment references. - an
 * optional declaration of the bean's message destination references
 *
 * The elements that are optional are "optional" in the sense that they are
 * omitted when if lists represented by them are empty.
 *
 * Either both the local-home and the local elements or both the home and the
 * remote elements must be specified for the session bean.
 *
 * The service-endpoint element may only be specified if the bean is a stateless
 * session bean.
 *
 *
 *
 * Java class for Session complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="Session"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ejb.xmi}EnterpriseBean"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="serviceEndpoint" type="{java.xmi}JavaClass"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="serviceEndpoint" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sessionType" type="{ejb.xmi}SessionType" /&gt;
 *       &lt;attribute name="transactionType" type="{ejb.xmi}TransactionType" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Session", propOrder = {"serviceEndpoints"})
public class Session extends EnterpriseBean {

    @XmlElement(name = "serviceEndpoint")
    protected List<JavaClass> serviceEndpoints;
    @XmlAttribute
    protected String serviceEndpoint;
    @XmlAttribute
    protected SessionEnum sessionType;
    @XmlAttribute
    protected TransactionEnum transactionType;

    /**
     * Gets the value of the serviceEndpoints property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the serviceEndpoints property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getServiceEndpoints().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getServiceEndpoints() {
        if (serviceEndpoints == null) {
            serviceEndpoints = new ArrayList<JavaClass>();
        }
        return this.serviceEndpoints;
    }

    /**
     * Gets the value of the serviceEndpoint property.
     *
     * @return possible object is {@link String }
     */
    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Sets the value of the serviceEndpoint property.
     *
     * @param value allowed object is {@link String }
     */
    public void setServiceEndpoint(final String value) {
        this.serviceEndpoint = value;
    }

    /**
     * Gets the value of the sessionType property.
     *
     * @return possible object is {@link SessionEnum }
     */
    public SessionEnum getSessionType() {
        return sessionType;
    }

    /**
     * Sets the value of the sessionType property.
     *
     * @param value allowed object is {@link SessionEnum }
     */
    public void setSessionType(final SessionEnum value) {
        this.sessionType = value;
    }

    /**
     * Gets the value of the transactionType property.
     *
     * @return possible object is {@link TransactionEnum }
     */
    public TransactionEnum getTransactionType() {
        return transactionType;
    }

    /**
     * Sets the value of the transactionType property.
     *
     * @param value allowed object is {@link TransactionEnum }
     */
    public void setTransactionType(final TransactionEnum value) {
        this.transactionType = value;
    }

}
