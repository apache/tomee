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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config.typed;

import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.typed.util.Builders;
import org.apache.openejb.config.typed.util.DurationAdapter;
import org.apache.openejb.util.Duration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SingletonContainer")
public class SingletonContainerBuilder extends Container {

    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration accessTimeout = Duration.parse("30 seconds");

    public SingletonContainerBuilder() {
        setClassName("org.apache.openejb.core.singleton.SingletonContainer");
        setType("SINGLETON");
        setId("SingletonContainer");

        setConstructor("id, securityService");

    }

    public SingletonContainerBuilder id(final String id) {
        setId(id);
        return this;
    }

    public SingletonContainerBuilder withAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
        return this;
    }

    public void setAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public SingletonContainerBuilder withAccessTimeout(final long time, final TimeUnit unit) {
        return withAccessTimeout(new Duration(time, unit));
    }

    public void setAccessTimeout(final long time, final TimeUnit unit) {
        setAccessTimeout(new Duration(time, unit));
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
