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
package org.apache.webbeans.config;

import org.apache.webbeans.corespi.DefaultSingletonService;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.spi.SingletonService;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Holds the SingletonService that tracks WebBeansContext instances
 * 
 * @version $Rev$ $Date$
 *
 */
public final class WebBeansFinder
{   
    //How you use singleton provider ,
    //As a default we use ClassLoader --> Object
    private static SingletonService<WebBeansContext> singletonService = new DefaultSingletonService();

    /** safety mechanism to allow setting a special SingletonService only once */
    private static boolean customSingletonServiceUsed = false;

    /**
     * No instantiate.
     */
    private WebBeansFinder()
    {
        //No action
    }
    
    public static WebBeansContext getSingletonInstance()
    {
        return singletonService.get(WebBeansUtil.getCurrentClassLoader());
    }

    public static void clearInstances(Object key)
    {
        singletonService.clear(key);
    }

    // Third pary frameworks can set singleton instance
    // For example, OpenEJB could provide its own provider
    // This can be called at most once.
    public static void setSingletonService(SingletonService<WebBeansContext> singletonSvc)
    {
        if (customSingletonServiceUsed && !singletonService.equals(singletonSvc))
        {
            throw new WebBeansConfigurationException("Already using another custom SingletonService!");
        }
        
        singletonService = singletonSvc;
        customSingletonServiceUsed = true;
    }

}