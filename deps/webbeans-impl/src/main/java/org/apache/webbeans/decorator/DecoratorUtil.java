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
package org.apache.webbeans.decorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Decorator;

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;

/**
 * Decorator related utility class.
 * 
 * @version $Rev$ $Date$
 *
 */
public final class DecoratorUtil
{
    //Logger instance
    private static final Logger logger = WebBeansLoggerFacade.getLogger(DecoratorUtil.class);

    //Non-instantiate
    private DecoratorUtil()
    {
        //Empty
    }

    /**
     * Check decorator conditions.
     * @param decoratorClazz decorator class
     */
    public static void checkDecoratorConditions(Class<?> decoratorClazz)
    {       
        Asserts.assertNotNull(decoratorClazz, "Decorator class is null");
        
        Method[] methods = decoratorClazz.getDeclaredMethods();
        for(Method method : methods)
        {
            if(AnnotationUtil.hasMethodAnnotation(method, Produces.class))
            {
                throw new WebBeansConfigurationException("Decorator class : " + decoratorClazz + " can not have producer methods but it has one with name : " + method.getName());
            }
            
            if(AnnotationUtil.hasMethodParameterAnnotation(method, Observes.class))
            {
                throw new WebBeansConfigurationException("Decorator class : " + decoratorClazz + " can not have observer methods but it has one with name : " + method.getName());
            }
        }
        
        Set<Type> decoratorSet = new HashSet<Type>();
        ClassUtil.setInterfaceTypeHierarchy(decoratorSet, decoratorClazz);
        
        //Per section 8.1 do no consider Serializable a decorated type
        if(decoratorSet.contains(java.io.Serializable.class))
        {
            decoratorSet.remove(java.io.Serializable.class);
        }
        
    }
   
    public static void checkManagedBeanDecoratorConditions(AbstractInjectionTargetBean<?> bean, List<Decorator<?>> decoratorList)
    {
        Asserts.assertNotNull("bean", "bean parameter can not be null");

        Set<Annotation> annSet = bean.getQualifiers();
        Annotation[] anns = new Annotation[annSet.size()];
        anns = annSet.toArray(anns);

        if(decoratorList == null)
        {
            decoratorList = bean.getWebBeansContext().getBeanManagerImpl().resolveDecorators(bean.getTypes(), anns);
        }
        if (!decoratorList.isEmpty())
        {
            Class<?> clazz = bean.getReturnType();
            if (ClassUtil.isFinal(clazz.getModifiers()))
            {
                throw new WebBeansConfigurationException("Bean : " + bean.getReturnType().getName() + " can not be declared final, because it has one or more decorators");
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods)
            {
                int modifiers = method.getModifiers();
                if (!method.isSynthetic() && !method.isBridge() && !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers) && ClassUtil.isFinal(modifiers))
                {
                    // Check decorator implements this
                    Iterator<Decorator<?>> itDecorator = decoratorList.iterator();
                    while (itDecorator.hasNext())
                    {
                        WebBeansDecorator<?> decorator = (WebBeansDecorator<?>) itDecorator.next();
                        Class<?> decClazz = decorator.getClazz();

                        try
                        {
                            if (decClazz.getMethod(method.getName(), method.getParameterTypes()) != null)
                            {
                                throw new WebBeansConfigurationException("Bean : " + bean.getReturnType().getName() + " can not define non-private, non-static, final method : "
                                                                         + method.getName() + ", because one of its decorators implements this method");
                            }

                        }
                        catch (SecurityException e)
                        {
                            logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0006, decClazz.getName(),method.getName(), e));
                            throw new WebBeansException(e);

                        }
                        catch (NoSuchMethodException e)
                        {
                            continue;
                        }

                    }
                }
            }
        }
    }

}
