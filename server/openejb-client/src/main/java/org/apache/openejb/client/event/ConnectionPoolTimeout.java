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
package org.apache.openejb.client.event;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
@Log(Log.Level.FINE)
public class ConnectionPoolTimeout {

    private final URI uri;
    private final int size;
    private final long timeout;
    private final TimeUnit timeUnit;
    private final Throwable caller;

    public ConnectionPoolTimeout(URI uri, int size, long timeout, TimeUnit timeUnit, Throwable caller) {
        this.uri = uri;
        this.size = size;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.caller = caller;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public int getSize() {
        return size;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "ConnectionPoolCreated{" +
                "uri=" + uri +
                ", size=" + size +
                ", timeout='" + timeout + " " + timeUnit + "'" +
                '}';
    }
}
