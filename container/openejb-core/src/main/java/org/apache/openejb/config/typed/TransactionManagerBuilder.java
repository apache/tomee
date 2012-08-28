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
@XmlRootElement(name = "TransactionManager")
public class TransactionManagerBuilder extends TransactionManager {

    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration defaultTransactionTimeout = org.apache.openejb.util.Duration.parse("10 minutes");
    @XmlAttribute
    private boolean txRecovery = false;
    @XmlAttribute
    private int bufferSizeKb = 32;
    @XmlAttribute
    private boolean checksumEnabled = true;
    @XmlAttribute
    private boolean adler32Checksum = true;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration flushSleepTime = org.apache.openejb.util.Duration.parse("50 Milliseconds");
    @XmlAttribute
    private String logFileDir = "txlog";
    @XmlAttribute
    private String logFileExt = "log";
    @XmlAttribute
    private String logFileName = "howl";
    @XmlAttribute
    private int maxBlocksPerFile = -1;
    @XmlAttribute
    private int maxBuffers = 0;
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

    public TransactionManagerBuilder id(String id) {
        setId(id);
        return this;
    }

    public TransactionManagerBuilder withDefaultTransactionTimeout(org.apache.openejb.util.Duration defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
        return this;
    }

    public void setDefaultTransactionTimeout(org.apache.openejb.util.Duration defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
    }

    public org.apache.openejb.util.Duration getDefaultTransactionTimeout() {
        return defaultTransactionTimeout;
    }

    public TransactionManagerBuilder withDefaultTransactionTimeout(long time, TimeUnit unit) {
        return withDefaultTransactionTimeout(new Duration(time, unit));
    }

    public void setDefaultTransactionTimeout(long time, TimeUnit unit) {
        setDefaultTransactionTimeout(new Duration(time, unit));
    }

    public TransactionManagerBuilder withTxRecovery(boolean txRecovery) {
        this.txRecovery = txRecovery;
        return this;
    }

    public void setTxRecovery(boolean txRecovery) {
        this.txRecovery = txRecovery;
    }

    public boolean getTxRecovery() {
        return txRecovery;
    }

    public TransactionManagerBuilder withBufferSizeKb(int bufferSizeKb) {
        this.bufferSizeKb = bufferSizeKb;
        return this;
    }

    public void setBufferSizeKb(int bufferSizeKb) {
        this.bufferSizeKb = bufferSizeKb;
    }

    public int getBufferSizeKb() {
        return bufferSizeKb;
    }

    public TransactionManagerBuilder withChecksumEnabled(boolean checksumEnabled) {
        this.checksumEnabled = checksumEnabled;
        return this;
    }

    public void setChecksumEnabled(boolean checksumEnabled) {
        this.checksumEnabled = checksumEnabled;
    }

    public boolean getChecksumEnabled() {
        return checksumEnabled;
    }

    public TransactionManagerBuilder withAdler32Checksum(boolean adler32Checksum) {
        this.adler32Checksum = adler32Checksum;
        return this;
    }

    public void setAdler32Checksum(boolean adler32Checksum) {
        this.adler32Checksum = adler32Checksum;
    }

    public boolean getAdler32Checksum() {
        return adler32Checksum;
    }

    public TransactionManagerBuilder withFlushSleepTime(org.apache.openejb.util.Duration flushSleepTime) {
        this.flushSleepTime = flushSleepTime;
        return this;
    }

    public void setFlushSleepTime(org.apache.openejb.util.Duration flushSleepTime) {
        this.flushSleepTime = flushSleepTime;
    }

    public org.apache.openejb.util.Duration getFlushSleepTime() {
        return flushSleepTime;
    }

    public TransactionManagerBuilder withFlushSleepTime(long time, TimeUnit unit) {
        return withFlushSleepTime(new Duration(time, unit));
    }

    public void setFlushSleepTime(long time, TimeUnit unit) {
        setFlushSleepTime(new Duration(time, unit));
    }

    public TransactionManagerBuilder withLogFileDir(String logFileDir) {
        this.logFileDir = logFileDir;
        return this;
    }

    public void setLogFileDir(String logFileDir) {
        this.logFileDir = logFileDir;
    }

    public String getLogFileDir() {
        return logFileDir;
    }

    public TransactionManagerBuilder withLogFileExt(String logFileExt) {
        this.logFileExt = logFileExt;
        return this;
    }

    public void setLogFileExt(String logFileExt) {
        this.logFileExt = logFileExt;
    }

    public String getLogFileExt() {
        return logFileExt;
    }

    public TransactionManagerBuilder withLogFileName(String logFileName) {
        this.logFileName = logFileName;
        return this;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public TransactionManagerBuilder withMaxBlocksPerFile(int maxBlocksPerFile) {
        this.maxBlocksPerFile = maxBlocksPerFile;
        return this;
    }

    public void setMaxBlocksPerFile(int maxBlocksPerFile) {
        this.maxBlocksPerFile = maxBlocksPerFile;
    }

    public int getMaxBlocksPerFile() {
        return maxBlocksPerFile;
    }

    public TransactionManagerBuilder withMaxBuffers(int maxBuffers) {
        this.maxBuffers = maxBuffers;
        return this;
    }

    public void setMaxBuffers(int maxBuffers) {
        this.maxBuffers = maxBuffers;
    }

    public int getMaxBuffers() {
        return maxBuffers;
    }

    public TransactionManagerBuilder withMaxLogFiles(int maxLogFiles) {
        this.maxLogFiles = maxLogFiles;
        return this;
    }

    public void setMaxLogFiles(int maxLogFiles) {
        this.maxLogFiles = maxLogFiles;
    }

    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    public TransactionManagerBuilder withMinBuffers(int minBuffers) {
        this.minBuffers = minBuffers;
        return this;
    }

    public void setMinBuffers(int minBuffers) {
        this.minBuffers = minBuffers;
    }

    public int getMinBuffers() {
        return minBuffers;
    }

    public TransactionManagerBuilder withThreadsWaitingForceThreshold(int threadsWaitingForceThreshold) {
        this.threadsWaitingForceThreshold = threadsWaitingForceThreshold;
        return this;
    }

    public void setThreadsWaitingForceThreshold(int threadsWaitingForceThreshold) {
        this.threadsWaitingForceThreshold = threadsWaitingForceThreshold;
    }

    public int getThreadsWaitingForceThreshold() {
        return threadsWaitingForceThreshold;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
