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
package org.apache.webbeans.newtests.interceptors.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

/**
 * this class has no &#064;LifecycleBinding nor &#064;Interceptor.
 * Those will get added later via
 * an Extension on {@link javax.enterprise.inject.spi.ProcessAnnotatedType}.
 */
public class LifecycleInterceptorPat implements Serializable
{
    public static boolean POST_CONSTRUCT = false;
    
    public static boolean PRE_DESTROY = false;
    
    @PostConstruct
    public void postConstruct(InvocationContext context)
    {
        POST_CONSTRUCT = true;
        NotAnnotatedBean.PC = true;
    }
    
    @PreDestroy
    public void preDestroy(InvocationContext context)
    {
        PRE_DESTROY = true;
        NotAnnotatedBean.PC = true;
    }
}
