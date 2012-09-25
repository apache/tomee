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
package org.apache.webbeans.lifecycle.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ClassPool;

import org.apache.webbeans.corespi.scanner.AbstractMetaDataDiscovery;
import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.Asserts;

/**
 * Used by each test. 
 * @version $Rev$ $Date$
 *
 */
public class OpenWebBeansTestMetaDataDiscoveryService extends AbstractMetaDataDiscovery
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(OpenWebBeansTestMetaDataDiscoveryService.class);

    public OpenWebBeansTestMetaDataDiscoveryService()
    {
        
    }

    @Override
    protected void configure()
    {
        //Nothing we scan
    }
    
    /**
     * Those classes will be scanned by container.
     * @param classes deployed classes
     */
    public void deployClasses(Collection<Class<?>> classes)
    {
        if(classes != null)
        {
            for(Class<?> clazz : classes)
            {
                addBeanClass(clazz);
            }
        }
    }
    
    /**
     * Those xmls will be scanned by container.
     * @param xmls beans xmls
     */
    public void deployXMLs(Collection<String> xmls)
    {
        if(xmls != null)
        {
            for(String url : xmls)
            {
                try
                {
                    addBeanXml(new URL(url));
                }
                catch (MalformedURLException e)
                {
                    throw new WebBeansDeploymentException("could not convert to URL: " + url, e);
                }
            }
        }
    }
    
    
    /**
     * Adds new class for scanning.
     * @param clazz new scanned class
     */
    private void addBeanClass(Class<?> clazz)
    {
        Asserts.assertNotNull(clazz);
        
        URL url = ClassPool.getDefault().find(clazz.getName());
        
        try
        {
            getAnnotationDB().scanClass(url.openStream());
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING, "Problems while scanning class " + clazz.getName());
        }
    }
    
    /**
     * Adds new beans.xml url for scanning.
     * @param url new xml url
     */
    private void addBeanXml(URL url)
    {
        Asserts.assertNotNull(url);
        
        addWebBeansXmlLocation(url);
    }
    

}
