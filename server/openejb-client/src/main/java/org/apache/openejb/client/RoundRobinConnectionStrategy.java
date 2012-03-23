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
package org.apache.openejb.client;

import org.apache.openejb.client.event.FailoverSelection;
import org.apache.openejb.client.event.RoundRobinFailoverSelection;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy {

    private static class RoundRobinIterable implements Iterable<URI> {
        private final URI[] locations;
        private AtomicInteger index = new AtomicInteger(-1);

        private RoundRobinIterable(ClusterMetaData clusterMetaData) {
            this.locations = clusterMetaData.getLocations();
        }

        private int index() {
            final int i = index.incrementAndGet();
            if (i < locations.length) return i;

            index.compareAndSet(i, -1);
            return index();
        }

        @Override
        public Iterator<URI> iterator() {
            return new RoundRobinIterator();
        }

        private class RoundRobinIterator implements Iterator<URI> {
            private final Set<URI> seen = new HashSet<URI>();

            @Override
            public boolean hasNext() {
                return seen.size() < locations.length;
            }

            @Override
            public URI next() {
                if (!hasNext()) throw new NoSuchElementException();

                final URI location = locations[index()];
                seen.add(location);

                return location;
            }

            @Override
            public void remove() {
            }
        }
    }

    @Override
    protected FailoverSelection createFailureEvent(Set<URI> remaining, Set<URI> failed, URI uri) {
        return new RoundRobinFailoverSelection(remaining, failed, uri);
    }

    @Override
    protected Iterable<URI> createIterable(ClusterMetaData cluster) {
        return new RoundRobinIterable(cluster);
    }

}