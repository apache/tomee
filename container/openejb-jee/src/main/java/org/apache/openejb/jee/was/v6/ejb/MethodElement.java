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
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.openejb.jee.was.v6.common.Description;
import org.apache.openejb.jee.was.v6.xmi.Extension;

/**
 * The method element is used to denote a method of an enterprise bean's home or
 * remote interface, or a set of methods. The ejb-name element must be the name
 * of one of the enterprise beans in declared in the deployment descriptor; the
 * optional method-intf element allows to distinguish between a method with the
 * same signature that is defined in both the home and remote interface; the
 * method-name element specifies the method name; and the optional method-params
 * elements identify a
 *
 * single method among multiple methods with an overloaded method name.
 *
 * There are three possible styles of the method element syntax:
 *
 * 1. <method>
 *
 * <ejb-name>EJBNAME</ejb-name>
 *
 * <method-name>*</method-name>
 *
 * </method>
 *
 *
 * This style is used to refer to all the methods of the specified enterprise
 * bean's home and remote interfaces.
 *
 *
 * 2. <method>
 *
 * <ejb-name>EJBNAME</ejb-name>
 *
 * <method-name>METHOD</method-name>
 *
 * </method>>
 *
 * This style is used to refer to the specified method of the specified
 * enterprise bean. If there are multiple methods with
 *
 * the same overloaded name, the element of this style refers to all the methods
 * with the overloaded name.
 *
 *
 *
 *
 *
 * 3. <method>
 *
 * <ejb-name>EJBNAME</ejb-name>
 *
 * <method-name>METHOD</method-name>
 *
 * <method-params>
 *
 * <method-param>PARAM-1</method-param>
 *
 * <method-param>PARAM-2</method-param>
 *
 * ...
 *
 * <method-param>PARAM-n</method-param>
 *
 * </method-params> <method>
 *
 *
 * This style is used to refer to a single method within a set of methods with
 * an overloaded name. PARAM-1 through PARAM-n are the fully-qualified Java
 * types of the method's input parameters (if the method has no input arguments,
 * the method-params element
 *
 * contains no method-param elements). Arrays are specified by the array
 * element's type, followed by one or more pair of square brackets (e.g.
 * int[][]).
 *
 *
 *
 * Used in: method-permission and container-transaction
 *
 * Examples:
 *
 *
 * Style 1: The following method element refers to all the methods of the
 * EmployeeService bean's home and remote interfaces:
 *
 *
 * <method>
 *
 * <ejb-name>EmployeeService</ejb-name>
 *
 * <method-name>*</method-name>
 *
 * </method>
 *
 *
 * Style 2: The following method element refers to all the create methods of the
 * EmployeeService bean's home interface:
 *
 *
 * <method>
 *
 * <ejb-name>EmployeeService</ejb-name>
 *
 * <method-name>create</method-name>
 *
 * </method>
 *
 * Style 3: The following method element refers to the create(String firstName,
 * String LastName) method of the EmployeeService bean's home interface.
 *
 *
 * <method>
 *
 * <ejb-name>EmployeeService</ejb-name>
 *
 * <method-name>create</method-name>
 *
 * <method-params>
 *
 * <method-param>java.lang.String</method-param>
 *
 * <method-param>java.lang.String</method-param>
 *
 * </method-params> </method>
 *
 *
 *
 * The following example illustrates a Style 3 element with more complex
 * parameter types. The method foobar(char s, int i, int[] iar,
 * mypackage.MyClass mycl, mypackage.MyClass[][] myclaar)
 *
 * would be specified as:
 *
 *
 * <method>
 *
 * <ejb-name>EmployeeService</ejb-name>
 *
 * <method-name>foobar</method-name>
 *
 * <method-params>
 *
 * <method-param>char</method-param>
 *
 * <method-param>int</method-param>
 *
 * <method-param>int[]</method-param>
 *
 * <method-param>mypackage.MyClass</method-param>
 *
 * <method-param>mypackage.MyClass[][]</method-param>
 *
 * </method-params> </method>
 *
 *
 * The optional method-intf element can be used when it becomes necessary to
 * differentiate between a method defined in the home interface and a method
 * with the same name and signature that is defined in the remote interface.
 *
 * For example, the method element
 *
 *
 * <method>
 *
 * <ejb-name>EmployeeService</ejb-name>
 *
 * <method-intf>Remote</method-intf>
 *
 * <method-name>create</method-name>
 *
 * <method-params>
 *
 * <method-param>java.lang.String</method-param>
 *
 * <method-param>java.lang.String</method-param>
 *
 * </method-params> </method>
 *
 *
 * can be used to differentiate the create(String, String) method defined in the
 * remote interface from the create(String, String) method defined in the home
 * interface, which would be defined as
 *
 *
 * <method>
 *
 * <ejb-name>EmployeeService</ejb-name>
 *
 * <method-intf>Home</method-intf>
 *
 * <method-name>create</method-name>
 *
 * <method-params>
 *
 * <method-param>java.lang.String</method-param>
 *
 * <method-param>java.lang.String</method-param>
 *
 * </method-params> </method>
 *
 *
 *
 * Java class for MethodElement complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="MethodElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="enterpriseBean" type="{ejb.xmi}EnterpriseBean"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="descriptions" type="{common.xmi}Description"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.omg.org/XMI}Extension"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="enterpriseBean" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="parms" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" type="{ejb.xmi}MethodElementKind" /&gt;
 *       &lt;attribute ref="{http://www.omg.org/XMI}id"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MethodElement", propOrder = {"enterpriseBeans",
    "descriptions", "extensions"})
public class MethodElement {

    @XmlElement(name = "enterpriseBean")
    protected List<EnterpriseBean> enterpriseBeans;
    protected List<Description> descriptions;
    @XmlElement(name = "Extension", namespace = "http://www.omg.org/XMI")
    protected List<Extension> extensions;
    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected String enterpriseBean;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String parms;
    @XmlAttribute(name = "type")
    protected MethodElementEnum methodElementType;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected QName type;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String version;
    @XmlAttribute
    protected String href;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    @XmlIDREF
    protected Object idref;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String label;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String uuid;

    /**
     * Gets the value of the enterpriseBeans property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the enterpriseBeans property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEnterpriseBeans().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EnterpriseBean }
     */
    public List<EnterpriseBean> getEnterpriseBeans() {
        if (enterpriseBeans == null) {
            enterpriseBeans = new ArrayList<EnterpriseBean>();
        }
        return this.enterpriseBeans;
    }

    /**
     * Gets the value of the descriptions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the descriptions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDescriptions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Description }
     */
    public List<Description> getDescriptions() {
        if (descriptions == null) {
            descriptions = new ArrayList<Description>();
        }
        return this.descriptions;
    }

    /**
     * Gets the value of the extensions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the extensions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getExtensions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Extension }
     */
    public List<Extension> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<Extension>();
        }
        return this.extensions;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDescription(final String value) {
        this.description = value;
    }

    /**
     * Gets the value of the enterpriseBean property.
     *
     * @return possible object is {@link String }
     */
    public String getEnterpriseBean() {
        return enterpriseBean;
    }

    /**
     * Sets the value of the enterpriseBean property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEnterpriseBean(final String value) {
        this.enterpriseBean = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the parms property.
     *
     * @return possible object is {@link String }
     */
    public String getParms() {
        return parms;
    }

    /**
     * Sets the value of the parms property.
     *
     * @param value allowed object is {@link String }
     */
    public void setParms(final String value) {
        this.parms = value;
    }

    /**
     * Gets the value of the methodElementType property.
     *
     * @return possible object is {@link MethodElementEnum }
     */
    public MethodElementEnum getMethodElementType() {
        return methodElementType;
    }

    /**
     * Sets the value of the methodElementType property.
     *
     * @param value allowed object is {@link MethodElementEnum }
     */
    public void setMethodElementType(final MethodElementEnum value) {
        this.methodElementType = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link QName }
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link QName }
     */
    public void setType(final QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is {@link String }
     */
    public String getVersion() {
        if (version == null) {
            return "2.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }

    /**
     * Gets the value of the href property.
     *
     * @return possible object is {@link String }
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHref(final String value) {
        this.href = value;
    }

    /**
     * Gets the value of the idref property.
     *
     * @return possible object is {@link Object }
     */
    public Object getIdref() {
        return idref;
    }

    /**
     * Sets the value of the idref property.
     *
     * @param value allowed object is {@link Object }
     */
    public void setIdref(final Object value) {
        this.idref = value;
    }

    /**
     * Gets the value of the label property.
     *
     * @return possible object is {@link String }
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLabel(final String value) {
        this.label = value;
    }

    /**
     * Gets the value of the uuid property.
     *
     * @return possible object is {@link String }
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUuid(final String value) {
        this.uuid = value;
    }

}
