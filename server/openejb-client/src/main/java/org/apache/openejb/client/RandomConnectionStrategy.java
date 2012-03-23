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
import org.apache.openejb.client.event.RandomFailoverSelection;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class RandomConnectionStrategy extends AbstractConnectionStrategy {

    @Override
    protected FailoverSelection createFailureEvent(Set<URI> remaining, Set<URI> failed, URI uri) {
        return new RandomFailoverSelection(remaining, failed, uri);
    }

    @Override
    protected Iterable<URI> createIterable(ClusterMetaData cluster) {
        return new RandomIterable(cluster);
    }

    public static class RandomIterable implements Iterable<URI> {
        private final URI[] locations;

        public RandomIterable(ClusterMetaData clusterMetaData) {
            this.locations = clusterMetaData.getLocations();
        }

        @Override
        public Iterator<URI> iterator() {
            return new RandomIterator<URI>(locations);
        }
    }

    public static class RandomIterator<T> implements Iterator<T> {
        private final Random random = new Random();
        private final T[] items;
        private int size;

        public RandomIterator(T[] items) {
            this.items = Arrays.copyOf(items, items.length);
            this.size = items.length;
        }

        @Override
        public boolean hasNext() {
            return size > 0;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();

            // Random.nextInt is exclusive.
            // So if size=10, result will be between 0-9
            final int selected = random.nextInt(size--);

            T selectedObject = items[selected];

            // Take the object from the end of the list
            // and move it into the place where selected was.
            items[selected] = items[size];
            items[size] = null;

            return selectedObject;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}