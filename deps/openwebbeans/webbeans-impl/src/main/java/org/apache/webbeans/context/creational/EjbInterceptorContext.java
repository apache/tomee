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
package org.apache.webbeans.context.creational;

import java.io.Serializable;


public class EjbInterceptorContext implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Object interceptorInstance;

    private Class<?> interceptorClass;

    public EjbInterceptorContext()
    {

    }

    /**
     * @return the interceptorClass
     */
    public Class<?> getInterceptorClass()
    {
        return interceptorClass;
    }

    /**
     * @param interceptorClass the interceptorClass to set
     */
    public void setInterceptorClass(Class<?> interceptorClass)
    {
        this.interceptorClass = interceptorClass;
    }

    /**
     * @return the interceptorInstance
     */
    public Object getInterceptorInstance()
    {
        return interceptorInstance;
    }

    /**
     * @param interceptorInstance the interceptorInstance to set
     */
    public void setInterceptorInstance(Object interceptorInstance)
    {
        this.interceptorInstance = interceptorInstance;
    }

    @Override
    public String toString() 
    {
        return "EjbInterceptorContext [interceptorClass=" + interceptorClass
            + ", interceptorInstance=" + interceptorInstance + "]";
    }
}
