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

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.inject.InjectableField;
import org.apache.webbeans.inject.InjectableMethods;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;

import javax.decorator.Delegate;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/**
 * Defines decorators. It wraps the bean instance related
 * with decorator class. Actually, each decorator is an instance
 * of the {@link ManagedBean}.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> decorator type info
 */
public class WebBeansDecorator<T> extends AbstractInjectionTargetBean<T> implements OwbDecorator<T>
{
    /** Decorator class */
    private Class<?> clazz;

    /** Decorates api types */
    private Set<Type> decoratedTypes = new HashSet<Type>();

    /** Delegate field class type */
    protected Type delegateType;
    
    /** Delegate field bindings */
    protected Set<Annotation> delegateBindings = new HashSet<Annotation>();
    
    protected Field delegateField;

    /** Wrapped bean*/
    private AbstractInjectionTargetBean<T> wrappedBean;
    
    /**Custom Decorator*/
    private Decorator<T> customDecorator = null;

    private final Set<String> ignoredDecoratorInterfaces;
    
    /**
     * Creates a new decorator bean instance with the given wrapped bean and custom decorator bean.
     * @param wrappedBean wrapped bean instance
     * @param customDecorator custom decorator
     */
    public WebBeansDecorator(AbstractInjectionTargetBean<T> wrappedBean, Decorator<T> customDecorator)
    {
        super(WebBeansType.DECORATOR,wrappedBean.getReturnType(), wrappedBean.getWebBeansContext());
        this.wrappedBean = wrappedBean;
        this.customDecorator = customDecorator;
        ignoredDecoratorInterfaces = getIgnoredDecoratorInterfaces(wrappedBean);
        initDelegate();
    }

    /**
     * Creates a new decorator bean instance with the given wrapped bean.
     * @param wrappedBean wrapped bean instance
     */
    public WebBeansDecorator(AbstractInjectionTargetBean<T> wrappedBean)
    {
        super(WebBeansType.DECORATOR,wrappedBean.getReturnType(), wrappedBean.getWebBeansContext());
        
        this.wrappedBean = wrappedBean;
        clazz = wrappedBean.getReturnType();
        ignoredDecoratorInterfaces = getIgnoredDecoratorInterfaces(wrappedBean);

        init();
    }

    private static <T> Set<String> getIgnoredDecoratorInterfaces(AbstractInjectionTargetBean<T> wrappedBean)
    {
        Set<String> result = new HashSet<String>(wrappedBean.getWebBeansContext().getOpenWebBeansConfiguration().getIgnoredInterfaces());
        result.add(Serializable.class.getName());
        return result;
    }

    protected void init()
    {
        ClassUtil.setInterfaceTypeHierarchy(decoratedTypes, clazz);

        for (Iterator<Type> i = decoratedTypes.iterator(); i.hasNext(); )
        {
            Type t = i.next();
            if (t instanceof Class<?> && ignoredDecoratorInterfaces.contains(((Class) t).getName()))
            {
                i.remove();
            }
        }

        initDelegate();
    }

    protected void initDelegate()
    {
        Set<InjectionPoint> injectionPoints = getInjectionPoints();
        boolean found = false;
        InjectionPoint ipFound = null;
        for(InjectionPoint ip : injectionPoints)
        {
            if(ip.getAnnotated().isAnnotationPresent(Delegate.class))
            {
                if(!found)
                {
                    found = true;
                    ipFound = ip;                    
                }
                else
                {
                    throw new WebBeansConfigurationException("Decorators must have a one @Delegate injection point. " +
                            "But the decorator bean : " + toString() + " has more than one");
                }
            }            
        }
        
        
        if(ipFound == null)
        {
            throw new WebBeansConfigurationException("Decorators must have a one @Delegate injection point." +
                    "But the decorator bean : " + toString() + " has none");
        }
        
        if(!(ipFound.getMember() instanceof Constructor))
        {
            AnnotatedElement element = (AnnotatedElement)ipFound.getMember();
            if(!element.isAnnotationPresent(Inject.class))
            {
                String message = "Error in decorator : "+ toString() + ". The delegate injection point must be an injected field, " +
                        "initializer method parameter or bean constructor method parameter.";

                throw new WebBeansConfigurationException(message);
            }                
        }
        
        initDelegateInternal(ipFound);
        
    }
    
    @Override
    public boolean isPassivationCapable()
    {
        return wrappedBean.isPassivationCapable();
    }

