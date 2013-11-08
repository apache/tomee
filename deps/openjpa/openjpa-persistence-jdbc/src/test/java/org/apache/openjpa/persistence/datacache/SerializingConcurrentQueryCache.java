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
package org.apache.openjpa.persistence.datacache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.openjpa.datacache.ConcurrentQueryCache;
import org.apache.openjpa.datacache.QueryKey;
import org.apache.openjpa.datacache.QueryResult;

/**
 * A QueryCache implementation that serializes all keys / values to simulate having a remote cache.
 */
public class SerializingConcurrentQueryCache extends ConcurrentQueryCache {
    private static final long serialVersionUID = 1L;
    public static String SERIALIZING_CONCURRENT_QUERY_CACHE = SerializingConcurrentQueryCache.class.getName();

    @Override
    public QueryResult get(QueryKey key) {
        return roundtrip(super.get(roundtrip(key)));
    }

    @Override
    public QueryResult put(QueryKey qk, QueryResult oids) {
        roundtrip(qk);
        roundtrip(oids);
        return roundtrip(super.put(qk, oids));
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundtrip(T o) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeObject(o);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
            return (T) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
