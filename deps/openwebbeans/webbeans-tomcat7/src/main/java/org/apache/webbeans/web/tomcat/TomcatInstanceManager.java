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
package org.apache.webbeans.web.tomcat;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.InstanceManager;

public class TomcatInstanceManager implements InstanceManager
{
    private static final Log log = LogFactory.getLog(TomcatInstanceManager.class);

    private InstanceManager processor;

    private ClassLoader loader;

    private Map<Object, Object> objects = new ConcurrentHashMap<Object, Object>();

    public TomcatInstanceManager(ClassLoader loader, InstanceManager processor)
    {
        this.processor = processor;
        this.loader = loader;
    }

    public void destroyInstance(Object instance) throws IllegalAccessException, InvocationTargetException
    {
        Object injectorInstance = objects.get(instance);
        if (injectorInstance != null)
        {
            try
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Destroying the OpenWebBeans injector instance");
                }
                TomcatUtil.destroy(injectorInstance, loader);
            }
            catch (Exception e)
            {
                log.error("Erros is occured while destroying the OpenWebBeans injector instance", e);
            }
        }
        this.processor.destroyInstance(instance);
    }

    public Object newInstance(String str) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException
    {
        // Creates a defaut instance
        Object object = this.processor.newInstance(str);

        // Inject dependencies
        inject(object);

        return object;
    }

    public void newInstance(Object object) throws IllegalAccessException, InvocationTargetException, NamingException
    {
        // Inject dependencies
        inject(object);
    }

    public Object newInstance(String str, ClassLoader cl) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException
    {
        // Creates a defaut instance
        Object object = this.processor.newInstance(str, cl);

        // Inject dependencies
        inject(object);

        return object;
    }

    private void inject(Object object)
    {
        try
        {
            if(log.isDebugEnabled())
            {
                log.debug("Injecting the dependencies for OpenWebBeans, " +
                          "instance : " + object);
            }

            Object injectorInstance = TomcatUtil.inject(object, loader);
            if (injectorInstance != null)
            {
                objects.put(object, injectorInstance);
            }
        }
        catch (Exception e)
        {
            log.error("Error is occured while injecting the OpenWebBeans " +
                      "dependencies for instance " + object,e);
        }
    }

}
