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
package org.apache.openejb.jee.was.v6.java;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.ecore.EClass;

/**
 * <p>
 * Java class for JavaClass complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="JavaClass">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EClass">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="implementsInterfaces" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="classImport" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="packageImports" type="{java.xmi}JavaPackage"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="fields" type="{java.xmi}Field"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="methods" type="{java.xmi}Method"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="initializers" type="{java.xmi}Initializer"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="declaredClasses" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="declaringClass" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="events" type="{java.xmi}JavaEvent"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="classImport" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="declaredClasses" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="declaringClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="final" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="implementsInterfaces" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="kind" type="{java.xmi}TypeKind" />
 *       &lt;attribute name="packageImports" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="public" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JavaClass", propOrder = { "javaClassImplementsInterfaces",
		"javaClassImports", "javaPackageImports", "fields", "methods",
		"initializers", "javaDeclaredClasses", "javaDeclaringClasses", "events" })
public class JavaClass extends EClass {

	@XmlElement(name = "implementsInterfaces")
	protected List<JavaClass> javaClassImplementsInterfaces;
	@XmlElement(name = "classImport")
	protected List<JavaClass> javaClassImports;
	@XmlElement(name = "packageImports")
	protected List<JavaPackage> javaPackageImports;
	protected List<Field> fields;
	protected List<Method> methods;
	protected List<Initializer> initializers;
	@XmlElement(name = "declaredClasses")
	protected List<JavaClass> javaDeclaredClasses;
	@XmlElement(name = "declaringClass")
	protected List<JavaClass> javaDeclaringClasses;
	protected List<JavaEvent> events;
	@XmlAttribute
	protected String classImport;
	@XmlAttribute
	protected String declaredClasses;
	@XmlAttribute
	protected String declaringClass;
	@XmlAttribute(name = "final")
	protected Boolean isFinal;
	@XmlAttribute
	protected String implementsInterfaces;
	@XmlAttribute
	protected TypeKind kind;
	@XmlAttribute
	protected String packageImports;
	@XmlAttribute(name = "public")
	protected Boolean isPublic;

	/**
	 * Gets the value of the javaClassImplementsInterfaces property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the javaClassImplementsInterfaces property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getJavaClassImplementsInterfaces().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getJavaClassImplementsInterfaces() {
		if (javaClassImplementsInterfaces == null) {
			javaClassImplementsInterfaces = new ArrayList<JavaClass>();
		}
		return this.javaClassImplementsInterfaces;
	}

	/**
	 * Gets the value of the javaClassImports property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the javaClassImports property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getJavaClassImports().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getJavaClassImports() {
		if (javaClassImports == null) {
			javaClassImports = new ArrayList<JavaClass>();
		}
		return this.javaClassImports;
	}

	/**
	 * Gets the value of the javaPackageImports property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the javaPackageImports property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getJavaPackageImports().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaPackage }
	 * 
	 * 
	 */
	public List<JavaPackage> getJavaPackageImports() {
		if (javaPackageImports == null) {
			javaPackageImports = new ArrayList<JavaPackage>();
		}
		return this.javaPackageImports;
	}

	/**
	 * Gets the value of the fields property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the fields property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFields().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Field }
	 * 
	 * 
	 */
	public List<Field> getFields() {
		if (fields == null) {
			fields = new ArrayList<Field>();
		}
		return this.fields;
	}

	/**
	 * Gets the value of the methods property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the methods property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMethods().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Method }
	 * 
	 * 
	 */
	public List<Method> getMethods() {
		if (methods == null) {
			methods = new ArrayList<Method>();
		}
		return this.methods;
	}

	/**
	 * Gets the value of the initializers property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the initializers property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getInitializers().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Initializer }
	 * 
	 * 
	 */
	public List<Initializer> getInitializers() {
		if (initializers == null) {
			initializers = new ArrayList<Initializer>();
		}
		return this.initializers;
	}

	/**
	 * Gets the value of the javaDeclaredClasses property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the javaDeclaredClasses property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getJavaDeclaredClasses().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getJavaDeclaredClasses() {
		if (javaDeclaredClasses == null) {
			javaDeclaredClasses = new ArrayList<JavaClass>();
		}
		return this.javaDeclaredClasses;
	}

	/**
	 * Gets the value of the javaDeclaringClasses property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the javaDeclaringClasses property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getJavaDeclaringClasses().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getJavaDeclaringClasses() {
		if (javaDeclaringClasses == null) {
			javaDeclaringClasses = new ArrayList<JavaClass>();
		}
		return this.javaDeclaringClasses;
	}

	/**
	 * Gets the value of the events property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the events property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEvents().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaEvent }
	 * 
	 * 
	 */
	public List<JavaEvent> getEvents() {
		if (events == null) {
			events = new ArrayList<JavaEvent>();
		}
		return this.events;
	}

	/**
	 * Gets the value of the classImport property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClassImport() {
		return classImport;
	}

	/**
	 * Sets the value of the classImport property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClassImport(String value) {
		this.classImport = value;
	}

	/**
	 * Gets the value of the declaredClasses property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDeclaredClasses() {
		return declaredClasses;
	}

	/**
	 * Sets the value of the declaredClasses property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDeclaredClasses(String value) {
		this.declaredClasses = value;
	}

	/**
	 * Gets the value of the declaringClass property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * Sets the value of the declaringClass property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDeclaringClass(String value) {
		this.declaringClass = value;
	}

	/**
	 * Gets the value of the isFinal property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isIsFinal() {
		return isFinal;
	}

	/**
	 * Sets the value of the isFinal property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsFinal(Boolean value) {
		this.isFinal = value;
	}

	/**
	 * Gets the value of the implementsInterfaces property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getImplementsInterfaces() {
		return implementsInterfaces;
	}

	/**
	 * Sets the value of the implementsInterfaces property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setImplementsInterfaces(String value) {
		this.implementsInterfaces = value;
	}

	/**
	 * Gets the value of the kind property.
	 * 
	 * @return possible object is {@link TypeKind }
	 * 
	 */
	public TypeKind getKind() {
		return kind;
	}

	/**
	 * Sets the value of the kind property.
	 * 
	 * @param value
	 *            allowed object is {@link TypeKind }
	 * 
	 */
	public void setKind(TypeKind value) {
		this.kind = value;
	}

	/**
	 * Gets the value of the packageImports property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPackageImports() {
		return packageImports;
	}

	/**
	 * Sets the value of the packageImports property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPackageImports(String value) {
		this.packageImports = value;
	}

	/**
	 * Gets the value of the isPublic property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isIsPublic() {
		return isPublic;
	}

	/**
	 * Sets the value of the isPublic property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsPublic(Boolean value) {
		this.isPublic = value;
	}

}
