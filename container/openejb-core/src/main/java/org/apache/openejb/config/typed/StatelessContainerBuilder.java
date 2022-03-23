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
@XmlRootElement(name = "StatelessContainer")
public class StatelessContainerBuilder extends Container {

    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration accessTimeout = Duration.parse("30 seconds");
    @XmlAttribute
    private int maxSize = 10;
    @XmlAttribute
    private int minSize;
    @XmlAttribute
    private boolean strictPooling = true;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration maxAge = Duration.parse("0 hours");
    @XmlAttribute
    private boolean replaceAged = true;
    @XmlAttribute
    private boolean replaceFlushed;
    @XmlAttribute
    private int maxAgeOffset = -1;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration idleTimeout = Duration.parse("0 minutes");
    @XmlAttribute
    private boolean garbageCollection;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration sweepInterval = Duration.parse("5 minutes");
    @XmlAttribute
    private int callbackThreads = 5;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration closeTimeout = Duration.parse("5 minutes");

    public StatelessContainerBuilder() {
        setClassName("org.apache.openejb.core.stateless.StatelessContainerFactory");
        setType("STATELESS");
        setId("StatelessContainer");

        setFactoryName("create");

    }

    public StatelessContainerBuilder id(final String id) {
        setId(id);
        return this;
    }

    public StatelessContainerBuilder withAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
        return this;
    }

    public void setAccessTimeout(final Duration accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public Duration getAccessTimeout() {
        return accessTimeout;
    }

    public StatelessContainerBuilder withAccessTimeout(final long time, final TimeUnit unit) {
        return withAccessTimeout(new Duration(time, unit));
    }

    public void setAccessTimeout(final long time, final TimeUnit unit) {
        setAccessTimeout(new Duration(time, unit));
    }

    public StatelessContainerBuilder withMaxSize(final int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public StatelessContainerBuilder withMinSize(final int minSize) {
        this.minSize = minSize;
        return this;
    }

    public void setMinSize(final int minSize) {
        this.minSize = minSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public StatelessContainerBuilder withStrictPooling(final boolean strictPooling) {
        this.strictPooling = strictPooling;
        return this;
    }

    public void setStrictPooling(final boolean strictPooling) {
        this.strictPooling = strictPooling;
    }

    public boolean getStrictPooling() {
        return strictPooling;
    }

    public StatelessContainerBuilder withMaxAge(final Duration maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public void setMaxAge(final Duration maxAge) {
        this.maxAge = maxAge;
    }

    public Duration getMaxAge() {
        return maxAge;
    }

    public StatelessContainerBuilder withMaxAge(final long time, final TimeUnit unit) {
        return withMaxAge(new Duration(time, unit));
    }

    public void setMaxAge(final long time, final TimeUnit unit) {
        setMaxAge(new Duration(time, unit));
    }

    public StatelessContainerBuilder withReplaceAged(final boolean replaceAged) {
        this.replaceAged = replaceAged;
        return this;
    }

    public void setReplaceAged(final boolean replaceAged) {
        this.replaceAged = replaceAged;
    }

    public boolean getReplaceAged() {
        return replaceAged;
    }

    public StatelessContainerBuilder withReplaceFlushed(final boolean replaceFlushed) {
        this.replaceFlushed = replaceFlushed;
        return this;
    }

    public void setReplaceFlushed(final boolean replaceFlushed) {
        this.replaceFlushed = replaceFlushed;
    }

    public boolean getReplaceFlushed() {
        return replaceFlushed;
    }

    public StatelessContainerBuilder withMaxAgeOffset(final int maxAgeOffset) {
        this.maxAgeOffset = maxAgeOffset;
        return this;
    }

    public void setMaxAgeOffset(final int maxAgeOffset) {
        this.maxAgeOffset = maxAgeOffset;
    }

    public int getMaxAgeOffset() {
        return maxAgeOffset;
    }

    public StatelessContainerBuilder withIdleTimeout(final Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public void setIdleTimeout(final Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    public StatelessContainerBuilder withIdleTimeout(final long time, final TimeUnit unit) {
        return withIdleTimeout(new Duration(time, unit));
    }

    public void setIdleTimeout(final long time, final TimeUnit unit) {
        setIdleTimeout(new Duration(time, unit));
    }

    public StatelessContainerBuilder withGarbageCollection(final boolean garbageCollection) {
        this.garbageCollection = garbageCollection;
        return this;
    }

    public void setGarbageCollection(final boolean garbageCollection) {
        this.garbageCollection = garbageCollection;
    }

    public boolean getGarbageCollection() {
        return garbageCollection;
    }

    public StatelessContainerBuilder withSweepInterval(final Duration sweepInterval) {
        this.sweepInterval = sweepInterval;
        return this;
    }

    public void setSweepInterval(final Duration sweepInterval) {
        this.sweepInterval = sweepInterval;
    }

    public Duration getSweepInterval() {
        return sweepInterval;
    }

    public StatelessContainerBuilder withSweepInterval(final long time, final TimeUnit unit) {
        return withSweepInterval(new Duration(time, unit));
    }

    public void setSweepInterval(final long time, final TimeUnit unit) {
        setSweepInterval(new Duration(time, unit));
    }

    public StatelessContainerBuilder withCallbackThreads(final int callbackThreads) {
        this.callbackThreads = callbackThreads;
        return this;
    }

    public void setCallbackThreads(final int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    public int getCallbackThreads() {
        return callbackThreads;
    }

    public StatelessContainerBuilder withCloseTimeout(final Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public void setCloseTimeout(final Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public Duration getCloseTimeout() {
        return closeTimeout;
    }

    public StatelessContainerBuilder withCloseTimeout(final long time, final TimeUnit unit) {
        return withCloseTimeout(new Duration(time, unit));
    }

    public void setCloseTimeout(final long time, final TimeUnit unit) {
        setCloseTimeout(new Duration(time, unit));
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
