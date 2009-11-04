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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.apache.openejb.jee.was.v6.common.Description;
import org.apache.openejb.jee.was.v6.xmi.Extension;

/**
 * 
 * @since J2EE1.3 The ejb-relationship-role element describes a role within a
 *        relationship. There are two roles in each relationship. The
 *        ejb-relationship-role element contains an optional description; an
 *        optional name for the relationship role; a specification of the
 *        multiplicity of the role; an optional specification of cascade-delete
 *        functionality for the role; the role source; and a declaration of the
 *        cmr-field, if any, by means of which the other side of the
 *        relationship is accessed from the perspective of the role source. The
 *        multiplicity and relationship-role-source element are mandatory. The
 *        relationship-role-source element designates an entity-bean by means of
 *        an ejb-name element. For bidirectional relationships, both roles of a
 *        relationship must declare a relationship-role-source element that
 *        specifies a cmr-field in terms of which the relationship is accessed.
 *        The lack of a cmr-field element in an ejb-relationship-role specifies
 *        that the relationship is unidirectional in navigability and that
 *        entity bean that participates in the relationship is "not aware" of
 *        the relationship.
 * @invariant multiplicity != null
 * 
 * @invariant roleSource != null
 * @invariant Cascade delete can only be specified in an EJBRelationshipRole
 *            element in which the roleSource element specifies a dependent
 *            object class.
 * 
 * 
 * @invariant Cascade delete can only be specified for an EJBRelationshipRole
 *            contained in an EJBrelation in which the other EJBRelationshipRole
 *            element specifies a multiplicity of One.
 * 
 * 
 *            Example:
 * 
 *            <ejb-relation>
 * 
 *            <ejb-relation-name>Product-LineItem</ejb-relation-name>
 * 
 *            <ejb-relationship-role>
 * 
 *            
 *            <ejb-relationship-role-name>product-has-lineitems</ejb-relationship
 *            -role-name>
 * 
 *            <multiplicity>One</multiplicity>
 * 
 *            <relationship-role-source>
 * 
 *            <ejb-name>ProductEJB</ejb-name>
 * 
 *            </relationship-role-source>
 * 
 *            </ejb-relationship-role> ...
 * 
 * 
 *            <p>
 *            Java class for EJBRelationshipRole complex type.
 * 
 *            <p>
 *            The following schema fragment specifies the expected content
 *            contained within this class.
 * 
 *            <pre>
 * &lt;complexType name="EJBRelationshipRole">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="source" type="{ejb.xmi}RoleSource"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="cmrField" type="{ejb.xmi}CMRField"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="descriptions" type="{common.xmi}Description"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.omg.org/XMI}Extension"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/>
 *       &lt;attribute name="cascadeDelete" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="multiplicity" type="{ejb.xmi}MultiplicityKind" />
 *       &lt;attribute name="roleName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{http://www.omg.org/XMI}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EJBRelationshipRole", propOrder = { "sources", "cmrFields",
		"descriptions", "extensions" })
public class EJBRelationshipRole {

	@XmlElement(name = "source")
	protected List<RoleSource> sources;
	@XmlElement(name = "cmrField")
	protected List<CMRField> cmrFields;
	protected List<Description> descriptions;
	@XmlElement(name = "Extension", namespace = "http://www.omg.org/XMI")
	protected List<Extension> extensions;
	@XmlAttribute
	protected Boolean cascadeDelete;
	@XmlAttribute
	protected String description;
	@XmlAttribute
	protected MultiplicityEnum multiplicity;
	@XmlAttribute
	protected String roleName;
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
	 * Gets the value of the sources property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the sources property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSources().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link RoleSource }
	 * 
	 * 
	 */
	public List<RoleSource> getSources() {
		if (sources == null) {
			sources = new ArrayList<RoleSource>();
		}
		return this.sources;
	}

	/**
	 * Gets the value of the cmrFields property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the cmrFields property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCmrFields().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link CMRField }
	 * 
	 * 
	 */
	public List<CMRField> getCmrFields() {
		if (cmrFields == null) {
			cmrFields = new ArrayList<CMRField>();
		}
		return this.cmrFields;
	}

	/**
	 * Gets the value of the descriptions property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the descriptions property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDescriptions().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Description }
	 * 
	 * 
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
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the extensions property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExtensions().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Extension }
	 * 
	 * 
	 */
	public List<Extension> getExtensions() {
		if (extensions == null) {
			extensions = new ArrayList<Extension>();
		}
		return this.extensions;
	}

	/**
	 * Gets the value of the cascadeDelete property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isCascadeDelete() {
		return cascadeDelete;
	}

	/**
	 * Sets the value of the cascadeDelete property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setCascadeDelete(Boolean value) {
		this.cascadeDelete = value;
	}

	/**
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Gets the value of the multiplicity property.
	 * 
	 * @return possible object is {@link MultiplicityEnum }
	 * 
	 */
	public MultiplicityEnum getMultiplicity() {
		return multiplicity;
	}

	/**
	 * Sets the value of the multiplicity property.
	 * 
	 * @param value
	 *            allowed object is {@link MultiplicityEnum }
	 * 
	 */
	public void setMultiplicity(MultiplicityEnum value) {
		this.multiplicity = value;
	}

	/**
	 * Gets the value of the roleName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * Sets the value of the roleName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRoleName(String value) {
		this.roleName = value;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link QName }
	 * 
	 */
	public QName getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link QName }
	 * 
	 */
	public void setType(QName value) {
		this.type = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
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
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * Gets the value of the href property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Sets the value of the href property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHref(String value) {
		this.href = value;
	}

	/**
	 * Gets the value of the idref property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getIdref() {
		return idref;
	}

	/**
	 * Sets the value of the idref property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setIdref(Object value) {
		this.idref = value;
	}

	/**
	 * Gets the value of the label property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the value of the label property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLabel(String value) {
		this.label = value;
	}

	/**
	 * Gets the value of the uuid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the value of the uuid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUuid(String value) {
		this.uuid = value;
	}

}
