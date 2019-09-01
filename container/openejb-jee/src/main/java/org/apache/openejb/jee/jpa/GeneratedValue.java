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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.jpa;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface GeneratedValue {
 * GenerationType strategy() default AUTO;
 * String generator() default "";
 * }
 *
 *
 *
 * <p>Java class for generated-value complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="generated-value"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="generator" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="strategy" type="{http://java.sun.com/xml/ns/persistence/orm}generation-type" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generated-value")
public class GeneratedValue {

    @XmlAttribute
    protected String generator;
    @XmlAttribute
    protected GenerationType strategy;

    public GeneratedValue() {
    }

    public GeneratedValue(final GenerationType strategy) {
        this.strategy = strategy;
    }

    public GeneratedValue(final GenerationType strategy, final String generator) {
        this.strategy = strategy;
        this.generator = generator;
    }

    /**
     * Gets the value of the generator property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Sets the value of the generator property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGenerator(final String value) {
        this.generator = value;
    }

    /**
     * Gets the value of the strategy property.
     *
     * @return possible object is
     * {@link GenerationType }
     */
    public GenerationType getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     *
     * @param value allowed object is
     *              {@link GenerationType }
     */
    public void setStrategy(final GenerationType value) {
        this.strategy = value;
    }

}
