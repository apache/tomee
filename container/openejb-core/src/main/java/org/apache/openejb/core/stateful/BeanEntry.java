/**
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
package org.apache.openejb.core.stateful;

import javax.transaction.Transaction;

public class BeanEntry implements java.io.Serializable {
    protected final Object bean;
    protected Object primaryKey;
    protected Object ancillaryState;
    protected transient Transaction transaction;
    protected long timeStamp;
    protected long timeOutInterval;
    protected boolean inQue = false;

    protected BeanEntry(Object beanInstance, Object primKey, Object ancillary, long timeOut) {
        bean = beanInstance;
        primaryKey = primKey;
        ancillaryState = ancillary;
        transaction = null;
        timeStamp = System.currentTimeMillis();
        timeOutInterval = timeOut;
    }

    protected boolean isTimedOut() {
        if (timeOutInterval == 0)
            return false;
        long now = System.currentTimeMillis();
        return (now - timeStamp) > timeOutInterval;
    }

    protected void resetTimeOut() {
        if (timeOutInterval > 0) {
            timeStamp = System.currentTimeMillis();
        }
    }
}         
