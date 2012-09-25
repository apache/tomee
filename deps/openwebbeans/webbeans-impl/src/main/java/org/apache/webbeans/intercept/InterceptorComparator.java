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
package org.apache.webbeans.intercept;

import java.util.Comparator;

import javax.enterprise.inject.spi.Interceptor;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;

public class InterceptorComparator<T> implements Comparator<Interceptor<T>>
{

    private final WebBeansContext webBeansContext;

    public InterceptorComparator(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    public int compare(Interceptor<T> o1, Interceptor<T> o2)
    {
        WebBeansInterceptor<T> src = (WebBeansInterceptor<T>) o1;
        WebBeansInterceptor<T> target = (WebBeansInterceptor<T>) o2;

        if (o1.equals(o2))
        {
            return 0;
        }
        else
        {
            Class<?> o1Clazz = src.getClazz();
            Class<?> o2Clazz = target.getClazz();

            return webBeansContext.getInterceptorsManager().compare(o1Clazz, o2Clazz);

        }
    }

}
