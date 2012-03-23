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
import java.util.Collections;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class FailoverSelection {
    protected final Set<URI> remaining;
    protected final Set<URI> failed;
    protected final URI server;

    public FailoverSelection(Set<URI> remaining, Set<URI> failed, URI server) {
        this.remaining = Collections.unmodifiableSet(remaining);
        this.failed = Collections.unmodifiableSet(failed);
        this.server = server;
    }

    public Set<URI> getRemaining() {
        return remaining;
    }

    public Set<URI> getFailed() {
        return failed;
    }

    public URI getServer() {
        return server;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "remaining=" + remaining.size() +
                ", failed=" + failed.size() +
                ", server=" + server +
                '}';
    }
}
