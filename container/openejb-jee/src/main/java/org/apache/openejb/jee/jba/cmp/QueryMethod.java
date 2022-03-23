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

package org.apache.openejb.jee.jba.cmp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://jboss.org}method-name"/&gt;
 *         &lt;element ref="{http://jboss.org}method-params"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "methodName",
    "methodParams"
})
@XmlRootElement(name = "query-method")
public class QueryMethod {

    @XmlElement(name = "method-name", required = true)
    protected MethodName methodName;
    @XmlElement(name = "method-params", required = true)
    protected MethodParams methodParams;

    /**
     * Gets the value of the methodName property.
     *
     * @return possible object is
     * {@link MethodName }
     */
    public MethodName getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     *
     * @param value allowed object is
     *              {@link MethodName }
     */
    public void setMethodName(final MethodName value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the methodParams property.
     *
     * @return possible object is
     * {@link MethodParams }
     */
    public MethodParams getMethodParams() {
        return methodParams;
    }

    /**
     * Sets the value of the methodParams property.
     *
     * @param value allowed object is
     *              {@link MethodParams }
     */
    public void setMethodParams(final MethodParams value) {
        this.methodParams = value;
    }

}
