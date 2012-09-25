/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.injection.injectionpoint.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import org.apache.webbeans.config.PropertyLoader;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

/**
 * Factory class for PropertyHolder Annotation. 
 * Defines @Produces methods for PropertyHolder.
 */
public class PropertyHolderFactory {

    private static final Logger logger = WebBeansLoggerFacade.getLogger(PropertyHolderFactory.class);
    
    //Properties
    private static final String PROPERTY_FILE = 
            "org/apache/webbeans/newtests/injection/injectionpoint/tests/PlaceHolder.properties";
    private volatile static Properties placeHolderProperties;
    
    //Inject classes PropertyHolderFactory depends upon.
    @Inject
    private DataTransformer dataTransformer;
    
    @Inject
    private PropertyEncryptor propertyEncryptor;

    /**
     * Defines retrieval of Properties from placeHolder.properties file in class
     * path.
     *
     * @return Properties
     */
    public synchronized static Properties getProperties() {

        if (placeHolderProperties == null) {
            placeHolderProperties = PropertyLoader.getProperties(PROPERTY_FILE);
            logger.info("loaded "+placeHolderProperties);
        }
        return placeHolderProperties;
    }

    /**
     * Defines PropertyHolder value fetch, Producer method.
     *
     * @param injectionPoint
     * @return String
     */
    @Produces
    @PropertyHolder
    public String getPlaceHolderValue(InjectionPoint injectionPoint) {

        logger.log(Level.INFO, "getPlaceHolderValue {0}", injectionPoint);

        //Get value attribute of the PlaceHolder Annotation
        String keyName = injectionPoint.getAnnotated().getAnnotation(PropertyHolder.class).value();

        //If PlaceHolder value annotation is not specified, Use field name as Key to Properties.
        if (isBlank(keyName)) {
            keyName = injectionPoint.getMember().getName();
        }

        //Consturct list for appending
        List<String> stringList = new ArrayList<String>();
        stringList.add(injectionPoint.getMember().getDeclaringClass().getName());
        stringList.add(".");
        stringList.add(keyName);

        //Constuct KeyName string.
        keyName = dataTransformer.concatStrings(stringList);
        logger.info("Fetching value for key: " + keyName);

        //Check System Property, if its not available check in Properties file.
        String keyValue = System.getProperty(keyName);
        if (isBlank(keyValue)) {
            Properties properties = PropertyHolderFactory.getProperties();
            keyValue = properties.getProperty(keyName);
        }

        //Check for Encrypted property value
        keyValue = this.decryptProperty(keyValue);

        logger.info("Produced property : Key->{" + keyName + "}, Value->{" + keyValue + "}");

        
        return keyValue;
    }//End Method.

    /**
     * Checks if given property value is encrypted. If encrypted, returns the
     * decrypted value.
     *
     * @param propertyValue
     * @return String
     */
    private String decryptProperty(String propertyValue) {
        logger.info("Checking if decrypting of value is needed for " + propertyValue);

        if (!isEmpty(propertyValue)
                && propertyValue.matches("ENC(\\S+)")) {

            String decryptPropertyValue = substringBetween(propertyValue, "ENC(", ")");
            propertyValue = propertyEncryptor.decryptProperty(decryptPropertyValue);
        }

        return propertyValue;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Sets DataTransformer instance.
     *
     * @param dataTransformer
     */
    public void setDataTransformer(DataTransformer dataTransformer) {
        this.dataTransformer = dataTransformer;
    }
    public static final int INDEX_NOT_FOUND = -1;

    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            int end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
}