    private void initDelegateInternal(InjectionPoint ip)
    {
        if(customDecorator != null)
        {
            delegateType = customDecorator.getDelegateType();
            delegateBindings = customDecorator.getDelegateQualifiers();
        }
        else
        {
            delegateType = ip.getType();
            delegateBindings = ip.getQualifiers();
        }
                
        if(ip.getMember() instanceof Field)
        {
            delegateField = (Field)ip.getMember();
        }
        else
        {
            Field[] fields = ClassUtil.getFieldsWithType(wrappedBean.getWebBeansContext(), returnType, delegateType);
            if(fields.length == 0)
            {
                throw new WebBeansConfigurationException("Delegate injection field is not found for decorator : " + toString());
            }
            
            if(fields.length > 1)
            {
                throw new WebBeansConfigurationException("More than one delegate injection field is found for decorator : " + toString());
            }

            delegateField = fields[0];
        }
        
        Type fieldType = delegateField.getGenericType();

        for (Type decType : getDecoratedTypes())
        {
            if (!(ClassUtil.getClass(decType)).isAssignableFrom(ClassUtil.getClass(fieldType)))
            {
                throw new WebBeansConfigurationException("Decorator : " + toString() + " delegate attribute must implement all of the decorator decorated types" + 
                        ", but decorator type " + decType + " is not assignable from delegate type of " + fieldType);
            }
            else
            {
                if(ClassUtil.isParametrizedType(decType) && ClassUtil.isParametrizedType(fieldType))
                {                    
                    if(!fieldType.equals(decType))
                    {
                        throw new WebBeansConfigurationException("Decorator : " + toString() + " generic delegate attribute must be same with decorated type : " + decType);
                    }
                }
            }
        }
    }
    
