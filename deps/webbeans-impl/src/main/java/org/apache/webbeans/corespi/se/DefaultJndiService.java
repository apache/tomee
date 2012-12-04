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
package org.apache.webbeans.corespi.se;

import java.util.HashMap;

import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.spi.JNDIService;

/**
 * SPI Implementation of the JNDIService.
 * This version doesn't operate on a real JNDI context!
 * Instead it will store the values to bind in a singleton Map.
 * This is intended for all environments where
 * the java:comp and java:app context canot be written to, e.g.
 * because they are only readonly (tomcat, resin, jetty, ...) or
 * don't exist at all (JDK standalone applications)
 *
 * @see org.apache.webbeans.corespi.ee.JNDIServiceEnterpriseImpl
 */
public class DefaultJndiService implements JNDIService
{

    private HashMap<String, Object> jndiContent = new HashMap<String, Object>(); 
    
    /** 
     * {@inheritDoc}
     */
    public void bind(String name, Object object) throws WebBeansException
    {
        jndiContent.put(name, object);
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String name, Class<? extends T> expectedClass) throws WebBeansException
    {
        return (T) jndiContent.get(name);
    }

    /** 
     * {@inheritDoc}
     */
    public void unbind(String name) throws WebBeansException
    {
        jndiContent.remove(name);
    }

    public <T> T lookup(String name, Class<? extends T> expectedClass)
    {
        return getObject(name, expectedClass);
    }

}
