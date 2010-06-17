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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * web-common_3_0.xsd
 *
 * <p>Java class for session-configType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="session-configType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="session-timeout" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/>
 *         &lt;element name="cookie-config" type="{http://java.sun.com/xml/ns/javaee}cookie-configType" minOccurs="0"/>
 *         &lt;element name="tracking-mode" type="{http://java.sun.com/xml/ns/javaee}tracking-modeType" maxOccurs="3" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "session-configType", propOrder = {
    "sessionTimeout",
    "cookieConfig",
    "trackingMode"
})
public class SessionConfig {

    @XmlElement(name = "session-timeout")
    protected BigInteger sessionTimeout;
    @XmlElement(name = "cookie-config")
    protected CookieConfig cookieConfig;
    @XmlElement(name = "tracking-mode")
    protected List<TrackingMode> trackingMode;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public BigInteger getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(BigInteger value) {
        this.sessionTimeout = value;
    }

    public CookieConfig getCookieConfig() {
        return cookieConfig;
    }

    public void setCookieConfig(CookieConfig value) {
        this.cookieConfig = value;
    }

    public List<TrackingMode> getTrackingMode() {
        if (trackingMode == null) {
            trackingMode = new ArrayList<TrackingMode>();
        }
        return this.trackingMode;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}

