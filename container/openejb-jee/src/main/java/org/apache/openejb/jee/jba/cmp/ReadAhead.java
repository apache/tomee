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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://jboss.org}strategy"/>
 *         &lt;element ref="{http://jboss.org}page-size" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}eager-load-group" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}left-join" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "strategy",
    "pageSize",
    "eagerLoadGroup",
    "leftJoin"
})
@XmlRootElement(name = "read-ahead")
public class ReadAhead {

    @XmlElement(required = true)
    protected Strategy strategy;
    @XmlElement(name = "page-size")
    protected PageSize pageSize;
    @XmlElement(name = "eager-load-group")
    protected EagerLoadGroup eagerLoadGroup;
    @XmlElement(name = "left-join")
    protected List<LeftJoin> leftJoin;

    /**
     * Gets the value of the strategy property.
     * 
     * @return
     *     possible object is
     *     {@link Strategy }
     *     
     */
    public Strategy getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Strategy }
     *     
     */
    public void setStrategy(Strategy value) {
        this.strategy = value;
    }

    /**
     * Gets the value of the pageSize property.
     * 
     * @return
     *     possible object is
     *     {@link PageSize }
     *     
     */
    public PageSize getPageSize() {
        return pageSize;
    }

    /**
     * Sets the value of the pageSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link PageSize }
     *     
     */
    public void setPageSize(PageSize value) {
        this.pageSize = value;
    }

    /**
     * Gets the value of the eagerLoadGroup property.
     * 
     * @return
     *     possible object is
     *     {@link EagerLoadGroup }
     *     
     */
    public EagerLoadGroup getEagerLoadGroup() {
        return eagerLoadGroup;
    }

    /**
     * Sets the value of the eagerLoadGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link EagerLoadGroup }
     *     
     */
    public void setEagerLoadGroup(EagerLoadGroup value) {
        this.eagerLoadGroup = value;
    }

    /**
     * Gets the value of the leftJoin property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the leftJoin property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLeftJoin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LeftJoin }
     * 
     * 
     */
    public List<LeftJoin> getLeftJoin() {
        if (leftJoin == null) {
            leftJoin = new ArrayList<LeftJoin>();
        }
        return this.leftJoin;
    }

}
