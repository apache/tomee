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

import java.net.URL;
import java.util.Set;


/**
 * <p>This SPI is for abstracting the class scanning.</p>
 *
 * <p>In a production environment Many different modules need to perform
 * class scanning (EJB, JSF, JPA, ...). This SPI allows us to only have one 
 * central class scanner for the whole application server
 * which only performs the scanning once at startup of each WebApp.</p>
 *
 * <p>All URL path Strings in this interface contain the the protocol,
 * e.g. 'file:/...' we get directly from {@link java.net.URL#toExternalForm()}</p>
 *
 */
public interface ScannerService
{
    /**
     * Any initializtion action that is
     * required by the implementors. 
     * @param object initialization object
     */
    public void init(Object object);
    
    /**
     * Perform the actual class scanning.
     */
    public void scan();


    /**
     * This method will get called once the information found by the current
     * scan is not needed anymore and the ScannerService might free up
     * resources.
     */
    public void release();

    
    /**
     * Gets xml configuration files that are occured
     * in the deployment archives.
     * @return the URL of the beans.xml files.
     */
    public Set<URL> getBeanXmls();
    
    /**
     * Gets beans classes that are found in the
     * deployment archives. 
     * @return bean classes
     */
    public Set<Class<?>> getBeanClasses();


    /**
     * @param className
     * @return all Annotations used in the whole class
     */
    public Set<String> getAllAnnotations(String className);
    
    /**
     * Indicates if BDABeansXmlScanner is available. This method 
     * should only return true if a BDABeansXmlScanner is implemented
     * and the OpenWebBeansConfiguration.USE_BDA_BEANSXML_SCANNER 
     * custom property is set to true.
     * @return T - BDABeansXmlScanner is available and enabled;
     * F - No BDABeansXmlScanner is available or it is disabled
     */
    public boolean isBDABeansXmlScanningEnabled();    
    
    /**
     * Gets BDABeansXMLScanner used to determine the beans.xml 
     * modifiers (interceptors, decorators, and, alternatives) that
     * are enabled per BDA. This is different from the default behavior
     * that enables modifiers per application and not just in one BDA
     * contained in an application.
     * @return null or reference to BDABeansXMLScanner
     */
    public BDABeansXmlScanner getBDABeansXmlScanner();
}
