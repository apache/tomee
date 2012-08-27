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

import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.typed.util.Builders;
import org.apache.openejb.util.Duration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@XmlRootElement(name = "StatelessContainer")
public class StatelessContainerBuilder extends Container {

    private org.apache.openejb.util.Duration accessTimeout = org.apache.openejb.util.Duration.parse("30 seconds");
    private int maxSize = 10;
    private int minSize = 0;
    private boolean strictPooling = true;
    private org.apache.openejb.util.Duration maxAge = org.apache.openejb.util.Duration.parse("0 hours");
    private boolean replaceAged = true;
    private boolean replaceFlushed = false;
    private int maxAgeOffset = -1;
    private org.apache.openejb.util.Duration idleTimeout = org.apache.openejb.util.Duration.parse("0 minutes");
    private boolean garbageCollection = false;
    private org.apache.openejb.util.Duration sweepInterval = org.apache.openejb.util.Duration.parse("5 minutes");
    private int callbackThreads = 5;
    private org.apache.openejb.util.Duration closeTimeout = org.apache.openejb.util.Duration.parse("5 minutes");

    public StatelessContainerBuilder() {
        setClassName("org.apache.openejb.core.stateless.StatelessContainerFactory");
        setType("STATELESS");
        setId("StatelessContainer");

        setFactoryName("create");

    }

    public StatelessContainerBuilder id(String id) {
        setId(id);
        return this;
    }

    public StatelessContainerBuilder withAccessTimeout(org.apache.openejb.util.Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
        return this;
    }

    public void setAccessTimeout(org.apache.openejb.util.Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    @XmlAttribute
    public org.apache.openejb.util.Duration getAccessTimeout() {
        return accessTimeout;
    }

    public StatelessContainerBuilder withAccessTimeout(long time, TimeUnit unit) {
        return withAccessTimeout(new Duration(time, unit));
    }

    public void setAccessTimeout(long time, TimeUnit unit) {
        setAccessTimeout(new Duration(time, unit));
    }

    public StatelessContainerBuilder withMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @XmlAttribute
    public int getMaxSize() {
        return maxSize;
    }

    public StatelessContainerBuilder withMinSize(int minSize) {
        this.minSize = minSize;
        return this;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    @XmlAttribute
    public int getMinSize() {
        return minSize;
    }

    public StatelessContainerBuilder withStrictPooling(boolean strictPooling) {
        this.strictPooling = strictPooling;
        return this;
    }

    public void setStrictPooling(boolean strictPooling) {
        this.strictPooling = strictPooling;
    }

    @XmlAttribute
    public boolean getStrictPooling() {
        return strictPooling;
    }

    public StatelessContainerBuilder withMaxAge(org.apache.openejb.util.Duration maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public void setMaxAge(org.apache.openejb.util.Duration maxAge) {
        this.maxAge = maxAge;
    }

    @XmlAttribute
    public org.apache.openejb.util.Duration getMaxAge() {
        return maxAge;
    }

    public StatelessContainerBuilder withMaxAge(long time, TimeUnit unit) {
        return withMaxAge(new Duration(time, unit));
    }

    public void setMaxAge(long time, TimeUnit unit) {
        setMaxAge(new Duration(time, unit));
    }

    public StatelessContainerBuilder withReplaceAged(boolean replaceAged) {
        this.replaceAged = replaceAged;
        return this;
    }

    public void setReplaceAged(boolean replaceAged) {
        this.replaceAged = replaceAged;
    }

    @XmlAttribute
    public boolean getReplaceAged() {
        return replaceAged;
    }

    public StatelessContainerBuilder withReplaceFlushed(boolean replaceFlushed) {
        this.replaceFlushed = replaceFlushed;
        return this;
    }

    public void setReplaceFlushed(boolean replaceFlushed) {
        this.replaceFlushed = replaceFlushed;
    }

    @XmlAttribute
    public boolean getReplaceFlushed() {
        return replaceFlushed;
    }

    public StatelessContainerBuilder withMaxAgeOffset(int maxAgeOffset) {
        this.maxAgeOffset = maxAgeOffset;
        return this;
    }

    public void setMaxAgeOffset(int maxAgeOffset) {
        this.maxAgeOffset = maxAgeOffset;
    }

    @XmlAttribute
    public int getMaxAgeOffset() {
        return maxAgeOffset;
    }

    public StatelessContainerBuilder withIdleTimeout(org.apache.openejb.util.Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public void setIdleTimeout(org.apache.openejb.util.Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @XmlAttribute
    public org.apache.openejb.util.Duration getIdleTimeout() {
        return idleTimeout;
    }

    public StatelessContainerBuilder withIdleTimeout(long time, TimeUnit unit) {
        return withIdleTimeout(new Duration(time, unit));
    }

    public void setIdleTimeout(long time, TimeUnit unit) {
        setIdleTimeout(new Duration(time, unit));
    }

    public StatelessContainerBuilder withGarbageCollection(boolean garbageCollection) {
        this.garbageCollection = garbageCollection;
        return this;
    }

    public void setGarbageCollection(boolean garbageCollection) {
        this.garbageCollection = garbageCollection;
    }

    @XmlAttribute
    public boolean getGarbageCollection() {
        return garbageCollection;
    }

    public StatelessContainerBuilder withSweepInterval(org.apache.openejb.util.Duration sweepInterval) {
        this.sweepInterval = sweepInterval;
        return this;
    }

    public void setSweepInterval(org.apache.openejb.util.Duration sweepInterval) {
        this.sweepInterval = sweepInterval;
    }

    @XmlAttribute
    public org.apache.openejb.util.Duration getSweepInterval() {
        return sweepInterval;
    }

    public StatelessContainerBuilder withSweepInterval(long time, TimeUnit unit) {
        return withSweepInterval(new Duration(time, unit));
    }

    public void setSweepInterval(long time, TimeUnit unit) {
        setSweepInterval(new Duration(time, unit));
    }

    public StatelessContainerBuilder withCallbackThreads(int callbackThreads) {
        this.callbackThreads = callbackThreads;
        return this;
    }

    public void setCallbackThreads(int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    @XmlAttribute
    public int getCallbackThreads() {
        return callbackThreads;
    }

    public StatelessContainerBuilder withCloseTimeout(org.apache.openejb.util.Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public void setCloseTimeout(org.apache.openejb.util.Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    @XmlAttribute
    public org.apache.openejb.util.Duration getCloseTimeout() {
        return closeTimeout;
    }

    public StatelessContainerBuilder withCloseTimeout(long time, TimeUnit unit) {
        return withCloseTimeout(new Duration(time, unit));
    }

    public void setCloseTimeout(long time, TimeUnit unit) {
        setCloseTimeout(new Duration(time, unit));
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
