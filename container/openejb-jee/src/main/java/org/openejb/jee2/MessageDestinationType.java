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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The message-destinationType specifies a message
 * destination. The logical destination described by this
 * element is mapped to a physical destination by the Deployer.
 * <p/>
 * The message destination element contains:
 * <p/>
 * - an optional description
 * - an optional display-name
 * - an optional icon
 * - a message destination name which must be unique
 * among message destination names within the same
 * Deployment File.
 * - an optional mapped name
 * <p/>
 * Example:
 * <p/>
 * <message-destination>
 * <message-destination-name>CorporateStocks
 * </message-destination-name>
 * </message-destination>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-destinationType", propOrder = {
        "description",
        "displayName",
        "icon",
        "messageDestinationName",
        "mappedName"
        })
public class MessageDestinationType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "display-name", required = true)
    protected List<Text> displayName;
    @XmlElement(required = true)
    protected List<IconType> icon;
    @XmlElement(name = "message-destination-name", required = true)
    protected String messageDestinationName;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDisplayName().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<Text>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getIcon().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link IconType }
     */
    public List<IconType> getIcon() {
        if (icon == null) {
            icon = new ArrayList<IconType>();
        }
        return this.icon;
    }

    public String getMessageDestinationName() {
        return messageDestinationName;
    }

    public void setMessageDestinationName(String value) {
        this.messageDestinationName = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
