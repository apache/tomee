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
package org.apache.openejb.core.transaction;

import java.util.Timer;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.geronimo.connector.work.GeronimoWorkManager;

public class SimpleBootstrapContext implements BootstrapContext {
    private final WorkManager workManager;
    private final XATerminator xaTerminator;

    public SimpleBootstrapContext(WorkManager workManager) {
        this.workManager = workManager;
        xaTerminator = null;
    }

    public SimpleBootstrapContext(WorkManager workManager, XATerminator xaTerminator) {
        this.workManager = workManager;
        this.xaTerminator = xaTerminator;
    }

    public WorkManager getWorkManager() {
        return workManager;
    }

    public XATerminator getXATerminator() {
        return xaTerminator;
    }

    public Timer createTimer() {
        return new Timer(true);
    }

    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        // for Geronimo transaction manager, it implements XATerminator, TransactionManager & TransactionSynchronizationRegistry
        if (this.xaTerminator != null && this.xaTerminator instanceof TransactionSynchronizationRegistry) {
            return (TransactionSynchronizationRegistry)this.xaTerminator;
        }
        return null;
    }

    public boolean isContextSupported(Class<? extends WorkContext> cls) {
        if (workManager instanceof GeronimoWorkManager) {
        	GeronimoWorkManager geronimoWorkManager = (GeronimoWorkManager) workManager;
        	return geronimoWorkManager.isContextSupported(cls);
        }
        
        return false;
    }
}