    private boolean bindingMatchesAnnotations(Annotation bindingType, Set<Annotation> annotations)
    {

        for (Annotation annot : annotations)
        {
            if (AnnotationUtil.isQualifierEqual(annot, bindingType))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper method to check if any of a list of Types are assignable to the
     * delegate type.
     * 
     * @param apiTypes Set of apiTypes to check against the delegate type
     * @return true if one of the types is assignable to the delegate type
     */
    private boolean apiTypesMatchDelegateType(Set<Type> apiTypes)
    {
        boolean ok = false;
        for (Type apiType : apiTypes)
        {
            if (DecoratorResolverRules.compareType(getDelegateType(), apiType))
            {
                ok = true;
                break;
            }
        }
        
        if(ok) 
        {
            return true;
        }

        return false;
    }

    public boolean isDecoratorMatch(Set<Type> apiTypes, Set<Annotation> annotations)
    {

        if (!apiTypesMatchDelegateType(apiTypes))
        {
            return false;
        }

        for (Annotation bindingType : getDelegateQualifiers())
        {
            if (!bindingMatchesAnnotations(bindingType, annotations))
            {
                return false;
            }
        }

        return true;
    }

    public Set<Annotation> getDelegateQualifiers()
    {
        if(customDecorator != null)
        {
            return customDecorator.getDelegateQualifiers();
        }
        
        return delegateBindings;
    }

    public Type getDelegateType()
    {
        if(customDecorator != null)
        {
            return customDecorator.getDelegateType();
        }        
        
        return delegateType;
    }

    public void setDelegate(Object instance, Object delegate)
    {
        if (!delegateField.isAccessible())
        {
            getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(delegateField, true);
        }

        try
        {
            delegateField.set(instance, delegate);

        }
        catch (IllegalArgumentException e)
        {
            getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0007, instance.getClass().getName()), e);
            throw new WebBeansException(e);

        }
        catch (IllegalAccessException e)
        {
            getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0015, delegateField.getName(), instance.getClass().getName()), e);
        }

    }

    
    @SuppressWarnings("unchecked")    
    protected  T createInstance(CreationalContext<T> creationalContext)
    {
        if(customDecorator != null)
        {
            return customDecorator.create(creationalContext);
        }

        WebBeansContext webBeansContext = wrappedBean.getWebBeansContext();
        Context context = webBeansContext.getBeanManagerImpl().getContext(getScope());
        Object actualInstance = context.get((Bean<Object>) wrappedBean, (CreationalContext<Object>)creationalContext);
        T proxy = (T) webBeansContext.getProxyFactory().createDependentScopedBeanProxy(wrappedBean, actualInstance, creationalContext);
        
        return proxy;        
    }

    public void setInjections(Object proxy, CreationalContext<?> cretionalContext)
    {
        if(customDecorator != null)
        {
            Set<InjectionPoint> injections = customDecorator.getInjectionPoints();
            if(injections != null)
            {
                for(InjectionPoint ip : injections)
                {
                    if(!ip.isDelegate())
                    {
                        Member member = ip.getMember();
                        if(member instanceof Field)
                        {
                            injectField((Field)member  , proxy, cretionalContext);
                        }
                        if(member instanceof Method)
                        {
                            injectMethod((Method)member  , proxy, cretionalContext);
                        }                        
                    }
                }
            }
        }
        else
        {
            // Set injected fields
            ManagedBean<T> delegate = (ManagedBean<T>) wrappedBean;

            Set<Field> injectedFields = delegate.getInjectedFromSuperFields();
            for (Field injectedField : injectedFields)
            {
                boolean isDecorates = injectedField.isAnnotationPresent(Delegate.class);

                if (!isDecorates)
                {
                    injectField(injectedField, proxy, cretionalContext);
                }
            }
            
            Set<Method> injectedMethods = delegate.getInjectedFromSuperMethods();
            for (Method injectedMethod : injectedMethods)
            {
                injectMethod(injectedMethod, proxy, cretionalContext);
            }        

            injectedFields = delegate.getInjectedFields();
            for (Field injectedField : injectedFields)
            {
                boolean isDecorates = injectedField.isAnnotationPresent(Delegate.class);

                if (!isDecorates)
                {
                    injectField(injectedField, proxy, cretionalContext);
                }
            }
            
            injectedMethods = delegate.getInjectedMethods();
            for (Method injectedMethod : injectedMethods)
            {
                injectMethod(injectedMethod, proxy, cretionalContext);
            }                    
        }        
    }
    
    private void injectField(Field field, Object instance, CreationalContext<?> creationalContext)
    {
        InjectableField f = new InjectableField(field, instance, wrappedBean, creationalContext);
        f.doInjection();        
    }

    @SuppressWarnings("unchecked")
    private void injectMethod(Method method, Object instance, CreationalContext<?> creationalContext)
    {
        InjectableMethods m = new InjectableMethods(method, instance, wrappedBean, creationalContext);
        m.doInjection();        
    }
        
    @Override
    public Set<Annotation> getQualifiers()
    {
        if(customDecorator != null)
        {
            return customDecorator.getQualifiers();
        }
        
        return wrappedBean.getQualifiers();
    }

    @Override
    public String getName()
    {
        if(customDecorator != null)
        {
            return customDecorator.getName();
        }
        
        return wrappedBean.getName();
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        if(customDecorator != null)
        {
            return customDecorator.getScope();
        }
        
        return wrappedBean.getScope();
    }

    
    public Set<Type> getTypes()
    {
        if(customDecorator != null)
        {
            return customDecorator.getTypes();
        }
        
        return wrappedBean.getTypes();
    }

    @Override
    public boolean isNullable()
    {
        if(customDecorator != null)
        {
            return customDecorator.isNullable();
        }
        
        return wrappedBean.isNullable();
    }

    @Override
    public boolean isSerializable()
    {        
        return wrappedBean.isSerializable();
    }

    public Set<InjectionPoint> getInjectionPoints()
    {
        if(customDecorator != null)
        {
            return customDecorator.getInjectionPoints();
        }
        
        return wrappedBean.getInjectionPoints();
    }

    /**
     * @return the clazz
     */
    public Class<?> getClazz()
    {
        if(customDecorator != null)
        {
            return customDecorator.getBeanClass();
        }
        
        return clazz;
    }

    @Override
    public Class<?> getBeanClass()
    {
        if(customDecorator != null)
        {
            return customDecorator.getBeanClass();
        }
        
        return wrappedBean.getBeanClass();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        if(customDecorator != null)
        {
            return customDecorator.getStereotypes();
        }

        return wrappedBean.getStereotypes();
    }

    public Set<Type> getDecoratedTypes()
    {
        if(customDecorator != null)
        {
            return customDecorator.getDecoratedTypes();
        }

        return decoratedTypes;
    }

    @Override
    public boolean isAlternative()
    {
        if(customDecorator != null)
        {
            return customDecorator.isAlternative();
        }

        return wrappedBean.isAlternative();
    }

    @Override
    public void validatePassivationDependencies()
    {
        wrappedBean.validatePassivationDependencies();
    }

}
