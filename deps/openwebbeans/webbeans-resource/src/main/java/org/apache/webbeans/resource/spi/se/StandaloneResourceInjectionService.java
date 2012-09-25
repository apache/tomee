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
package org.apache.webbeans.resource.spi.se;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.apache.webbeans.component.ResourceBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.FailOverService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.api.ResourceReference;
import org.apache.webbeans.util.AnnotationUtil;

public class StandaloneResourceInjectionService implements ResourceInjectionService
{
    /**
     * When ResourceProxyHandler deserialized, this will instruct owb to create a new actual instance, if
     * the actual resource is not serializable.
     */
    private static final String DUMMY_STRING = "owb.actual.resource.dummy";

    private final StandaloneResourceProcessor processor = StandaloneResourceProcessor.getProcessor();
    
    private static final Logger logger = WebBeansLoggerFacade.getLogger(StandaloneResourceInjectionService.class);

    private final WebBeansContext webBeansContext;

    /**
     * Cache the information if a certain class contains any EE resource at all
     */
    private final Map<Class<?>, Boolean> classContainsEEResources = new ConcurrentHashMap<Class<?>, Boolean>();

    public StandaloneResourceInjectionService(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    protected WebBeansContext getWebBeansContext()
    {
        return webBeansContext;
    }

    public <X, T extends Annotation> X getResourceReference(ResourceReference<X, T> resourceReference)
    {
        if(resourceReference.supports(Resource.class))
        {         
            Resource resource = resourceReference.getAnnotation(Resource.class);
            return processor.getResource(resource, resourceReference.getResourceType());
        }

        if(resourceReference.supports(WebServiceRef.class))
        {         
            WebServiceRef resource = resourceReference.getAnnotation(WebServiceRef.class);
            return processor.getWebServiceResource(resource, resourceReference.getResourceType());

        }
                
        if(resourceReference.supports(PersistenceContext.class))
        {
            PersistenceContext persistenceContext = resourceReference.getAnnotation(PersistenceContext.class);
            return processor.getEntityManager(persistenceContext, resourceReference.getResourceType());
        }
        
        if(resourceReference.supports(PersistenceUnit.class))
        {
            PersistenceUnit persistenceUnit = resourceReference.getAnnotation(PersistenceUnit.class);
            return processor.getEntityManagerFactory(persistenceUnit, resourceReference.getResourceType());
        }
        
        return null;
    }

    public void injectJavaEEResources(Object managedBeanInstance)
    {
        Class currentClass = managedBeanInstance.getClass();
        Boolean containsEeResource = classContainsEEResources.get(currentClass);
        if (containsEeResource != null && !containsEeResource)
        {
            // nothing to do it seems.
            return;
        }


        while (currentClass != null && !Object.class.getName().equals(currentClass.getName()))
        {
            Field[] fields = webBeansContext.getSecurityService().doPrivilegedGetDeclaredFields(currentClass);

            for(Field field : fields)
            {
                if(!field.isAnnotationPresent(Produces.class))
                {
                    if(!Modifier.isStatic(field.getModifiers()))
                    {
                        Annotation ann = AnnotationUtil.hasOwbInjectableResource(field.getDeclaredAnnotations());
                        if(ann != null)
                        {
                            @SuppressWarnings("unchecked")
                            ResourceReference<Object, ?> resourceRef = new ResourceReference(field.getDeclaringClass(), field.getName(), field.getType(), ann);
                            try
                            {
                                if(!field.isAccessible())
                                {
                                    webBeansContext.getSecurityService().doPrivilegedSetAccessible(field, true);
                                }
                                field.set(managedBeanInstance, getResourceReference(resourceRef));

                                containsEeResource = Boolean.TRUE;
                            }
                            catch(Exception e)
                            {
                                logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0025, e, field));
                                throw new WebBeansException(MessageFormat.format(WebBeansLoggerFacade.getTokenString(OWBLogConst.ERROR_0025), field), e);

                            }
                        }
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        if (containsEeResource == null)
        {
            containsEeResource = Boolean.FALSE;
        }

        classContainsEEResources.put(managedBeanInstance.getClass(), containsEeResource);
    }

    public void clear()
    {
        processor.clear();       
    }
    
    /**
     * delegation of serialization behavior
     */
    public <T> void writeExternal(Bean<T> bean, T actualResource, ObjectOutput out) throws IOException
    {
        // try fail over service to serialize the resource object
        FailOverService failoverService = webBeansContext.getService(FailOverService.class);
        if (failoverService != null)
        {
            Object ret = failoverService.handleResource(bean, actualResource, null, out);
            if (ret != FailOverService.NOT_HANDLED)
            {
                return;
            }
        }

        // default behavior
        if (actualResource instanceof Serializable)
        {
            // for remote ejb stub and other serializable resources
            out.writeObject(actualResource);
        }
        else
        {
            // otherwise, write a dummy string.
            out.writeObject(DUMMY_STRING);
        }

    }

    /**
     * delegation of serialization behavior
     */
    public <T> T readExternal(Bean<T> bean, ObjectInput in) throws IOException,
            ClassNotFoundException
    {
        T actualResource = null;
        // try fail over service to serialize the resource object
        FailOverService failoverService = webBeansContext.getService(FailOverService.class);
        if (failoverService != null)
        {
            actualResource = (T) failoverService.handleResource(bean, actualResource, in, null);
            if (actualResource != FailOverService.NOT_HANDLED)
            {
                return actualResource;
            }
        }

        // default behavior
        actualResource = (T) in.readObject();
        if (actualResource instanceof javax.rmi.CORBA.Stub)
        {
            // for remote ejb stub, reconnect after deserialization.
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
            ((javax.rmi.CORBA.Stub)actualResource).connect(orb);
        }
        else if (actualResource.equals(DUMMY_STRING))
        {
            actualResource = (T) ((ResourceBean)bean).getActualInstance();
        }
        return actualResource;
    }


}
