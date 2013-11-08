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
package org.apache.openjpa.enhance.stats;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * FetchStatsCollector aggregates fetch statistics and outputs the data periodically (10 minutes).
 */
public final class FetchStatsCollector {
    // Fully qualified persistent field name -> number of access
    private static ConcurrentHashMap<String, AtomicInteger> _used = new ConcurrentHashMap<String, AtomicInteger>();
    private static Set<String> _entities = new TreeSet<String>();

    private static Log _log;
    private static final Localizer _loc = Localizer.forPackage(FetchStatsCollector.class);
    // default to 10 min
    private final static int DEFAULT_INTERVAL = 10 * 60 * 1000;
    private static Timer timer;

    public static void setlogger(Log log) {
        if (FetchStatsCollector._log == null) {
            FetchStatsCollector._log = log;
            FetchStatsCollector._log.info(_loc.get("start-monitoring"));
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Shutdown());
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                TimerTask statsOutputTask = new TimerTask() {
                    public void run() {
                        dump();
                    }
                };
                timer = new Timer();
                timer.schedule(statsOutputTask, DEFAULT_INTERVAL, DEFAULT_INTERVAL);
                return null;
            }
        });
    }

    public static void registerEntity(ClassMetaData cmd) {
        _entities.add(cmd.getDescribedTypeString());
    }

    public static AtomicInteger registerField(String field) {
        return _used.putIfAbsent(field, new AtomicInteger(Integer.valueOf(0)));
    }

    public static void hit(String field) {
        AtomicInteger value = _used.get(field);
        if (value != null) {
            value.incrementAndGet();
        }
    }

    static class Shutdown extends Thread {
        @Override
        public void run() {
            timer.cancel();
            dump();
        }
    }

    public static Set<String> getStatistics() {
        // TreeSet for a sorted set.
        Set<String> noAccess = new TreeSet<String>();
        for (Map.Entry<String, AtomicInteger> entry : _used.entrySet()) {
            if (entry.getValue().intValue() == 0) {
                noAccess.add(entry.getKey());
            }
        }

        return noAccess;
    }

    public static void dump() {
        Set<String> zeroAccessFieldSet = getStatistics();

        StringBuilder message = new StringBuilder();
        message.append(_loc.get("fields-never-fetched",
            new Object[] { _entities, new Integer(zeroAccessFieldSet.size()) }).getMessage());

        for (String field : zeroAccessFieldSet) {
            message.append("\n\t" + field);

        }
        _log.info(message);
    }

    public static void clear() {
        for (Map.Entry<String, AtomicInteger> entry : _used.entrySet()) {
            entry.setValue(new AtomicInteger(0));
        }
    }

    static class Container {
        String _name;
        Integer _value;

        public Container(String name, int value) {
            _name = name;
            _value = value;
        }

        String getName() {
            return _name;
        }

        public Integer getValue() {
            return _value;
        }

        @Override
        public String toString() {
            return _name;
        }
    }
}
