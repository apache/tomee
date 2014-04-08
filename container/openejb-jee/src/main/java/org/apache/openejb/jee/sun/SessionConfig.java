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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sessionManager",
    "sessionProperties",
    "cookieProperties"
})
public class SessionConfig {
    @XmlElement(name = "session-manager")
    protected SessionManager sessionManager;
    @XmlElement(name = "session-properties")
    protected SessionProperties sessionProperties;
    @XmlElement(name = "cookie-properties")
    protected CookieProperties cookieProperties;

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager value) {
        this.sessionManager = value;
    }

    public SessionProperties getSessionProperties() {
        return sessionProperties;
    }

    public void setSessionProperties(SessionProperties value) {
        this.sessionProperties = value;
    }

    public CookieProperties getCookieProperties() {
        return cookieProperties;
    }

    public void setCookieProperties(CookieProperties value) {
        this.cookieProperties = value;
    }
}
