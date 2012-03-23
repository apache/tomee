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
package org.apache.openejb.itest.failover;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
* @version $Rev$ $Date$
*/
public class ClientThread implements Runnable {

    private final AtomicBoolean run = new AtomicBoolean(false);
    private final AtomicLong delay = new AtomicLong(0);
    private final Callable callable;

    public ClientThread(Callable callable) {
        this.callable = callable;
    }

    @Override
    public void run() {
        while (run.get()) {
            pause();

            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ClientThread delay(long delay){
        setDelay(delay);
        return this;
    }

    public void setDelay(long delay) {
        this.delay.set(delay);
    }

    private void pause() {
        final long l = delay.get();
        try {
            if (l > 0) Thread.sleep(l);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public ClientThread start() {
        run.set(true);
        final Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
        return this;
    }

    public ClientThread stop() {
        run.set(false);
        return this;
    }
}
