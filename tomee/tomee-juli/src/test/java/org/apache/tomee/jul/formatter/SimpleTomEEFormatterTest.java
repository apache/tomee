/**
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * </p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */
package org.apache.tomee.jul.formatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertEquals;

public class SimpleTomEEFormatterTest {
    private static final String LINE_SEPARATOR_KEY = "line.separator";

    @Test
    public void formatNullThrown() throws Exception {
        final String previousLineSeparatorProperty = System.getProperty(LINE_SEPARATOR_KEY);
        try {
            final String lineSeparatorValue = "\n";
            final String logMessage = "An example log record";
            final Level level = Level.FINEST;

            System.setProperty(LINE_SEPARATOR_KEY, lineSeparatorValue);
            final LogRecord logRecordInput = new LogRecord(level, logMessage);
            logRecordInput.setThrown(null);

            final Formatter formatter = new SimpleTomEEFormatter();
            final String actualFormatOutput = formatter.format(logRecordInput);

            final String expectedFormatOutput = level.getLocalizedName() + " - " + logMessage + "\n";

            assertEquals(expectedFormatOutput, actualFormatOutput);
        } finally {
            System.setProperty(LINE_SEPARATOR_KEY, previousLineSeparatorProperty);
        }
    }

    @Test
    public void formatNotNullThrown() throws Exception {
        final String previousLineSeparatorProperty = System.getProperty(LINE_SEPARATOR_KEY);

        try {
            final String lineSeparatorValue = "\n";
            final String logMessage = "An example log record";
            final Level level = Level.CONFIG;
            final String exceptionMessage = "An example exception";
            final Throwable thrown = new Exception(exceptionMessage);

            System.setProperty(LINE_SEPARATOR_KEY, lineSeparatorValue);
            final LogRecord logRecordInput = new LogRecord(level, logMessage);
            logRecordInput.setThrown(thrown);

            final Formatter formatter = new SimpleTomEEFormatter();
            final String actualFormatOutput = formatter.format(logRecordInput);

            final String expectedFormatOutput = level.getLocalizedName() + " - " + logMessage + lineSeparatorValue + ExceptionUtils.getStackTrace(thrown);

            assertEquals(expectedFormatOutput, actualFormatOutput);
        } finally {
            System.setProperty(LINE_SEPARATOR_KEY, previousLineSeparatorProperty);
        }
    }

}
