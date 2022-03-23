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
package org.apache.openejb.jee.sun;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "remoteImpl",
    "localImpl",
    "remoteHomeImpl",
    "localHomeImpl"
})
public class GenClasses {
    @XmlElement(name = "remote-impl")
    protected String remoteImpl;
    @XmlElement(name = "local-impl")
    protected String localImpl;
    @XmlElement(name = "remote-home-impl")
    protected String remoteHomeImpl;
    @XmlElement(name = "local-home-impl")
    protected String localHomeImpl;

    public String getRemoteImpl() {
        return remoteImpl;
    }

    public void setRemoteImpl(final String value) {
        this.remoteImpl = value;
    }

    public String getLocalImpl() {
        return localImpl;
    }

    public void setLocalImpl(final String value) {
        this.localImpl = value;
    }

    public String getRemoteHomeImpl() {
        return remoteHomeImpl;
    }

    public void setRemoteHomeImpl(final String value) {
        this.remoteHomeImpl = value;
    }

    public String getLocalHomeImpl() {
        return localHomeImpl;
    }

    public void setLocalHomeImpl(final String value) {
        this.localHomeImpl = value;
    }
}
