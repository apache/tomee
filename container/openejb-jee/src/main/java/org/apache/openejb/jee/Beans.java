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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}interceptors" minOccurs="0"/>
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}decorators" minOccurs="0"/>
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}alternatives" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "beans")
public class Beans {
    @XmlTransient
    protected List<String> duplicatedInterceptors;

    @XmlTransient
    protected List<String> duplicatedDecorators;

    @XmlTransient
    protected Alternatives duplicatedAlternatives;

    @XmlTransient
    protected List<String> startupBeans;

    @XmlTransient
    private final List<String> managedClasses = new ArrayList<String>();

    @XmlElementWrapper(name = "interceptors")
    @XmlElement(name = "class")
    protected List<String> interceptors;

    @XmlElementWrapper(name = "decorators")
    @XmlElement(name = "class")
    protected List<String> decorators;

    protected Alternatives alternatives;

    public List<String> getManagedClasses() {
        return managedClasses;
    }

    public void addManagedClass(final String className) {
        managedClasses.add(className);
    }

    public void addManagedClass(final Class clazz) {
        addManagedClass(clazz.getName());
    }

    public List<String> getStartupBeans() {
        if (startupBeans == null) {
            startupBeans = new LinkedList<String>();
        }
        return startupBeans;
    }

    public List<String> getInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<String>();
        }
        return interceptors;
    }

    public void addInterceptor(final String className) {
        getInterceptors().add(className);
    }

    public void addInterceptor(final Class clazz) {
        addInterceptor(clazz.getName());
    }

    public List<String> getDecorators() {
        if (decorators == null) {
            decorators = new ArrayList<String>();
        }
        return decorators;
    }

    public void addDecorator(final String className) {
        getDecorators().add(className);
    }

    public void addDecorator(final Class clazz) {
        addDecorator(clazz.getName());
    }

    public List<String> getAlternativeClasses() {
        return getAlternatives().getClasses();
    }

    public void addAlternativeClass(final String className) {
        getAlternativeClasses().add(className);
    }

    public void addAlternativeClass(final Class clazz) {
        addAlternativeClass(clazz.getName());
    }

    public List<String> getAlternativeStereotypes() {
        return getAlternatives().getStereotypes();
    }

    public void addAlternativeStereotype(final String className) {
        getAlternativeStereotypes().add(className);
    }

    public void addAlternativeStereotype(final Class clazz) {
        addAlternativeStereotype(clazz.getName());
    }

    /**
     * Gets the value of the alternatives property.
     *
     * @return possible object is
     * {@link Alternatives }
     */
    private Alternatives getAlternatives() {
        if (alternatives == null) {
            alternatives = new Alternatives();
        }
        return alternatives;
    }


    /**
     * <p>Java class for anonymous complex type.
     * <p/>
     * <p>The following schema fragment specifies the expected content contained within this class.
     * <p/>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice maxOccurs="unbounded" minOccurs="0">
     *         &lt;element name="class" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="stereotype" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "classes",
        "stereotypes"
    })
    @XmlRootElement(name = "alternatives")
    public static class Alternatives {

        @XmlElement(name = "class")
        protected List<String> classes;

        @XmlElement(name = "stereotype")
        protected List<String> stereotypes;

        public List<String> getClasses() {
            if (classes == null) {
                classes = new ArrayList<String>();
            }
            return classes;
        }

        public List<String> getStereotypes() {
            if (stereotypes == null) {
                stereotypes = new ArrayList<String>();
            }
            return stereotypes;
        }
    }

    public List<String> getDuplicatedInterceptors() {
        if (duplicatedInterceptors == null) {
            duplicatedInterceptors = new ArrayList<String>();
        }
        return duplicatedInterceptors;
    }

    public List<String> getDuplicatedDecorators() {
        if (duplicatedDecorators == null) {
            duplicatedDecorators = new ArrayList<String>();
        }
        return duplicatedDecorators;
    }

    public Alternatives getDuplicatedAlternatives() {
        if (duplicatedAlternatives == null) {
            duplicatedAlternatives = new Alternatives();
        }
        return duplicatedAlternatives;
    }

    public void removeDuplicates() {
        removeDuplicates(getAlternativeClasses());
        removeDuplicates(getAlternativeStereotypes());
        removeDuplicates(getDecorators());
        removeDuplicates(getInterceptors());
    }

    private <T> void removeDuplicates(final List<T> list) {
        // don't use a set to keep order
        final List<T> classes = new ArrayList<T>();
        for (final T t : list) {
            if (!classes.contains(t)) {
                classes.add(t);
            }
        }
        list.clear();
        list.addAll(classes);
    }
}
