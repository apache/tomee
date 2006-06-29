/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.csm;

/**
 * @version $Revision$ $Date$
 */
public class NameConverter {

    public static String getXmlName(Class clazz) {
        String className = clazz.getName().replaceFirst(".*(\\.|\\$)", "");

        return getXmlName(className);
    }

    public static String getXmlName(String className) {
        StringBuffer stringBuffer = new StringBuffer(className);
        StringBuffer name = new StringBuffer();

        name.append(Character.toLowerCase(stringBuffer.charAt(0)));

        for (int i = 1; i < stringBuffer.length(); i++) {
            char c = stringBuffer.charAt(i);
            if (Character.isUpperCase(c)) {
                name.append("-");
                name.append(Character.toLowerCase(c));
            } else {
                name.append(c);
            }
        }

        return name.toString();
    }

    public static String getJavaFieldName(String xmlName) {
        StringBuffer sb = new StringBuffer(xmlName);

        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));

        for (int i = 1; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == '-' && i + 1 < sb.length()) {
                sb.deleteCharAt(i);
                c = sb.charAt(i);
                sb.setCharAt(i, Character.toUpperCase(c));
            } else {
                sb.setCharAt(i, Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public static String getJavaClassName(String xmlName) {
        StringBuffer sb = new StringBuffer(xmlName);

        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

        for (int i = 1; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == '-' && i + 1 < sb.length()) {
                sb.deleteCharAt(i);
                c = sb.charAt(i);
                sb.setCharAt(i, Character.toUpperCase(c));
            } else {
                sb.setCharAt(i, Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
