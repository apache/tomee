/**
 *
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The message-destination-ref element contains a declaration
 * of Deployment Component's reference to a message destination
 * associated with a resource in Deployment Component's
 * environment. It consists of:
 * <p/>
 * - an optional description
 * - the message destination reference name
 * - an optional message destination type
 * - an optional specification as to whether
 * the destination is used for
 * consuming or producing messages, or both.
 * if not specified, "both" is assumed.
 * - an optional link to the message destination
 * - optional injection targets
 * <p/>
 * The message destination type must be supplied unless an
 * injection target is specified, in which case the type
 * of the target is used.  If both are specified, the type
 * must be assignment compatible with the type of the injection
 * target.
 * <p/>
 * Examples:
 * <p/>
 * <message-destination-ref>
 * <message-destination-ref-name>jms/StockQueue
 * </message-destination-ref-name>
 * <message-destination-type>javax.jms.Queue
 * </message-destination-type>
 * <message-destination-usage>Consumes
 * </message-destination-usage>
 * <message-destination-link>CorporateStocks
 * </message-destination-link>
 * </message-destination-ref>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-destination-refType", propOrder = {
        "description",
        "messageDestinationRefName",
        "messageDestinationType",
        "messageDestinationUsage",
        "messageDestinationLink",
        "mappedName",
        "injectionTarget"
        })
public class MessageDestinationRef implements Injectable {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "message-destination-ref-name", required = true)
    protected String messageDestinationRefName;
    @XmlElement(name = "message-destination-type")
    protected String messageDestinationType;
    @XmlElement(name = "message-destination-usage")
    protected MessageDestinationUsage messageDestinationUsage;
    @XmlElement(name = "message-destination-link")
    protected String messageDestinationLink;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    public String getName() {
        return getMessageDestinationRefName();
    }

    @XmlTransient
    public String getType() {
        return getMessageDestinationType();
    }

    public void setName(String name) {
        setMessageDestinationRefName(name);
    }

    public void setType(String type) {
        setMessageDestinationType(type);
    }

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getMessageDestinationRefName() {
        return messageDestinationRefName;
    }

    public void setMessageDestinationRefName(String value) {
        this.messageDestinationRefName = value;
    }

    public String getMessageDestinationType() {
        return messageDestinationType;
    }

    public void setMessageDestinationType(String value) {
        this.messageDestinationType = value;
    }

    public MessageDestinationUsage getMessageDestinationUsage() {
        return messageDestinationUsage;
    }

    public void setMessageDestinationUsage(MessageDestinationUsage value) {
        this.messageDestinationUsage = value;
    }

    /**
     * The Assembler sets the value to reflect the flow of messages
     * between producers and consumers in the application.
     * <p/>
     * The value must be the message-destination-name of a message
     * destination in the same Deployment File or in another
     * Deployment File in the same Java EE application unit.
     * <p/>
     * Alternatively, the value may be composed of a path name
     * specifying a Deployment File containing the referenced
     * message destination with the message-destination-name of the
     * destination appended and separated from the path name by
     * "#". The path name is relative to the Deployment File
     * containing Deployment Component that is referencing the
     * message destination.  This allows multiple message
     * destinations with the same name to be uniquely identified.
     */
    public String getMessageDestinationLink() {
        return messageDestinationLink;
    }

    public void setMessageDestinationLink(String value) {
        this.messageDestinationLink = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public List<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
