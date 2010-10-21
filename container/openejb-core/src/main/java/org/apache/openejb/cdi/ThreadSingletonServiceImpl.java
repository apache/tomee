/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.cdi;

import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.exception.WebBeansException;

/**
 * @version $Rev:$ $Date:$
 */
public class ThreadSingletonServiceImpl implements ThreadSingletonService {

    private final ThreadLocal<OWBContext> contexts = new ThreadLocal<OWBContext>();
    private final ClassLoader classLoader;

    public ThreadSingletonServiceImpl(ClassLoader classLoader) {
        this.classLoader = classLoader;
        
    }

    @Override
    public void initialize(OWBContext owbContext) {
        Object old = contextEntered(owbContext);
        try {
            setConfiguration(OpenWebBeansConfiguration.getInstance());
        } finally {
            contextExited(old);
        }
    }

    //not sure what openejb will need
    private void setConfiguration(OpenWebBeansConfiguration configuration) {
//        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");

//        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, EjbContainerLifecycle.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.JNDI_SERVICE, NoopJndiService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, OsgiMetaDataScannerService.class.getName());
//        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, WebContextsService.class.getName());
//        configuration.setProperty(ELAdaptor.class.getName(), EL22Adaptor.class.getName());
    }

    @Override
    public Object contextEntered(OWBContext newOWBContext) {
        OWBContext oldContext = contexts.get();
        contexts.set(newOWBContext);
        return oldContext;
    }

    @Override
    public void contextExited(Object oldOWBContext) {
        if (oldOWBContext != null && !(oldOWBContext instanceof OWBContext)) throw new IllegalArgumentException("ThreadSingletonServiceImpl can only be used with OWBContext, not " + oldOWBContext.getClass().getName());
        contexts.set((OWBContext) oldOWBContext);
    }

    @Override
     public Object get(Object key, String singletonClassName) {
         OWBContext context = getContext();
         Object service = context.getSingletons().get(singletonClassName);
         if (service == null) {
             try {
                 Class clazz = classLoader.loadClass(singletonClassName);
                 service = clazz.newInstance();
             } catch (ClassNotFoundException e) {
                 throw new WebBeansException("Could not locate requested class " + singletonClassName + " in classloader " + classLoader, e);
             } catch (InstantiationException e) {
                 throw new WebBeansException("Could not create instance of class " + singletonClassName, e);
             } catch (IllegalAccessException e) {
                 throw new WebBeansException("Could not create instance of class " + singletonClassName, e);
             } catch (NoClassDefFoundError e) {
                 throw new WebBeansException("Could not locate requested class " + singletonClassName + " in classloader " + classLoader, e);
             }
             context.getSingletons().put(singletonClassName, service);
         }
         return service;
     }

     private OWBContext getContext() {
         OWBContext context = contexts.get();
         if (context == null) {
             throw new IllegalStateException("On a thread without an initialized context");
         }
         return context;
     }

     @Override
     public void clear(Object key) {
         getContext().getSingletons().clear();
     }

     @Override
     public boolean isExist(Object key, String singletonClassName) {
         return getContext().getSingletons().containsKey(singletonClassName);
     }

     @Override
     public Object getExist(Object key, String singletonClassName) {
         return getContext().getSingletons().get(singletonClassName);
     }

     @Override
     public Object getKey(Object singleton) {
         return null;
     }
}
