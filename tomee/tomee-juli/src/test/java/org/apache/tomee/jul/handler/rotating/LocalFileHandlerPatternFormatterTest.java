/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jul.handler.rotating;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalFileHandlerPatternFormatterTest {
    private Locale locale;

    @Before
    public void setLocale() {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @Before
    public void resetLocale() {
        Locale.setDefault(locale);
    }

    @Test
    public void format() {
        final LogRecord record = new LogRecord(Level.FINE, "test message");
        record.setLoggerName("logger");
        record.setLevel(Level.FINER);
        record.setMillis(123456789);
        record.setSourceClassName("my.class.Name");
        record.setSourceMethodName("aMethod");

        // default
        assertEquals(
                "Jan 02, 1970 my.class.Name aMethod\nFINER: test message\n",
                new LocalFileHandler.PatternFormatter("%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n", Locale.ENGLISH)
                        .format(record).replace("\r", "").replaceFirst("1970.*my"/*skip time*/, "1970 my"));

        // simple
        assertEquals(
                "test message\n",
                new LocalFileHandler.PatternFormatter("%5$s%n", Locale.ENGLISH).format(record).replace("\r", ""));

        final String custom = new LocalFileHandler.PatternFormatter("%1$tY-%1$tM-%1$td %1$tT [%4$5s][%7$s] %5$s%6$s%n", Locale.ENGLISH)
                .format(record).replace("\r", "");
        assertTrue(custom
                .matches("1970\\-17\\-02 \\p{Digit}+\\:17\\:36 \\[FINER\\]\\[my\\.class\\.Name\\] test message\\\n"));
    }
}
