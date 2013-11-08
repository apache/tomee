/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.kernel;

import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;

/**
 * Abstract {@link LockManager} implementation.
 *
 * @author Marc Prud'hommeaux
 */
public abstract class AbstractLockManager
    implements LockManager {

    /**
     * The context against which this LockManager is operating.
     */
    protected StoreContext ctx;

    /**
     * Runtime log to write locking messages to.
     */
    protected Log log;

    public void setContext(StoreContext ctx) {
        this.ctx = ctx;
        this.log = ctx.getConfiguration().getLog
            (OpenJPAConfiguration.LOG_RUNTIME);
    }

    public StoreContext getContext() {
        return ctx;
    }

    /**
     * Delegates to {@link LockManager#lock} with each element of the collection
     */
    public void lockAll(Collection sms, int level, int timeout,
        Object context) {
        for (Iterator<?> itr = sms.iterator(); itr.hasNext();)
            lock((OpenJPAStateManager) itr.next(), level, timeout, context);
    }

    /**
     * Does nothing by default.
     */
    public void beginTransaction() {
    }

    /**
     * Does nothing by default.
     */
    public void endTransaction() {
    }

    /**
     * Does nothing by default.
     */
    public void close () {
	}

    /**
     * Default not to skip relation field to maintain PessimisticLockManager semantics. 
     */
    public boolean skipRelationFieldLock() {
        return false;
    }
}

