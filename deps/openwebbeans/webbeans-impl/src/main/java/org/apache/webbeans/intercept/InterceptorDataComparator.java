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

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;

public class InterceptorDataComparator implements Comparator<InterceptorData>
{
    private final WebBeansContext instance;

    public InterceptorDataComparator(WebBeansContext webBeansContext)
    {
        instance = webBeansContext;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(InterceptorData o1, InterceptorData o2)
    {
        if (o1.equals(o2))
        {
            return 0;
        }
        else
        {
            WebBeansInterceptor<?> interceptorFirst = (WebBeansInterceptor<?>) o1.getWebBeansInterceptor();
            WebBeansInterceptor<?> interceptorSecond = (WebBeansInterceptor<?>) o2.getWebBeansInterceptor();

            if (interceptorFirst == null && interceptorSecond == null)
            {
                return 0;
            }

            /* If either is an EJB-style interceptor (@Interceptors or ejb-jar.xml), it is higher priority */
            if (interceptorFirst == null)
            {
                return -1;
            }
            else if (interceptorSecond == null)
            {
                return 1;
            }
            else
            {
                Class<?> o1Clazz = interceptorFirst.getClazz();
                Class<?> o2Clazz = interceptorSecond.getClazz();

                return instance.getInterceptorsManager().compare(o1Clazz, o2Clazz);
            }

        }
    }

}
