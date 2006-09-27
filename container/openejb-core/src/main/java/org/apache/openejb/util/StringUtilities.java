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