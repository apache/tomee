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
package org.apache.webbeans.corespi;

import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.SingletonService;
import org.apache.webbeans.util.Asserts;

public class DefaultSingletonService implements SingletonService<WebBeansContext>
{
    /**
     * Keys --> ClassLoaders
     * Values --> WebBeansContext
     */
    private final Map<ClassLoader, WebBeansContext> singletonMap = new WeakHashMap<ClassLoader, WebBeansContext>();
    
    private final Map<Object, WeakReference<ClassLoader>> objectToClassLoaderMap = new IdentityHashMap<Object, WeakReference<ClassLoader>>();

 
    /**
     * Gets singleton instance for deployment.
     * @return signelton instance for this deployment
     */
    public WebBeansContext get(Object key)
    {
        assertClassLoaderKey(key);
        ClassLoader classLoader = (ClassLoader) key;
        synchronized (singletonMap)
        {
            //util.Track.sync(key);
            
            WebBeansContext webBeansContext = singletonMap.get(classLoader);
            //util.Track.get(key);

            if (webBeansContext == null)
            {
                webBeansContext = new WebBeansContext();
                singletonMap.put(classLoader, webBeansContext);
            }

            return webBeansContext;

        }
    }

    /**
     * Clear all deployment instances when the application is undeployed.
     * @param classLoader of the deployment
     */
    public void clearInstances(ClassLoader classLoader)
    {
        Asserts.assertNotNull(classLoader, "classloader is null");
        synchronized (singletonMap)
        {
            singletonMap.remove(classLoader);
        }
    }
    
    /**
     * Gets classloader with given singelton instance.
     * @param object singleton instance
     * @return the classloader that instance is created within
     */
    public ClassLoader getSingletonClassLoader(Object object)
    {
        Asserts.assertNotNull(object, "object is null");
        synchronized (objectToClassLoaderMap)
        {
            if(objectToClassLoaderMap.containsKey(object))
            {
                WeakReference<ClassLoader> current = objectToClassLoaderMap.get(object);
                if(current != null)
                {
                    return current.get();
                }                
            }              
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void clear(Object classLoader)
    {
        assertClassLoaderKey(classLoader);
        clearInstances((ClassLoader)classLoader);
    }

    /**
     * Assert that key is classloader instance.
     * @param key key
     */
    private void assertClassLoaderKey(Object key)
    {
        if(!(key instanceof ClassLoader))
        {
            throw new IllegalArgumentException("Key instance must be ClassLoader for using DefaultSingletonService");
        }
    }

}
