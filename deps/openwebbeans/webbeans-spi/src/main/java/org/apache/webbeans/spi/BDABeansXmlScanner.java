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
package org.apache.webbeans.spi;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * due to a file-url issue it isn't compatible with wls (see OWB-519)
 */
public interface BDABeansXmlScanner
{

    /**
     * Stores the beans.xml an Interceptor class is defined in
     * 
     * @return T - Interceptor class successfully stored; F - Interceptor class
     *         already exists and was not stored.
     */
    public boolean addInterceptor(Class<?> interceptorClass,
            String beansXMLFilePath);
    
    /**
     * 
     * @param beansXMLFilePath
     * @return a non-null set of Interceptors defined by the specified
     *         beansXMLFilePath
     */
    public Set<Class<?>> getInterceptors(String beansXMLFilePath);
    
    /**
     * 
     * @param beansXMLFilePath
     * @return a non-null set of Decorators defined by the specified
     *         beansXMLFilePath
     */
    public Set<Class<?>> getDecorators(String beansXMLFilePath);
    
    /**
     * Stores the beans.xml a Decorator class is defined in
     * 
     * @return T - Decorator class successfully stored; F - Decorator class
     *         already exists and was not stored.
     */
    public boolean addDecorator(Class<?> decoratorClass, String beansXMLFilePath);
    
    /**
     * Stores the beans.xml an Alternative class is defined in
     * 
     * @return T - Alternative class successfully stored; F - Alternative class
     *         already exists and was not stored.
     */
    public boolean addAlternative(Class<?> alternativeClass,
            String beansXMLFilePath);
    
    /**
     * Stores the beans.xml a Stereotype class is defined in
     * 
     * @return T - Stereotype class successfully stored; F - Stereotype class
     *         already exists and was not stored.
     */
    public boolean addStereoType(Class<? extends Annotation> stereoTypeClass,
            String beansXMLFilePath);
    
    /**
     * 
     * @param beansXMLFilePath
     * @return a non-null set of Alternatives defined by the specified
     *         beansXMLFilePath
     */
    public Set<Class<?>> getAlternatives(String beansXMLFilePath);
    
    /**
     * 
     * @param beansXMLFilePath
     * @return a non-null set of Stereotypes defined by the specified
     *         beansXMLFilePath
     */
    public Set<Class<? extends Annotation>> getStereotypes(
            String beansXMLFilePath);
    
    /**
     * 
     * @param class1
     *            deployed class
     * @return A String representing the file path to the beans.xml of the
     *         specified class's BDA
     */
    public String getBeansXml(Class<?> class1);
    
    /**
     * 
     * @param class1
     *            BDA class
     * @param beansXmlFilePath
     *            - file path to beans.xml of BDA
     */
    public void setBeansXml(Class<?> class1, String beansXmlFilePath);

}