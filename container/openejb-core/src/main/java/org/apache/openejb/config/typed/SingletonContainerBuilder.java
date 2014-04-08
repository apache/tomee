/*
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
package org.apache.openejb.config.typed;

import org.apache.openejb.config.typed.util.*;
import org.apache.openejb.config.sys.*;
import javax.xml.bind.annotation.*;
import org.apache.openejb.util.Duration;
import java.util.*;
import java.util.concurrent.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SingletonContainer")
public class SingletonContainerBuilder extends Container {

    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration accessTimeout = org.apache.openejb.util.Duration.parse("30 seconds");

    public SingletonContainerBuilder() {
        setClassName("org.apache.openejb.core.singleton.SingletonContainer");
        setType("SINGLETON");
        setId("SingletonContainer");

        setConstructor("id, securityService");

    }

    public SingletonContainerBuilder id(String id) {
        setId(id);
        return this;
    }

    public SingletonContainerBuilder withAccessTimeout(org.apache.openejb.util.Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
        return this;
    }

    public void setAccessTimeout(org.apache.openejb.util.Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public org.apache.openejb.util.Duration getAccessTimeout() {
        return accessTimeout;
    }

    public SingletonContainerBuilder withAccessTimeout(long time, TimeUnit unit) {
        return withAccessTimeout(new Duration(time, unit));
    }

    public void setAccessTimeout(long time, TimeUnit unit) {
        setAccessTimeout(new Duration(time, unit));
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
