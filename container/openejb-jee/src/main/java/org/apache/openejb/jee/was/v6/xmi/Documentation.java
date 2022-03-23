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
package org.apache.openejb.jee.was.v6.xmi;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 *
 * Java class for Documentation complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="Documentation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="contact" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="exporter" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="exporterVersion" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="longDescription" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="shortDescription" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="notice" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="owner" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/&gt;
 *       &lt;attribute name="contact" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="exporter" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="exporterVersion" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="longDescription" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="notice" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="owner" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="shortDescription" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Documentation", propOrder = {"contacts", "exporters",
    "exporterVersions", "longDescriptions", "shortDescriptions", "notices",
    "owners"})
public class Documentation {

    @XmlElement(name = "contact")
    protected List<String> contacts;
    @XmlElement(name = "exporter")
    protected List<String> exporters;
    @XmlElement(name = "exporterVersion")
    protected List<String> exporterVersions;
    @XmlElement(name = "longDescription")
    protected List<String> longDescriptions;
    @XmlElement(name = "shortDescription")
    protected List<String> shortDescriptions;
    @XmlElement(name = "notice")
    protected List<String> notices;
    @XmlElement(name = "owner")
    protected List<String> owners;
    @XmlAttribute
    protected String contact;
    @XmlAttribute
    protected String exporter;
    @XmlAttribute
    protected String exporterVersion;
    @XmlAttribute
    protected String longDescription;
    @XmlAttribute
    protected String notice;
    @XmlAttribute
    protected String owner;
    @XmlAttribute
    protected String shortDescription;
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
     * Gets the value of the contacts property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the contacts property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getContacts().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<String>();
        }
        return this.contacts;
    }

    /**
     * Gets the value of the exporters property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the exporters property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getExporters().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getExporters() {
        if (exporters == null) {
            exporters = new ArrayList<String>();
        }
        return this.exporters;
    }

    /**
     * Gets the value of the exporterVersions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the exporterVersions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getExporterVersions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getExporterVersions() {
        if (exporterVersions == null) {
            exporterVersions = new ArrayList<String>();
        }
        return this.exporterVersions;
    }

    /**
     * Gets the value of the longDescriptions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the longDescriptions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getLongDescriptions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getLongDescriptions() {
        if (longDescriptions == null) {
            longDescriptions = new ArrayList<String>();
        }
        return this.longDescriptions;
    }

    /**
     * Gets the value of the shortDescriptions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the shortDescriptions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getShortDescriptions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getShortDescriptions() {
        if (shortDescriptions == null) {
            shortDescriptions = new ArrayList<String>();
        }
        return this.shortDescriptions;
    }

    /**
     * Gets the value of the notices property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the notices property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getNotices().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getNotices() {
        if (notices == null) {
            notices = new ArrayList<String>();
        }
        return this.notices;
    }

    /**
     * Gets the value of the owners property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the owners property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getOwners().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getOwners() {
        if (owners == null) {
            owners = new ArrayList<String>();
        }
        return this.owners;
    }

    /**
     * Gets the value of the contact property.
     *
     * @return possible object is {@link String }
     */
    public String getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContact(final String value) {
        this.contact = value;
    }

    /**
     * Gets the value of the exporter property.
     *
     * @return possible object is {@link String }
     */
    public String getExporter() {
        return exporter;
    }

    /**
     * Sets the value of the exporter property.
     *
     * @param value allowed object is {@link String }
     */
    public void setExporter(final String value) {
        this.exporter = value;
    }

    /**
     * Gets the value of the exporterVersion property.
     *
     * @return possible object is {@link String }
     */
    public String getExporterVersion() {
        return exporterVersion;
    }

    /**
     * Sets the value of the exporterVersion property.
     *
     * @param value allowed object is {@link String }
     */
    public void setExporterVersion(final String value) {
        this.exporterVersion = value;
    }

    /**
     * Gets the value of the longDescription property.
     *
     * @return possible object is {@link String }
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Sets the value of the longDescription property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLongDescription(final String value) {
        this.longDescription = value;
    }

    /**
     * Gets the value of the notice property.
     *
     * @return possible object is {@link String }
     */
    public String getNotice() {
        return notice;
    }

    /**
     * Sets the value of the notice property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNotice(final String value) {
        this.notice = value;
    }

    /**
     * Gets the value of the owner property.
     *
     * @return possible object is {@link String }
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOwner(final String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the shortDescription property.
     *
     * @return possible object is {@link String }
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets the value of the shortDescription property.
     *
     * @param value allowed object is {@link String }
     */
    public void setShortDescription(final String value) {
        this.shortDescription = value;
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
