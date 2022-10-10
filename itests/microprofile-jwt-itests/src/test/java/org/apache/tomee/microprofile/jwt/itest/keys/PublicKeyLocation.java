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
package org.apache.tomee.microprofile.jwt.itest.keys;

import org.apache.openejb.util.Duration;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PublicKeyLocation {

    private Duration initialRetryDelay = new Duration("500 milliseconds");
    private Duration maxRetryDelay = new Duration("5 seconds");
    private Duration accessTimeout = new Duration("1 second");
    private Duration refreshInterval = new Duration("1 second");
    private String location = null;

    public PublicKeyLocation location(final URI location) {
        this.location = location.toASCIIString();
        return this;
    }

    public PublicKeyLocation location(final String location) {
        this.location = location;
        return this;
    }

    public PublicKeyLocation initialRetryDelay(final long time, final TimeUnit unit) {
        this.initialRetryDelay = new Duration(time, unit);
        return this;
    }

    public PublicKeyLocation maxRetryDelay(final long time, final TimeUnit unit) {
        this.maxRetryDelay = new Duration(time, unit);
        return this;
    }

    public PublicKeyLocation accessTimeout(final long time, final TimeUnit unit) {
        this.accessTimeout = new Duration(time, unit);
        return this;
    }

    public PublicKeyLocation refreshInterval(final long time, final TimeUnit unit) {
        this.refreshInterval = new Duration(time, unit);
        return this;
    }

    public Properties build() throws Exception {
        return p(
                "mp.jwt.verify.publickey.location", location,
                "tomee.jwt.verify.publickey.cache", true,
                "tomee.jwt.verify.publickey.cache.initialRetryDelay", initialRetryDelay,
                "tomee.jwt.verify.publickey.cache.maxRetryDelay", maxRetryDelay,
                "tomee.jwt.verify.publickey.cache.accessTimeout", accessTimeout,
                "tomee.jwt.verify.publickey.cache.refreshInterval", refreshInterval
        );
    }

    private static Properties p(final Object... p) {
        if (p.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of strings is required");
        }
        final Properties properties = new Properties();
        for (int i = 0; i < p.length; i += 2) {
            final String key = p[i].toString();
            final String value = p[i + 1].toString();
            properties.put(key, value);
        }
        return properties;
    }

}
