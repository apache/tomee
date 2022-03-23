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
    "steadyPoolSize",
    "resizeQuantity",
    "maxPoolSize",
    "poolIdleTimeoutInSeconds",
    "maxWaitTimeInMillis"
})
public class BeanPool {
    @XmlElement(name = "steady-pool-size")
    protected String steadyPoolSize;
    @XmlElement(name = "resize-quantity")
    protected String resizeQuantity;
    @XmlElement(name = "max-pool-size")
    protected String maxPoolSize;
    @XmlElement(name = "pool-idle-timeout-in-seconds")
    protected String poolIdleTimeoutInSeconds;
    @XmlElement(name = "max-wait-time-in-millis")
    protected String maxWaitTimeInMillis;

    public String getSteadyPoolSize() {
        return steadyPoolSize;
    }

    public void setSteadyPoolSize(final String value) {
        this.steadyPoolSize = value;
    }

    public String getResizeQuantity() {
        return resizeQuantity;
    }

    public void setResizeQuantity(final String value) {
        this.resizeQuantity = value;
    }

    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final String value) {
        this.maxPoolSize = value;
    }

    public String getPoolIdleTimeoutInSeconds() {
        return poolIdleTimeoutInSeconds;
    }

    public void setPoolIdleTimeoutInSeconds(final String value) {
        this.poolIdleTimeoutInSeconds = value;
    }

    public String getMaxWaitTimeInMillis() {
        return maxWaitTimeInMillis;
    }

    public void setMaxWaitTimeInMillis(final String value) {
        this.maxWaitTimeInMillis = value;
    }
}
