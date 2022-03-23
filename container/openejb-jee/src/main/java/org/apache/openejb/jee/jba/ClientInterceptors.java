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

package org.apache.openejb.jee.jba;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "home",
    "bean",
    "listEntity"
})
@XmlRootElement(name = "client-interceptors")
public class ClientInterceptors {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String exposeContainer;
    @XmlElement(required = true)
    protected Home home;
    @XmlElement(required = true)
    protected Bean bean;
    @XmlElement(name = "list-entity")
    protected ListEntity listEntity;

    /**
     * Gets the value of the exposeContainer property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExposeContainer() {
        if (exposeContainer == null) {
            return "false";
        } else {
            return exposeContainer;
        }
    }

    /**
     * Sets the value of the exposeContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExposeContainer(final String value) {
        this.exposeContainer = value;
    }

    /**
     * Gets the value of the home property.
     *
     * @return possible object is
     * {@link Home }
     */
    public Home getHome() {
        return home;
    }

    /**
     * Sets the value of the home property.
     *
     * @param value allowed object is
     *              {@link Home }
     */
    public void setHome(final Home value) {
        this.home = value;
    }

    /**
     * Gets the value of the bean property.
     *
     * @return possible object is
     * {@link Bean }
     */
    public Bean getBean() {
        return bean;
    }

    /**
     * Sets the value of the bean property.
     *
     * @param value allowed object is
     *              {@link Bean }
     */
    public void setBean(final Bean value) {
        this.bean = value;
    }

    /**
     * Gets the value of the listEntity property.
     *
     * @return possible object is
     * {@link ListEntity }
     */
    public ListEntity getListEntity() {
        return listEntity;
    }

    /**
     * Sets the value of the listEntity property.
     *
     * @param value allowed object is
     *              {@link ListEntity }
     */
    public void setListEntity(final ListEntity value) {
        this.listEntity = value;
    }

}
