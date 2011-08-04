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

/**
 * @version $Rev$ $Date$
 */
public class Strings {


    public static String lc(String string){
        return lowercase(string);
    }

    public static String lowercase(String string) {
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < sb.length(); i++) {
              sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static String uc(String string){
        return uppercase(string);
    }

    public static String uppercase(String string) {
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < sb.length(); i++) {
              sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static String ucfirst(String string){
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        if (sb.length() > 0){
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static String lcfirst(String string){
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        if (sb.length() > 0){
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static String camelCase(String string){
        StringBuilder sb = new StringBuilder();
        String[] strings = string.split("-");
        for (String s : strings) {
            int l = sb.length();
            sb.append(s);
            sb.setCharAt(l, Character.toUpperCase(sb.charAt(l)));
        }
        return sb.toString();
    }

}
