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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "maxCacheSize",
    "resizeQuantity",
    "isCacheOverflowAllowed",
    "cacheIdleTimeoutInSeconds",
    "removalTimeoutInSeconds",
    "victimSelectionPolicy"
})
public class BeanCache {
    @XmlElement(name = "max-cache-size")
    protected String maxCacheSize;
    @XmlElement(name = "resize-quantity")
    protected String resizeQuantity;
    @XmlElement(name = "is-cache-overflow-allowed")
    protected String isCacheOverflowAllowed;
    @XmlElement(name = "cache-idle-timeout-in-seconds")
    protected String cacheIdleTimeoutInSeconds;
    @XmlElement(name = "removal-timeout-in-seconds")
    protected String removalTimeoutInSeconds;
    @XmlElement(name = "victim-selection-policy")
    protected String victimSelectionPolicy;

    public String getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(String value) {
        this.maxCacheSize = value;
    }

    public String getResizeQuantity() {
        return resizeQuantity;
    }

    public void setResizeQuantity(String value) {
        this.resizeQuantity = value;
    }

    public String getIsCacheOverflowAllowed() {
        return isCacheOverflowAllowed;
    }

    public void setIsCacheOverflowAllowed(String value) {
        this.isCacheOverflowAllowed = value;
    }

    public String getCacheIdleTimeoutInSeconds() {
        return cacheIdleTimeoutInSeconds;
    }

    public void setCacheIdleTimeoutInSeconds(String value) {
        this.cacheIdleTimeoutInSeconds = value;
    }

    public String getRemovalTimeoutInSeconds() {
        return removalTimeoutInSeconds;
    }

    public void setRemovalTimeoutInSeconds(String value) {
        this.removalTimeoutInSeconds = value;
    }

    public String getVictimSelectionPolicy() {
        return victimSelectionPolicy;
    }

    public void setVictimSelectionPolicy(String value) {
        this.victimSelectionPolicy = value;
    }
}
