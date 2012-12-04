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

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.plugins.PluginLoader;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;

public final class EJBWebBeansConfigurator
{
    private EJBWebBeansConfigurator()
    {

    }

    /**
     * Returns true if given class is an deployed ejb bean class, false otherwise.
     * @param clazz bean class
     * @param webBeansContext
     * @return true if given class is an deployed ejb bean class
     * @throws WebBeansConfigurationException if any exception occurs
     */
    public static boolean isSessionBean(Class<?> clazz, WebBeansContext webBeansContext) throws WebBeansConfigurationException
    {
        PluginLoader loader = webBeansContext.getPluginLoader();
        OpenWebBeansEjbPlugin ejbPlugin = loader.getEjbPlugin();
        
        //There is no ejb container
        if(ejbPlugin == null)
        {
            return false;
        }
        
        return ejbPlugin.isSessionBean(clazz);
    }
    
    /**
     * Returns ejb bean.
     * @param webBeansContext
     * @param <T> bean class info
     * @param clazz bean class
     * @return ejb bean
     */
    public static <T> Bean<T> defineEjbBean(Class<T> clazz, ProcessAnnotatedType<T> processAnnotatedTypeEvent,
                                            WebBeansContext webBeansContext)
    {
        PluginLoader loader = webBeansContext.getPluginLoader();
        OpenWebBeansEjbPlugin ejbPlugin = loader.getEjbPlugin();
        
        if(ejbPlugin == null)
        {
            throw new IllegalStateException("There is no provided EJB plugin. Unable to define session bean for class : " + clazz.getName());
        }
        
        return ejbPlugin.defineSessionBean(clazz, processAnnotatedTypeEvent);
    }
    
}
