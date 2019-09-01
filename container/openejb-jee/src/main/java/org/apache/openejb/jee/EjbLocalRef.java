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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * javaee6.xsd
 *
 * <p>Java class for ejb-local-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ejb-local-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ejb-ref-name" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-nameType"/&gt;
 *         &lt;element name="ejb-ref-type" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-typeType" minOccurs="0"/&gt;
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/&gt;
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/&gt;
 *         &lt;element name="ejb-link" type="{http://java.sun.com/xml/ns/javaee}ejb-linkType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-local-refType", propOrder = {
    "descriptions",
    "ejbRefName",
    "ejbRefType",
    "localHome",
    "local",
    "ejbLink",
    "mappedName",
    "injectionTarget",
    "lookupName"
})
public class EjbLocalRef implements EjbReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "ejb-ref-name", required = true)
    protected String ejbRefName;
    @XmlElement(name = "ejb-ref-type")
    protected EjbRefType ejbRefType;
    @XmlElement(name = "local-home")
    protected String localHome;
    protected String local;
    @XmlElement(name = "ejb-link")
    protected String ejbLink;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "lookup-name")
    protected String lookupName;
    @XmlElement(name = "injection-target", required = true)
    protected Set<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public EjbLocalRef() {
    }

    public EjbLocalRef(final String ejbRefName, final String ejbLink) {
        this.ejbRefName = ejbRefName;
        this.ejbLink = ejbLink;
    }

    public EjbLocalRef(final EjbReference ref) {
        this.ejbRefName = ref.getName();
        this.ejbRefType = ref.getEjbRefType();
        this.ejbLink = ref.getEjbLink();
        this.mappedName = ref.getMappedName();
        setDescriptions(ref.getDescriptions());
        this.injectionTarget = ref.getInjectionTarget();
        this.local = ref.getInterface();
        this.localHome = ref.getHome();
    }

    public EjbLocalRef name(final String ejbRefName) {
        this.ejbRefName = ejbRefName;
        return this;
    }

    public EjbLocalRef type(final EjbRefType ejbRefType) {
        this.ejbRefType = ejbRefType;
        return this;
    }

    public EjbLocalRef link(final String link) {
        this.ejbLink = link;
        return this;
    }

    public EjbLocalRef local(final String local) {
        this.local = local;
        return this;
    }

    public EjbLocalRef local(final Class<?> local) {
        return local(local.getName());
    }

    public EjbLocalRef localHome(final String localHome) {
        this.localHome = localHome;
        return this;
    }

    public EjbLocalRef localHome(final Class<?> localHome) {
        return localHome(localHome.getName());
    }

    public EjbLocalRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public EjbLocalRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public EjbLocalRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        if (this.ejbRefName == null) {
            this.ejbRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public EjbLocalRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    public String getName() {
        return getEjbRefName();
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    public String getType() {
        return getEjbRefType().name();
    }

    public void setName(final String name) {
        setEjbRefName(name);
    }

    public void setType(final String type) {
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

    public String getEjbRefName() {
        return ejbRefName;
    }

    /**
     * The ejb-ref-name element contains the name of an EJB
     * reference. The EJB reference is an entry in the
     * Deployment Component's environment and is relative to the
     * java:comp/env context.  The name must be unique within the
     * Deployment Component.
     *
     * It is recommended that name is prefixed with "ejb/".
     *
     * Example:
     *
     * <ejb-ref-name>ejb/Payroll</ejb-ref-name>
     */
    public void setEjbRefName(final String value) {
        this.ejbRefName = value;
    }

    public EjbRefType getEjbRefType() {
        return ejbRefType;
    }

    public void setEjbRefType(final EjbRefType value) {
        this.ejbRefType = value;
    }

    public String getLocalHome() {
        return localHome;
    }

    public String getHome() {
        return getLocalHome();
    }

    public String getInterface() {
        return getLocal();
    }

    public Type getRefType() {
        return Type.LOCAL;
    }

    public void setRefType(final Type refType) {
    }

    public void setLocalHome(final String value) {
        this.localHome = value;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(final String value) {
        this.local = value;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    /**
     * The value of the ejb-link element must be the ejb-name of an
     * enterprise bean in the same ejb-jar file or in another ejb-jar
     * file in the same Java EE application unit.
     *
     * Alternatively, the name in the ejb-link element may be
     * composed of a path name specifying the ejb-jar containing the
     * referenced enterprise bean with the ejb-name of the target
     * bean appended and separated from the path name by "#".  The
     * path name is relative to the Deployment File containing
     * Deployment Component that is referencing the enterprise
     * bean.  This allows multiple enterprise beans with the same
     * ejb-name to be uniquely identified.
     *
     * Examples:
     *
     * <ejb-link>EmployeeRecord</ejb-link>
     *
     * <ejb-link>../products/product.jar#ProductEJB</ejb-link>
     */
    public void setEjbLink(final String value) {
        this.ejbLink = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(final String value) {
        this.mappedName = value;
    }

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(final String lookupName) {
        this.lookupName = lookupName;
    }

    public Set<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new HashSet<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    @Override
    public String toString() {
        return "EjbLocalRef{" +
            "name='" + ejbRefName + '\'' +
            ", local=" + local +
            ", link='" + ejbLink + '\'' +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
