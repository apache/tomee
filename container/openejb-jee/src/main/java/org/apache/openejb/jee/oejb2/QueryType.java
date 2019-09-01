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

package org.apache.openejb.jee.oejb2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for queryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="queryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="query-method"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="method-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="method-params"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="method-param" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="result-type-mapping" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ejb-ql" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="no-cache-flush" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="group-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryType", propOrder = {
    "queryMethod",
    "resultTypeMapping",
    "ejbQl",
    "noCacheFlush",
    "groupName"
})
public class QueryType {

    @XmlElement(name = "query-method", required = true)
    protected QueryType.QueryMethod queryMethod;
    @XmlElement(name = "result-type-mapping")
    protected String resultTypeMapping;
    @XmlElement(name = "ejb-ql")
    protected String ejbQl;
    @XmlElement(name = "no-cache-flush")
    protected Object noCacheFlush;
    @XmlElement(name = "group-name")
    protected String groupName;

    /**
     * Gets the value of the queryMethod property.
     *
     * @return possible object is
     * {@link QueryType.QueryMethod }
     */
    public QueryType.QueryMethod getQueryMethod() {
        return queryMethod;
    }

    /**
     * Sets the value of the queryMethod property.
     *
     * @param value allowed object is
     *              {@link QueryType.QueryMethod }
     */
    public void setQueryMethod(final QueryType.QueryMethod value) {
        this.queryMethod = value;
    }

    /**
     * Gets the value of the resultTypeMapping property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getResultTypeMapping() {
        return resultTypeMapping;
    }

    /**
     * Sets the value of the resultTypeMapping property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResultTypeMapping(final String value) {
        this.resultTypeMapping = value;
    }

    /**
     * Gets the value of the ejbQl property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEjbQl() {
        return ejbQl;
    }

    /**
     * Sets the value of the ejbQl property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbQl(final String value) {
        this.ejbQl = value;
    }

    /**
     * Gets the value of the noCacheFlush property.
     *
     * @return possible object is
     * {@link Object }
     */
    public Object getNoCacheFlush() {
        return noCacheFlush;
    }

    /**
     * Sets the value of the noCacheFlush property.
     *
     * @param value allowed object is
     *              {@link Object }
     */
    public void setNoCacheFlush(final Object value) {
        this.noCacheFlush = value;
    }

    /**
     * Gets the value of the groupName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGroupName(final String value) {
        this.groupName = value;
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
     *       &lt;sequence&gt;
     *         &lt;element name="method-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="method-params"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="method-param" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
    public static class QueryMethod {

        @XmlElement(name = "method-name", required = true)
        protected String methodName;
        @XmlElement(name = "method-params", required = true)
        protected QueryType.QueryMethod.MethodParams methodParams;

        /**
         * Gets the value of the methodName property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMethodName() {
            return methodName;
        }

        /**
         * Sets the value of the methodName property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMethodName(final String value) {
            this.methodName = value;
        }

        /**
         * Gets the value of the methodParams property.
         *
         * @return possible object is
         * {@link QueryType.QueryMethod.MethodParams }
         */
        public QueryType.QueryMethod.MethodParams getMethodParams() {
            return methodParams;
        }

        /**
         * Sets the value of the methodParams property.
         *
         * @param value allowed object is
         *              {@link QueryType.QueryMethod.MethodParams }
         */
        public void setMethodParams(final QueryType.QueryMethod.MethodParams value) {
            this.methodParams = value;
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
         *       &lt;sequence&gt;
         *         &lt;element name="method-param" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "methodParam"
        })
        public static class MethodParams {

            @XmlElement(name = "method-param")
            protected List<String> methodParam;

            /**
             * Gets the value of the methodParam property.
             *
             *
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the methodParam property.
             *
             *
             * For example, to add a new item, do as follows:
             * <pre>
             *    getMethodParam().add(newItem);
             * </pre>
             *
             *
             *
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             */
            public List<String> getMethodParam() {
                if (methodParam == null) {
                    methodParam = new ArrayList<String>();
                }
                return this.methodParam;
            }

        }

    }

}
