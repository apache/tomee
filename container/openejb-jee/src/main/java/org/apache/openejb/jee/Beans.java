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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}interceptors" minOccurs="0"/&gt;
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}decorators" minOccurs="0"/&gt;
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}alternatives" minOccurs="0"/&gt;
 *         &lt;element ref="{http://java.sun.com/xml/ns/javaee}trim" minOccurs="0" maxOccurs="1"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "beans")
public class Beans {
    private static final URL DEFAULT_URL = null;

    @XmlTransient
    protected List<String> duplicatedInterceptors;

    @XmlTransient
    protected List<String> duplicatedDecorators;

    @XmlTransient
    protected List<String> startupBeans;

    @XmlTransient
    protected Alternatives duplicatedAlternatives;

    @XmlTransient
    private final Map<URL, List<String>> managedClasses = new HashMap<>();

    @XmlTransient
    private final Map<URL, List<String>> notManagedClasses = new HashMap<>();

    @XmlElementWrapper(name = "interceptors")
    @XmlElement(name = "class")
    protected List<String> interceptors;

    @XmlElementWrapper(name = "decorators")
    @XmlElement(name = "class")
    protected List<String> decorators;

    protected Alternatives alternatives;

    @XmlElement(name = "trim")
    protected String trim;

    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version;

    @XmlAttribute(name = "bean-discovery-mode", required = true)
    protected String beanDiscoveryMode;

    @XmlElement
    protected Scan scan;

    @XmlTransient
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public Scan getScan() {
        if (scan == null) {
            scan = new Scan();
        }
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getBeanDiscoveryMode() {
        return beanDiscoveryMode;
    }

    public void setBeanDiscoveryMode(final String beanDiscoveryMode) {
        this.beanDiscoveryMode = beanDiscoveryMode;
    }

    /**
     * only for ApplicationComposer
     * @param clazz the class to add
     */
    public Beans managedClass(final String clazz) {
        addManagedClass(null, clazz);
        return this;
    }

    public void addManagedClass(final URL url, final String clazz) {
        List<String> list = managedClasses.computeIfAbsent(url, k -> new LinkedList<>());
        list.add(clazz);
    }

    public Map<URL, List<String>> getManagedClasses() {
        return managedClasses;
    }

    public Map<URL, List<String>> getNotManagedClasses() {
        return notManagedClasses;
    }

    @Deprecated
    public void addManagedClass(final String className) {
        addManagedClass(DEFAULT_URL, className);
    }

    @Deprecated
    public void addManagedClass(final Class clazz) {
        addManagedClass(clazz.getName());
    }

    public List<String> getInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<>();
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
            decorators = new ArrayList<>();
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

    public String getTrim() {
        return trim;
    }

    public void setTrim(String trim) {
        this.trim = trim;
    }

    public boolean isTrim() {
        return trim != null;
    }

    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *         &lt;element name="class" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="stereotype" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
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
                classes = new ArrayList<>();
            }
            return classes;
        }

        public List<String> getStereotypes() {
            if (stereotypes == null) {
                stereotypes = new ArrayList<>();
            }
            return stereotypes;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "scan")
    public static class Scan {
        protected List<Scan.Exclude> exclude;

        public List<Scan.Exclude> getExclude() {
            if (exclude == null) {
                exclude = new ArrayList<>();
            }
            return this.exclude;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Exclude {
            @XmlElements({
                    @XmlElement(name = "if-class-available", type = IfAvailableClassCondition.class),
                    @XmlElement(name = "if-class-not-available", type = IfNotAvailableClassCondition.class),
                    @XmlElement(name = "if-system-property", type = IfSystemProperty.class)
            })
            protected List<Object> ifClassAvailableOrIfClassNotAvailableOrIfSystemProperty;

            @XmlAttribute(name = "name", required = true)
            protected String name;

            public List<Object> getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty() {
                if (ifClassAvailableOrIfClassNotAvailableOrIfSystemProperty == null) {
                    ifClassAvailableOrIfClassNotAvailableOrIfSystemProperty = new ArrayList<>();
                }
                return this.ifClassAvailableOrIfClassNotAvailableOrIfSystemProperty;
            }

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public static class IfAvailableClassCondition extends ClassCondition {
            }

            public static class IfNotAvailableClassCondition extends ClassCondition {
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            public static class ClassCondition {
                @XmlAttribute(name = "name", required = true)
                protected String name;

                public String getName() {
                    return name;
                }

                public void setName(String value) {
                    this.name = value;
                }
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            public static class IfSystemProperty {
                @XmlAttribute(name = "name", required = true)
                protected String name;

                @XmlAttribute(name = "value")
                protected String value;

                public String getName() {
                    return name;
                }

                public void setName(String value) {
                    this.name = value;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }
        }
    }

    public List<String> getDuplicatedInterceptors() {
        if (duplicatedInterceptors == null) {
            duplicatedInterceptors = new ArrayList<>();
        }
        return duplicatedInterceptors;
    }

    public List<String> getDuplicatedDecorators() {
        if (duplicatedDecorators == null) {
            duplicatedDecorators = new ArrayList<>();
        }
        return duplicatedDecorators;
    }

    public Alternatives getDuplicatedAlternatives() {
        if (duplicatedAlternatives == null) {
            duplicatedAlternatives = new Alternatives();
        }
        return duplicatedAlternatives;
    }

    public List<String> getStartupBeans() {
        if (startupBeans == null) {
            startupBeans = new LinkedList<>();
        }
        return startupBeans;
    }

    public void removeDuplicates() {
        removeDuplicates(getAlternativeClasses());
        removeDuplicates(getAlternativeStereotypes());
        removeDuplicates(getDecorators());
        removeDuplicates(getInterceptors());
    }

    private <T> void removeDuplicates(final List<T> list) {
        // don't use a set to keep order
        final List<T> classes = new ArrayList<>();
        for (final T t : list) {
            if (!classes.contains(t)) {
                classes.add(t);
            }
        }
        list.clear();
        list.addAll(classes);
    }
}
