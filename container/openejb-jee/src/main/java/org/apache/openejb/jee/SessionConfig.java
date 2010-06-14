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

