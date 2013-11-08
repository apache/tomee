/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package openbook.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * A set of static utilities used by the Java Server Pages.
 * 
 * @author Pinaki Poddar
 *
 */
public class JSPUtility {
    
    /**
     * Converts the given number in currency format.
     */
     public static final DecimalFormat currencyFormatter = new DecimalFormat("###.##");
     public static final DateFormat dateFormatter = new SimpleDateFormat("MMM dd, HH:mm");
     
     public static String format(Number price) {
        return currencyFormatter.format(price);
     }
     
     public static String format(Date date) {
        return dateFormatter.format(date);
     }
    
    /**
     * Converts the given String to a double.
     * Return null if the String is null or non-numeric.
     */
    public static Double toDouble(String v) {
       try {
           return Double.parseDouble(v);
       } catch (NumberFormatException e) {
           return null;
       }
     }
    
    /**
     * Encodes parameter key-values in a URL.
     * 
     * @param page the base page
     * @param params key-value pairs of parameters passed in to the page URL.
     * null or empty argument is allowed. 
     * @return a URL encoded string
     */
    public static String encodeURL(String page, Object...params) {
        StringBuilder paramBuffer = new StringBuilder();
        if (params != null && params.length != 0) {
            if (params.length % 2 != 0) {
                throw new IllegalArgumentException("Odd number of encoding parameters " + 
                        Arrays.toString(params) + " to " + page);
            }
            for (int i = 0; i < params.length; i += 2) {
                if (paramBuffer.length() > 0)
                    paramBuffer.append("&");
                String key   = params[i]   == null ? "" : params[i].toString();
                String value = params[i+1] == null ? "" : params[i+1].toString();
                paramBuffer.append(key).append("=").append(encode(value));
            }
        }
        if (paramBuffer.length() >0)
            return page + "?" + paramBuffer;
        else
            return page;
    }
    
    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
    
    public static final String SRC_ROOT = "generated-html";
    public static String getURL(String className, String anchor) {
        return SRC_ROOT + "/" + className.replace('.', '/') + (anchor != null ? "#"+anchor : "");
    }
}
