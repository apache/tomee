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
package org.apache.webbeans.service;

import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.util.WebBeansUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Default implementation which delegates to the s{@link ServiceLoader} of Java 1.6 and
 * uses a fallback for Java 1.5
 */
public class DefaultLoaderService implements LoaderService
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(DefaultLoaderService.class);

    private static final boolean JAVA_6_AVAILABLE = isJava6();

    public <T> List<T> load(Class<T> serviceType)
    {
        return load(serviceType, WebBeansUtil.getCurrentClassLoader());
    }

    public <T> List<T> load(Class<T> serviceType, ClassLoader classLoader)
    {
        if(JAVA_6_AVAILABLE)
        {
            List<T> result = new ArrayList<T>();
            ServiceLoader<T> services = ServiceLoader.load(serviceType, classLoader);

            for (T service : services)
            {
                result.add(service);
            }

            return result;
        }

        return new ManualImplementationLoaderService<T>(serviceType, classLoader).loadServiceImplementations();
    }

    private static boolean isJava6()
    {
        try
        {
            ServiceLoader.class.getName();
            return true;
        }
        catch (NoClassDefFoundError error)
        {
            logger.info("Using Java 5 compatibility mode, because didn't find ServiceLoader: " + error);
            return false;
        }
    }
}
