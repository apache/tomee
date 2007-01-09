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

import java.lang.reflect.Method;
import java.util.StringTokenizer;

public class StringUtilities {

    public static final String CRLF = "\r\n";

    //we don't want anyone creating new instances
    private StringUtilities() {
    }

    public static String getLastToken(String tokenString, String delimeter) {
        StringTokenizer token = new StringTokenizer(tokenString, delimeter);

        String returnValue = null;
        while (token.hasMoreTokens()) {
            returnValue = token.nextToken();
        }

        return returnValue;
    }

    public static String nullToBlankString(String stringToCheckForNull) {
        return (stringToCheckForNull == null) ? "" : stringToCheckForNull;
    }

    public static boolean checkNullBlankString(String stringToCheck) {
        return (stringToCheck == null || "".equals(stringToCheck.trim()));
    }

    public static String blankToNullString(String stringToCheckForBlank) {
        if (stringToCheckForBlank != null) stringToCheckForBlank = stringToCheckForBlank.trim();
        return ("".equals(stringToCheckForBlank)) ? null : stringToCheckForBlank;
    }

    public static String replaceNullOrBlankStringWithNonBreakingSpace(String stringToCheckForNull) {
        if ((stringToCheckForNull == null) || (stringToCheckForNull.equals(""))) {
            return "&nbsp;";
        } else {
            return stringToCheckForNull;
        }
    }

    public static String createMethodString(Method method, String lineBreak) {
        Class[] parameterList = method.getParameterTypes();
        Class[] exceptionList = method.getExceptionTypes();
        StringBuffer methodString = new StringBuffer();

        methodString.append(method.getName()).append("(");

        for (int j = 0; j < parameterList.length; j++) {
            methodString.append(StringUtilities.getLastToken(parameterList[j].getName(), "."));

            if (j != (parameterList.length - 1)) {
                methodString.append(", ");
            }
        }
        methodString.append(") ");

        if (exceptionList.length > 0) {
            methodString.append(lineBreak);
            methodString.append("throws ");
        }

        for (int j = 0; j < exceptionList.length; j++) {
            methodString.append(StringUtilities.getLastToken(exceptionList[j].getName(), "."));

            if (j != (exceptionList.length - 1)) {
                methodString.append(", ");
            }
        }

        return methodString.toString();
    }

    public static String stringArrayToCommaDelimitedStringList(String[] stringArray) {
        StringBuffer stringList = new StringBuffer();
        for (int i = 0; i < stringArray.length; i++) {
            stringList.append(stringArray[i]);
            if (i != (stringArray.length - 1)) {
                stringList.append(",");
            }
        }

        return stringList.toString();
    }

}