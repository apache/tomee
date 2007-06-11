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
package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.apache.openejb.OpenEJBException;

public class OpenEJBErrorHandler {

    private static Logger _logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");
    private static Messages _messages = new Messages("org.apache.openejb.util.resources");

    public static void handleUnknownError(Throwable error, String systemLocation) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        error.printStackTrace(pw);
        pw.flush();
        pw.close();

        _logger.i18n.error("ge0001", systemLocation, new String(baos.toByteArray()));

        /*
         * An error broadcasting system is under development.
         * At this point an appropriate error would be broadcast to all listeners.
         */
    }

    public static void propertiesObjectIsNull(String systemLocation) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0002", systemLocation));
    }

    public static void propertyFileNotFound(String propertyfileName, String systemLocation) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0003", propertyfileName, systemLocation));
    }

    public static void propertyNotFound(String propertyName, String propertyfileName) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0004", propertyName, propertyfileName));
    }

    public static void propertyValueIsIllegal(String propertyName, String value) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0005", propertyName, value));
    }

    public static void propertyValueIsIllegal(String propertyName, String value, String message) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0006", propertyName, value, message));
    }

    public static void classNotFound(String systemLocation, String className) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0007", systemLocation, className));
    }

    public static void classNotAccessible(String systemLocation, String className) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0008", systemLocation, className));
    }

    public static void classNotIntantiateable(String systemLocation, String className) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0009", systemLocation, className));
    }

    public static void classNotIntantiateableForUnknownReason(String systemLocation, String className, String exceptionClassName, String message) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0011", systemLocation, className, exceptionClassName, message));
    }

    public static void classNotIntantiateableFromCodebaseForUnknownReason(String systemLocation, String className, String codebase, String exceptionClassName, String message)
            throws OpenEJBException {
        throw new OpenEJBException(_messages.format("ge0012", systemLocation, className, codebase, exceptionClassName, message));
    }

    public static void classCodebaseNotFound(String systemLocation, String className, String codebase, Exception e) throws OpenEJBException {

        throw new OpenEJBException(_messages.format("ge0010", systemLocation, className, codebase, e.getMessage(), e));
    }

    public static void configurationParsingError(String messageType, String message, String line, String column) {

        _logger.i18n.error("as0001", messageType, message, line, column);
        /*
         * An error broadcasting system is under development.
         * At this point an appropriate error would be broadcast to all listeners.
         */
    }

}
