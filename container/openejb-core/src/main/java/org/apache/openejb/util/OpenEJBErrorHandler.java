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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util;

import org.apache.openejb.OpenEJBException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class OpenEJBErrorHandler {

    private static final Logger _logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final Messages _messages = new Messages("org.apache.openejb.util.resources");

    public static void handleUnknownError(final Throwable error, final String systemLocation) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintWriter pw = new PrintWriter(baos);
        error.printStackTrace(pw);
        pw.flush();
        pw.close();

        _logger.error("ge0001", systemLocation, new String(baos.toByteArray()));

        /*
         * An error broadcasting system is under development.
         * At this point an appropriate error would be broadcast to all listeners.
         */
    }

    public static void propertiesObjectIsNull(final String systemLocation) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0002", systemLocation));
    }

    public static void propertyFileNotFound(final String propertyfileName, final String systemLocation) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0003", propertyfileName, systemLocation));
    }

    public static void propertyNotFound(final String propertyName, final String propertyfileName) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0004", propertyName, propertyfileName));
    }

    public static void propertyValueIsIllegal(final String propertyName, final String value) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0005", propertyName, value));
    }

    public static void propertyValueIsIllegal(final String propertyName, final String value, final String message) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0006", propertyName, value, message));
    }

    public static void classNotFound(final String systemLocation, final String className) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0007", systemLocation, className));
    }

    public static void classNotAccessible(final String systemLocation, final String className) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0008", systemLocation, className));
    }

    public static void classNotIntantiateable(final String systemLocation, final String className) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0009", systemLocation, className));
    }

    public static void classNotIntantiateableForUnknownReason(final String systemLocation, final String className, final String exceptionClassName, final String message) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0011", systemLocation, className, exceptionClassName, message));
    }

    public static void classNotIntantiateableFromCodebaseForUnknownReason(final String systemLocation, final String className, final String codebase, final String exceptionClassName, final String message)
        throws OpenEJBException {
        throw new OpenEJBException(_messages.format("ge0012", systemLocation, className, codebase, exceptionClassName, message));
    }

    public static void classCodebaseNotFound(final String systemLocation, final String className, final String codebase, final Exception e) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0010", systemLocation, className, codebase, e.getMessage(), e));
    }

    public static void configurationParsingError(final String messageType, final String message, final String line, final String column) {

        _logger.error("as0001", messageType, message, line, column);
        /*
         * An error broadcasting system is under development.
         * At this point an appropriate error would be broadcast to all listeners.
         */
    }

}
