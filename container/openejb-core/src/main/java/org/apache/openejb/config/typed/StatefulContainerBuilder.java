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
@XmlRootElement(name = "StatefulContainer")
public class StatefulContainerBuilder extends Container {

    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration accessTimeout = org.apache.openejb.util.Duration.parse("30 seconds");
    @XmlAttribute
    private String cache = "org.apache.openejb.core.stateful.SimpleCache";
    @XmlAttribute
    private String passivator = "org.apache.openejb.core.stateful.SimplePassivater";
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration timeOut = org.apache.openejb.util.Duration.parse("20");
    @XmlAttribute
    private int frequency = 60;
    @XmlAttribute
    private int capacity = 1000;
    @XmlAttribute
    private int bulkPassivate = 100;

    public StatefulContainerBuilder() {
        setClassName("org.apache.openejb.core.stateful.StatefulContainerFactory");
        setType("STATEFUL");
        setId("StatefulContainer");

        setFactoryName("create");

    }

    public StatefulContainerBuilder id(String id) {
        setId(id);
        return this;
    }

    public StatefulContainerBuilder withAccessTimeout(org.apache.openejb.util.Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
        return this;
    }

    public void setAccessTimeout(org.apache.openejb.util.Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public org.apache.openejb.util.Duration getAccessTimeout() {
        return accessTimeout;
    }

    public StatefulContainerBuilder withAccessTimeout(long time, TimeUnit unit) {
        return withAccessTimeout(new Duration(time, unit));
    }

    public void setAccessTimeout(long time, TimeUnit unit) {
        setAccessTimeout(new Duration(time, unit));
    }

    public StatefulContainerBuilder withCache(String cache) {
        this.cache = cache;
        return this;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getCache() {
        return cache;
    }

    public StatefulContainerBuilder withPassivator(String passivator) {
        this.passivator = passivator;
        return this;
    }

    public void setPassivator(String passivator) {
        this.passivator = passivator;
    }

    public String getPassivator() {
        return passivator;
    }

    public StatefulContainerBuilder withTimeOut(org.apache.openejb.util.Duration timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public void setTimeOut(org.apache.openejb.util.Duration timeOut) {
        this.timeOut = timeOut;
    }

    public org.apache.openejb.util.Duration getTimeOut() {
        return timeOut;
    }

    public StatefulContainerBuilder withTimeOut(long time, TimeUnit unit) {
        return withTimeOut(new Duration(time, unit));
    }

    public void setTimeOut(long time, TimeUnit unit) {
        setTimeOut(new Duration(time, unit));
    }

    public StatefulContainerBuilder withFrequency(int frequency) {
        this.frequency = frequency;
        return this;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public StatefulContainerBuilder withCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public StatefulContainerBuilder withBulkPassivate(int bulkPassivate) {
        this.bulkPassivate = bulkPassivate;
        return this;
    }

    public void setBulkPassivate(int bulkPassivate) {
        this.bulkPassivate = bulkPassivate;
    }

    public int getBulkPassivate() {
        return bulkPassivate;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
