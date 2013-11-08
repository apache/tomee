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
package org.apache.openjpa.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Exception indicating that locks on one or more objects could not be acquired.
 *
 * @author Marc Prud'hommeaux
 * @since 0.3.1
 */
@SuppressWarnings("serial")
public class LockException
    extends StoreException {

    private static final transient Localizer _loc = Localizer.forPackage(LockException.class);

    private int timeout   = -1;
    private int lockLevel = -1;
    
    public LockException(Object failed) {
        super(_loc.get("lock-failed", Exceptions.toString(failed)));
        setFailedObject(failed);
    }

    public LockException(Object failed, int timeout) {
        this(failed, timeout, -1);
    }
    
    public LockException(Object failed, int timeout, int lockLevel) {
        super(_loc.get("lock-timeout", Exceptions.toString(failed), String.valueOf(timeout)));
        setFailedObject(failed);
        setTimeout(timeout);
        setLockLevel(lockLevel);
    }

    public int getSubtype() {
        return LOCK;
    }

    /**
     * The number of milliseconds to wait for a lock.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * The number of milliseconds to wait for a lock.
     */
    public LockException setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public void setLockLevel(int lockLevel) {
        this.lockLevel = lockLevel;
    }

    public int getLockLevel() {
        return lockLevel;
    }

    public String toString() {
        String str = super.toString();
        if (timeout < 0)
            return str;
        return str + Exceptions.SEP + "Timeout: " + timeout + ", LockLevel" + lockLevel;
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException {
        out.writeInt(timeout);
        out.writeInt(lockLevel);
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        timeout = in.readInt();
        lockLevel = in.readInt();
	}
}
