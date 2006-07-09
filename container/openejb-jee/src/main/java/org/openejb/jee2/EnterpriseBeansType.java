/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The enterprise-beansType declares one or more enterprise
 * beans. Each bean can be a session, entity or message-driven
 * bean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enterprise-beansType", propOrder = {
        "sessionOrEntityOrMessageDriven"
        })
public class EnterpriseBeansType {

    @XmlElements({
    @XmlElement(name = "message-driven", required = true, type = MessageDrivenBeanType.class),
    @XmlElement(name = "session", required = true, type = SessionBeanType.class),
    @XmlElement(name = "entity", required = true, type = EntityBeanType.class)
            })
    protected List<Object> sessionOrEntityOrMessageDriven;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the sessionOrEntityOrMessageDriven property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sessionOrEntityOrMessageDriven property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getSessionOrEntityOrMessageDriven().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDrivenBeanType }
     * {@link SessionBeanType }
     * {@link EntityBeanType }
     */
    public List<Object> getSessionOrEntityOrMessageDriven() {
        if (sessionOrEntityOrMessageDriven == null) {
            sessionOrEntityOrMessageDriven = new ArrayList<Object>();
        }
        return this.sessionOrEntityOrMessageDriven;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
