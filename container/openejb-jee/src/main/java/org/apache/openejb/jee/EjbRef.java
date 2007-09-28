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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
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
public class EjbRef implements JndiReference {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "ejb-ref-name", required = true)
    protected String ejbRefName;
    @XmlElement(name = "ejb-ref-type")
    protected EjbRefType ejbRefType;
    protected String home;
    protected String remote;
    @XmlElement(name = "ejb-link")
    protected String ejbLink;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public static enum Type {
        UNKNOWN, LOCAL, REMOTE;
    }

    @XmlTransient
    protected Type refType = Type.REMOTE;

    public Type getRefType() {
        return refType;
    }

    public void setRefType(Type refType) {
        this.refType = refType;
    }

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getEjbRefName() {
        return ejbRefName;
    }

    public String getName() {
        return getEjbRefName();
    }

    public String getKey() {
        return getName();
    }

    public String getType() {
        return getEjbRefType().name();
    }

    public void setName(String name) {
        setEjbRefName(name);
    }

    public void setType(String type) {
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

    public EjbRefType getEjbRefType() {
        return ejbRefType;
    }

    public void setEjbRefType(EjbRefType value) {
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

    public List<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTarget>();
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
