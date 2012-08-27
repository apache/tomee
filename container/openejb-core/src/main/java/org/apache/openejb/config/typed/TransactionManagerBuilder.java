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

import org.apache.openejb.config.sys.TransactionManager;
import org.apache.openejb.config.typed.util.Builders;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@XmlRootElement(name = "TransactionManager")
public class TransactionManagerBuilder extends TransactionManager {

    private long defaultTransactionTimeoutSeconds = 600;
    private boolean txRecovery = false;
    private int bufferSizeKb = 32;
    private boolean checksumEnabled = true;
    private boolean adler32Checksum = true;
    private long flushSleepTimeMilliseconds = 50;
    private String logFileDir = "txlog";
    private String logFileExt = "log";
    private String logFileName = "howl";
    private int maxBlocksPerFile = -1;
    private int maxBuffers = 0;
    private int maxLogFiles = 2;
    private int minBuffers = 4;
    private int threadsWaitingForceThreshold = -1;

    public TransactionManagerBuilder() {
        setClassName("org.apache.openejb.resource.GeronimoTransactionManagerFactory");
        setType("TransactionManager");
        setId("TransactionManager");

        setConstructor("defaultTransactionTimeoutSeconds, TxRecovery, tmId, bufferClassName, bufferSizeKb, checksumEnabled, adler32Checksum, flushSleepTimeMilliseconds, logFileDir, logFileExt, logFileName, maxBlocksPerFile, maxBuffers, maxLogFiles, minBuffers, threadsWaitingForceThreshold");

        setFactoryName("create");

    }

    public TransactionManagerBuilder id(String id) {
        setId(id);
        return this;
    }

    public TransactionManagerBuilder withDefaultTransactionTimeoutSeconds(long defaultTransactionTimeoutSeconds) {
        this.defaultTransactionTimeoutSeconds = defaultTransactionTimeoutSeconds;
        return this;
    }

    public void setDefaultTransactionTimeoutSeconds(long defaultTransactionTimeoutSeconds) {
        this.defaultTransactionTimeoutSeconds = defaultTransactionTimeoutSeconds;
    }

    @XmlAttribute
    public long getDefaultTransactionTimeoutSeconds() {
        return defaultTransactionTimeoutSeconds;
    }

    public TransactionManagerBuilder withDefaultTransactionTimeout(long time, TimeUnit unit) {
        return withDefaultTransactionTimeoutSeconds(TimeUnit.SECONDS.convert(time, unit));
    }

    public void setDefaultTransactionTimeout(long time, TimeUnit unit) {
        setDefaultTransactionTimeoutSeconds(TimeUnit.SECONDS.convert(time, unit));
    }

    public TransactionManagerBuilder withTxRecovery(boolean txRecovery) {
        this.txRecovery = txRecovery;
        return this;
    }

    public void setTxRecovery(boolean txRecovery) {
        this.txRecovery = txRecovery;
    }

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
    public boolean getAdler32Checksum() {
        return adler32Checksum;
    }

    public TransactionManagerBuilder withFlushSleepTimeMilliseconds(long flushSleepTimeMilliseconds) {
        this.flushSleepTimeMilliseconds = flushSleepTimeMilliseconds;
        return this;
    }

    public void setFlushSleepTimeMilliseconds(long flushSleepTimeMilliseconds) {
        this.flushSleepTimeMilliseconds = flushSleepTimeMilliseconds;
    }

    @XmlAttribute
    public long getFlushSleepTimeMilliseconds() {
        return flushSleepTimeMilliseconds;
    }

    public TransactionManagerBuilder withFlushSleepTime(long time, TimeUnit unit) {
        return withFlushSleepTimeMilliseconds(TimeUnit.MILLISECONDS.convert(time, unit));
    }

    public void setFlushSleepTime(long time, TimeUnit unit) {
        setFlushSleepTimeMilliseconds(TimeUnit.MILLISECONDS.convert(time, unit));
    }

    public TransactionManagerBuilder withLogFileDir(String logFileDir) {
        this.logFileDir = logFileDir;
        return this;
    }

    public void setLogFileDir(String logFileDir) {
        this.logFileDir = logFileDir;
    }

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
    public int getThreadsWaitingForceThreshold() {
        return threadsWaitingForceThreshold;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
