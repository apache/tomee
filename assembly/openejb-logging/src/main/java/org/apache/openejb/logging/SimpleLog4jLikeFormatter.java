/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.apache.openejb.logging;

import org.apache.log4j.SimpleLayout;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static org.apache.openejb.logging.Converter.toLoggingEvent;

/**
 * @author Romain Manni-Bucau
 */
public class SimpleLog4jLikeFormatter extends Formatter {
    private static final SimpleLayout layout = new SimpleLayout();

    public synchronized String format(LogRecord record) {
        return layout.format(toLoggingEvent(record));
    }
}
