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
package org.apache.webbeans.ee.common.services;

import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.util.JNDIUtil;

/**
 * SPI Implementation of the JNDIService.
 * This version performs JNDI handling in J2EE environments where
 * the java:comp and java:app context can be written
 *
 * @see org.apache.webbeans.corespi.se.DefaultJndiService
 */
public final class JNDIServiceEnterpriseImpl implements JNDIService
{
    public JNDIServiceEnterpriseImpl()
    {
        
    }

    /** 
     * {@inheritDoc}
     */
    public void bind(String name, Object object) throws WebBeansException
    {
        JNDIUtil.bind(name, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void unbind(String name) throws WebBeansException
    {
        JNDIUtil.unbind(name);
    }
    
    /** 
     * {@inheritDoc}
     */
    public <T> T getObject(String name, Class<? extends T> expectedClass) throws WebBeansException
    {
        return JNDIUtil.lookup(name, expectedClass);
    }

    public <T> T lookup(String name, Class<? extends T> expectedClass)
    {
        return getObject(name, expectedClass);
    }
}
