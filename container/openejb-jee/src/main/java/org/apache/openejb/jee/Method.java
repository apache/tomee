/**
 *
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for methodType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="methodType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/&gt;
 *         &lt;element name="method-intf" type="{http://java.sun.com/xml/ns/javaee}method-intfType" minOccurs="0"/&gt;
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/javaee}method-nameType"/&gt;
 *         &lt;element name="method-params" type="{http://java.sun.com/xml/ns/javaee}method-paramsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "methodType", propOrder = {
    "descriptions",
    "ejbName",
    "methodIntf",
    "methodName",
    "methodParams"
})
public class Method {

    @XmlTransient
    protected TextMap description = new TextMap();

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;

    @XmlTransient
    protected String className;

    @XmlElement(name = "method-intf")
    protected MethodIntf methodIntf;
    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "method-params")
    protected MethodParams methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;


    public Method(final String ejbName, final java.lang.reflect.Method method) {
        this.ejbName = ejbName;
        this.methodName = method.getName();
        this.className = method.getDeclaringClass().getName();
        final MethodParams methodParams = new MethodParams();
        for (final Class<?> type : method.getParameterTypes()) {
            methodParams.getMethodParam().add(type.getName());
        }
        this.methodParams = methodParams;
    }

    public Method(final String ejbName, final String methodName, final String... parameters) {
        this(ejbName, null, methodName, parameters);
    }

    public Method(final String ejbName, final String className, final String methodName, final String... parameters) {
        this.ejbName = ejbName;
        this.methodName = methodName;
        this.className = className;

        if (parameters.length > 0) {
            final MethodParams params = new MethodParams();
            for (final String paramName : parameters) {
                params.getMethodParam().add(paramName);
            }
            this.methodParams = params;
        }
    }

    public Method() {
    }

    public Method(final String ejbName, final String methodName) {
        this(ejbName, null, methodName);
    }

    public Method(final String ejbName, final String className, final String methodName) {
        this.ejbName = ejbName;
        this.methodName = methodName;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getEjbName() {
        return ejbName;
    }

    /**
     * The ejb-nameType specifies an enterprise bean's name. It is
     * used by ejb-name elements. This name is assigned by the
     * ejb-jar file producer to name the enterprise bean in the
     * ejb-jar file's deployment descriptor. The name must be
     * unique among the names of the enterprise beans in the same
     * ejb-jar file.
     *
     * There is no architected relationship between the used
     * ejb-name in the deployment descriptor and the JNDI name that
     * the Deployer will assign to the enterprise bean's home.
     *
     * The name for an entity bean must conform to the lexical
     * rules for an NMTOKEN.
     *
     * Example:
     *
     * <ejb-name>EmployeeService</ejb-name>
     */
    public void setEjbName(final String value) {
        this.ejbName = value;
    }

    public MethodIntf getMethodIntf() {
        return methodIntf;
    }

    public void setMethodIntf(final MethodIntf value) {
        this.methodIntf = value;
    }

    public Method withInterface(final MethodIntf methodIntf) {
        setMethodIntf(methodIntf);
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * contains a name of an enterprise
     * bean method or the asterisk (*) character. The asterisk is
     * used when the element denotes all the methods of an
     * enterprise bean's client view interfaces.
     */
    public void setMethodName(final String value) {
        this.methodName = value;
    }

    public MethodParams getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(final MethodParams value) {
        this.methodParams = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
