/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The ejb-refType is used by ejb-ref elements for the
 * declaration of a reference to an enterprise bean's home or
 * to the remote business interface of a 3.0 bean.
 * The declaration consists of:
 * <p/>
 * - an optional description
 * - the EJB reference name used in the code of
 * the Deployment Component that's referencing the enterprise
 * bean.
 * - the optional expected type of the referenced enterprise bean
 * - the optional remote interface of the referenced enterprise bean
 * or the remote business interface of the referenced enterprise
 * bean
 * - the optional expected home interface of the referenced
 * enterprise bean.  Not applicable if this ejb-ref
 * refers to the remote business interface of a 3.0 bean.
 * - optional ejb-link information, used to specify the
 * referenced enterprise bean
 * - optional elements to define injection of the named enterprise
 * bean into a component field or property
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-refType", propOrder = {
        "description",
        "ejbRefName",
        "ejbRefType",
        "home",
        "remote",
        "ejbLink",
        "mappedName",
        "injectionTarget"
        })
public class EjbRefType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "ejb-ref-name", required = true)
    protected String ejbRefName;
    @XmlElement(name = "ejb-ref-type")
    protected EjbRefTypeType ejbRefType;
    protected String home;
    protected String remote;
    @XmlElement(name = "ejb-link")
    protected String ejbLink;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTargetType> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getEjbRefName() {
        return ejbRefName;
    }

    /**
     * The ejb-ref-name element contains the name of an EJB
     * reference. The EJB reference is an entry in the
     * Deployment Component's environment and is relative to the
     * java:comp/env context.  The name must be unique within the
     * Deployment Component.
     * <p/>
     * It is recommended that name is prefixed with "ejb/".
     * <p/>
     * Example:
     * <p/>
     * <ejb-ref-name>ejb/Payroll</ejb-ref-name>
     */
    public void setEjbRefName(String value) {
        this.ejbRefName = value;
    }

    public EjbRefTypeType getEjbRefType() {
        return ejbRefType;
    }

    public void setEjbRefType(EjbRefTypeType value) {
        this.ejbRefType = value;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String value) {
        this.home = value;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String value) {
        this.remote = value;
    }

    /**
     * The value of the ejb-link element must be the ejb-name of an
     * enterprise bean in the same ejb-jar file or in another ejb-jar
     * file in the same Java EE application unit.
     * <p/>
     * Alternatively, the name in the ejb-link element may be
     * composed of a path name specifying the ejb-jar containing the
     * referenced enterprise bean with the ejb-name of the target
     * bean appended and separated from the path name by "#".  The
     * path name is relative to the Deployment File containing
     * Deployment Component that is referencing the enterprise
     * bean.  This allows multiple enterprise beans with the same
     * ejb-name to be uniquely identified.
     * <p/>
     * Examples:
     * <p/>
     * <ejb-link>EmployeeRecord</ejb-link>
     * <p/>
     * <ejb-link>../products/product.jar#ProductEJB</ejb-link>
     */
    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(String value) {
        this.ejbLink = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    /**
     * Gets the value of the injectionTarget property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the injectionTarget property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getInjectionTarget().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link InjectionTargetType }
     */
    public List<InjectionTargetType> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTargetType>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
