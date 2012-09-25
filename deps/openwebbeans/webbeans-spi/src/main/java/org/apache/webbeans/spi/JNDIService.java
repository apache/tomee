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

/**
 * SPI for setting and retrieving objects from the JNDI.
 * Use this interface for all objects which should be stored and retreived
 * from the JNDI at runtime.
 *
 * This may be implemented via a static map for 
 * environments where no JNDI is available.
 */
public interface JNDIService
{
    /**JMS Connection Factory JNDI Name*/
    public static final String JMS_CONNECTION_FACTORY_JNDI_KEY
            = "org.apache.webbeans.spi.JNDIService.jmsConnectionFactoryJndi";
    
    /**Default value*/
    public static final String JMS_CONNECTION_FACTORY_JNDI_DEFAULT_VALUE = "ConnectionFactory";
    
    /**
     * Bind the object with the given name to the JNDI environment
     *  
     * @param name
     * @param object
     * @throws WebBeansException on any internal error
     */
    public abstract void bind(String name, Object object);

    /**
     * Unbind and free the object with the given name from the JNDI environment
     * @param name
     * @throws WebBeansException on any internal error
     */
    public abstract void unbind(String name);

    /**
     * Get the object bound to the given name from the JNDI context.   
     * @param name
     * @param expectedClass
     * @return the bound object or <code>null</code> if nothing bound.
     * @throws WebBeansException on any internal error
     */
    public abstract <T> T lookup(String name, Class<? extends T> expectedClass);

}