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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.ScannerService;

public final class WebBeansDecoratorConfig
{
    private static Logger logger = WebBeansLoggerFacade.getLogger(WebBeansDecoratorConfig.class);

    private WebBeansDecoratorConfig()
    {

    }

    public static <T> void configureDecoratorClass(AbstractInjectionTargetBean<T> delegate)
    {
        if(delegate.getScope() != Dependent.class)
        {
            if(logger.isLoggable(Level.WARNING))
            {
                logger.log(Level.WARNING, OWBLogConst.WARN_0005_1, delegate.getBeanClass().getName());
            }
        }
        
        if(delegate.getName() != null)
        {
            if(logger.isLoggable(Level.WARNING))
            {
                logger.log(Level.WARNING, OWBLogConst.WARN_0005_2, delegate.getBeanClass().getName());
            }
        }       
        
        if(delegate.isAlternative())
        {
            if(logger.isLoggable(Level.WARNING))
            {
                logger.log(Level.WARNING, OWBLogConst.WARN_0005_3, delegate.getBeanClass().getName());
            }                
        }            


        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "Configuring decorator class : [{0}]", delegate.getReturnType());
        }
        WebBeansDecorator<T> decorator = new WebBeansDecorator<T>(delegate);
        delegate.getWebBeansContext().getBeanManagerImpl().addDecorator(decorator);
    }

    public static void configureDecorators(AbstractInjectionTargetBean<?> component)
    {
        if (!component.getDecoratorStack().isEmpty())
        {
            // only define decorator stack once!
            return;
        }
        
        Set<Annotation> qualifiers = component.getQualifiers();
        Annotation[] anns = new Annotation[qualifiers.size()];
        anns = qualifiers.toArray(anns);

        List<Decorator<?>> decoratorList = component.getWebBeansContext().getBeanManagerImpl().resolveDecorators(component.getTypes(), anns);
        
        if(decoratorList != null && !decoratorList.isEmpty())
        {
            DecoratorUtil.checkManagedBeanDecoratorConditions(component, decoratorList);
            Iterator<Decorator<?>> itList = decoratorList.iterator();

            while (itList.hasNext())
            {
                WebBeansDecorator<?> decorator = (WebBeansDecorator<?>) itList.next();            
                component.getDecoratorStack().add(decorator);            
            }            
            filterDecoratorsPerBDA(component,component.getDecoratorStack());
        }
    }
    
    private static void filterDecoratorsPerBDA(AbstractInjectionTargetBean<?> component, List<Decorator<?>> stack)
    {

        ScannerService scannerService = component.getWebBeansContext().getScannerService();
        if (!scannerService.isBDABeansXmlScanningEnabled())
        {
            return;
        }
        BDABeansXmlScanner beansXMLScanner = scannerService.getBDABeansXmlScanner();
        String beanBDABeansXML = beansXMLScanner.getBeansXml(component.getBeanClass());
        Set<Class<?>> definedDecorators = beansXMLScanner.getDecorators(beanBDABeansXML);

        WebBeansDecorator<?> dec;

        if (stack != null && stack.size() > 0)
        {
            Iterator<Decorator<?>> it = stack.iterator();
            while (it.hasNext())
            {
                dec = (WebBeansDecorator<?>) it.next();
                if (!definedDecorators.contains(dec.getClazz()))
                {
                    it.remove();
                }
            }
        }
    }
    
    public static List<Object> getDecoratorStack(InjectionTargetBean<?> component, Object instance, 
            Object delegate, CreationalContextImpl<?> ownerCreationalContext)
    {
        // we need to synchronize on the instance to prevent
        // creating the decorators too often
        synchronized(instance)
        {
            List<Object> decoratorStack = new ArrayList<Object>();
            List<Decorator<?>> decoratorList = component.getDecoratorStack();
            Iterator<Decorator<?>> itList = decoratorList.iterator();
            BeanManager manager = component.getWebBeansContext().getBeanManagerImpl();
            while (itList.hasNext())
            {
                Object decoratorInstance ;
                WebBeansDecorator<Object> decorator = (WebBeansDecorator<Object>) itList.next();
                decoratorInstance = ownerCreationalContext.getDependentDecorator(instance, decorator);
                if(decoratorInstance == null)
                {
                    decoratorInstance = manager.getReference(decorator, decorator.getBeanClass(), ownerCreationalContext);

                    decorator.setInjections(decoratorInstance, ownerCreationalContext);
                    decorator.setDelegate(decoratorInstance, delegate);

                    ownerCreationalContext.addDependent(instance, decorator, decoratorInstance);
                }

                decoratorStack.add(decoratorInstance);
            }

            return decoratorStack;
        }
    }

    public static Set<Decorator<?>> findDeployedWebBeansDecorator(BeanManagerImpl beanManagerImpl, Set<Type> apiType, Annotation... anns)
    {
        Set<Decorator<?>> set = new HashSet<Decorator<?>>();

        Iterator<Decorator<?>> it = Collections.unmodifiableSet(beanManagerImpl.getDecorators()).iterator();
        WebBeansDecorator<?> decorator = null;

        List<Class<? extends Annotation>> bindingTypes = new ArrayList<Class<? extends Annotation>>();
        Set<Annotation> listAnnot = new HashSet<Annotation>();
        for (Annotation ann : anns)
        {
            bindingTypes.add(ann.annotationType());
            listAnnot.add(ann);
        }

        if (listAnnot.isEmpty())
        {
            listAnnot.add(new DefaultLiteral());
        }

        while (it.hasNext())
        {
            decorator = (WebBeansDecorator<?>) it.next();

            if (decorator.isDecoratorMatch(apiType, listAnnot))
            {
                set.add(decorator);
            }
        }

        return set;

    }

}
