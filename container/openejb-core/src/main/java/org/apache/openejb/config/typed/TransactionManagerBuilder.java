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

import org.apache.openejb.config.sys.TransactionManager;
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
@XmlRootElement(name = "TransactionManager")
public class TransactionManagerBuilder extends TransactionManager {

    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration defaultTransactionTimeout = Duration.parse("10 minutes");
    @XmlAttribute
    private boolean txRecovery;
    @XmlAttribute
    private int bufferSizeKb = 32;
    @XmlAttribute
    private boolean checksumEnabled = true;
    @XmlAttribute
    private boolean adler32Checksum = true;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private Duration flushSleepTime = Duration.parse("50 Milliseconds");
    @XmlAttribute
    private String logFileDir = "txlog";
    @XmlAttribute
    private String logFileExt = "log";
    @XmlAttribute
    private String logFileName = "howl";
    @XmlAttribute
    private int maxBlocksPerFile = -1;
    @XmlAttribute
    private int maxBuffers;
    @XmlAttribute
    private int maxLogFiles = 2;
    @XmlAttribute
    private int minBuffers = 4;
    @XmlAttribute
    private int threadsWaitingForceThreshold = -1;

    public TransactionManagerBuilder() {
        setClassName("org.apache.openejb.resource.GeronimoTransactionManagerFactory");
        setType("TransactionManager");
        setId("TransactionManager");

        setConstructor("defaultTransactionTimeoutSeconds, defaultTransactionTimeout, txRecovery, tmId, bufferClassName, bufferSizeKb, checksumEnabled, adler32Checksum, flushSleepTimeMilliseconds, flushSleepTime, logFileDir, logFileExt, logFileName, maxBlocksPerFile, maxBuffers, maxLogFiles, minBuffers, threadsWaitingForceThreshold");

        setFactoryName("create");

    }

    public TransactionManagerBuilder id(final String id) {
        setId(id);
        return this;
    }

    public TransactionManagerBuilder withDefaultTransactionTimeout(final Duration defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
        return this;
    }

    public void setDefaultTransactionTimeout(final Duration defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
    }

    public Duration getDefaultTransactionTimeout() {
        return defaultTransactionTimeout;
    }

    public TransactionManagerBuilder withDefaultTransactionTimeout(final long time, final TimeUnit unit) {
        return withDefaultTransactionTimeout(new Duration(time, unit));
    }

    public void setDefaultTransactionTimeout(final long time, final TimeUnit unit) {
        setDefaultTransactionTimeout(new Duration(time, unit));
    }

    public TransactionManagerBuilder withTxRecovery(final boolean txRecovery) {
        this.txRecovery = txRecovery;
        return this;
    }

    public void setTxRecovery(final boolean txRecovery) {
        this.txRecovery = txRecovery;
    }

    public boolean getTxRecovery() {
        return txRecovery;
    }

    public TransactionManagerBuilder withBufferSizeKb(final int bufferSizeKb) {
        this.bufferSizeKb = bufferSizeKb;
        return this;
    }

    public void setBufferSizeKb(final int bufferSizeKb) {
        this.bufferSizeKb = bufferSizeKb;
    }

    public int getBufferSizeKb() {
        return bufferSizeKb;
    }

    public TransactionManagerBuilder withChecksumEnabled(final boolean checksumEnabled) {
        this.checksumEnabled = checksumEnabled;
        return this;
    }

    public void setChecksumEnabled(final boolean checksumEnabled) {
        this.checksumEnabled = checksumEnabled;
    }

    public boolean getChecksumEnabled() {
        return checksumEnabled;
    }

    public TransactionManagerBuilder withAdler32Checksum(final boolean adler32Checksum) {
        this.adler32Checksum = adler32Checksum;
        return this;
    }

    public void setAdler32Checksum(final boolean adler32Checksum) {
        this.adler32Checksum = adler32Checksum;
    }

    public boolean getAdler32Checksum() {
        return adler32Checksum;
    }

    public TransactionManagerBuilder withFlushSleepTime(final Duration flushSleepTime) {
        this.flushSleepTime = flushSleepTime;
        return this;
    }

    public void setFlushSleepTime(final Duration flushSleepTime) {
        this.flushSleepTime = flushSleepTime;
    }

    public Duration getFlushSleepTime() {
        return flushSleepTime;
    }

    public TransactionManagerBuilder withFlushSleepTime(final long time, final TimeUnit unit) {
        return withFlushSleepTime(new Duration(time, unit));
    }

    public void setFlushSleepTime(final long time, final TimeUnit unit) {
        setFlushSleepTime(new Duration(time, unit));
    }

    public TransactionManagerBuilder withLogFileDir(final String logFileDir) {
        this.logFileDir = logFileDir;
        return this;
    }

    public void setLogFileDir(final String logFileDir) {
        this.logFileDir = logFileDir;
    }

    public String getLogFileDir() {
        return logFileDir;
    }

    public TransactionManagerBuilder withLogFileExt(final String logFileExt) {
        this.logFileExt = logFileExt;
        return this;
    }

    public void setLogFileExt(final String logFileExt) {
        this.logFileExt = logFileExt;
    }

    public String getLogFileExt() {
        return logFileExt;
    }

    public TransactionManagerBuilder withLogFileName(final String logFileName) {
        this.logFileName = logFileName;
        return this;
    }

    public void setLogFileName(final String logFileName) {
        this.logFileName = logFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public TransactionManagerBuilder withMaxBlocksPerFile(final int maxBlocksPerFile) {
        this.maxBlocksPerFile = maxBlocksPerFile;
        return this;
    }

    public void setMaxBlocksPerFile(final int maxBlocksPerFile) {
        this.maxBlocksPerFile = maxBlocksPerFile;
    }

    public int getMaxBlocksPerFile() {
        return maxBlocksPerFile;
    }

    public TransactionManagerBuilder withMaxBuffers(final int maxBuffers) {
        this.maxBuffers = maxBuffers;
        return this;
    }

    public void setMaxBuffers(final int maxBuffers) {
        this.maxBuffers = maxBuffers;
    }

    public int getMaxBuffers() {
        return maxBuffers;
    }

    public TransactionManagerBuilder withMaxLogFiles(final int maxLogFiles) {
        this.maxLogFiles = maxLogFiles;
        return this;
    }

    public void setMaxLogFiles(final int maxLogFiles) {
        this.maxLogFiles = maxLogFiles;
    }

    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    public TransactionManagerBuilder withMinBuffers(final int minBuffers) {
        this.minBuffers = minBuffers;
        return this;
    }

    public void setMinBuffers(final int minBuffers) {
        this.minBuffers = minBuffers;
    }

    public int getMinBuffers() {
        return minBuffers;
    }

    public TransactionManagerBuilder withThreadsWaitingForceThreshold(final int threadsWaitingForceThreshold) {
        this.threadsWaitingForceThreshold = threadsWaitingForceThreshold;
        return this;
    }

    public void setThreadsWaitingForceThreshold(final int threadsWaitingForceThreshold) {
        this.threadsWaitingForceThreshold = threadsWaitingForceThreshold;
    }

    public int getThreadsWaitingForceThreshold() {
        return threadsWaitingForceThreshold;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
