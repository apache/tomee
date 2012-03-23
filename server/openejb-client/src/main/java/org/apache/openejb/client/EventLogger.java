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
package org.apache.openejb.client;

import org.apache.openejb.client.event.ClusterMetaDataUpdated;
import org.apache.openejb.client.event.Log;
import org.apache.openejb.client.event.Observes;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class EventLogger {

    public void log(@Observes ClusterMetaDataUpdated event) {
        final Logger logger = Logger.getLogger(event.getClass().getName());

        final ClusterMetaData cluster = event.getClusterMetaData();

        final String msg = event.toString();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, msg);
        }

        if (logger.isLoggable(Level.FINER)) {
            int i = 0;
            for (URI uri : cluster.getLocations()) {
                final String format = String.format("%s #%s %s", msg, ++i, uri.toASCIIString());
                logger.log(Level.FINER, format);
            }
        }
    }

    public void log(@Observes Object event) {
        final Log log = event.getClass().getAnnotation(Log.class);

        if (log == null) return;

        final Logger logger = Logger.getLogger(event.getClass().getName());

        try {
            final Level level = Level.parse(log.value().name());

            if (logger.isLoggable(level)) {
                logger.log(level, event.toString());
            }

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, event.toString());
        }
    }
}
