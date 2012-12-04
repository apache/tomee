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
package org.apache.webbeans.intercept.webbeans;

import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.inject.InjectableField;
import org.apache.webbeans.inject.InjectableMethods;
import org.apache.webbeans.intercept.OwbInterceptor;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Defines the webbeans specific interceptors.
 * <p>
 * WebBeans interceotor classes has at least one {@link javax.interceptor.InterceptorBinding}
 * annotation. It can be defined on the class or method level at the component.
 * WebBeans interceptors are called after the EJB related interceptors are
 * called in the chain. Semantics of the interceptors are specified by the EJB
 * specificatin.
 * </p>
 * 
 * @version $Rev$ $Date$
 */
public class WebBeansInterceptor<T> extends AbstractOwbBean<T> implements OwbInterceptor<T>
{
    /** InterceptorBindingTypes exist on the interceptor class */
    private Map<Class<? extends Annotation>, Annotation> interceptorBindingSet = new HashMap<Class<? extends Annotation>, Annotation>();

    /** Interceptor class */
    private Class<?> clazz;

    /**Delegate Bean*/
    private AbstractInjectionTargetBean<T> delegateBean;
    private final WebBeansContext webBeansContext;

    public WebBeansInterceptor(AbstractInjectionTargetBean<T> delegateBean)
    {
        super(WebBeansType.INTERCEPTOR,delegateBean.getReturnType(), delegateBean.getWebBeansContext());
        
        this.delegateBean = delegateBean;
        clazz = getDelegate().getReturnType();

        webBeansContext = delegateBean.getWebBeansContext();
    }

    public AbstractOwbBean<T> getDelegate()
    {
        return delegateBean;
    }
    
    public AnnotatedType<T> getAnnotatedType()
    {
        return delegateBean.getAnnotatedType();
    }
    

    /**
     * Add new binding type to the interceptor.
     * 
     * @param binding interceptor binding annotation. class
     * @param annot binding type annotation
     */
    public void addInterceptorBinding(Class<? extends Annotation> binding, Annotation annot)
    {
        Method[] methods = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethods(binding);

        for (Method method : methods)
        {
            Class<?> clazz = method.getReturnType();
            if (clazz.isArray() || clazz.isAnnotation())
            {
                if (!AnnotationUtil.hasAnnotation(method.getAnnotations(), Nonbinding.class))
                {
                    throw new WebBeansConfigurationException("Interceptor definition class : " + getClazz().getName() + " @InterceptorBinding : "
                                                             + binding.getName()
                                                             + " must have @NonBinding valued members for its array-valued and annotation valued members");
                }
            }
        }

        interceptorBindingSet.put(binding, annot);
    }

    /**
     * Checks whether all of this interceptors binding types are present on the bean, with 
     * {@link Nonbinding} member values.
     * 
     * @param bindingTypes binding types of bean
     * @param annots binding types annots of bean
     * @return true if all binding types of this interceptor exist ow false
     */
    public boolean hasBinding(List<Class<? extends Annotation>> bindingTypes, List<Annotation> annots)
    {
        if (bindingTypes == null || annots == null)
        {
            return false;
        }
        if (bindingTypes.size() != annots.size())
        {
            return false;
        }
        if (bindingTypes.size() == 0)
        {
            return false;
        }

        /* This interceptor is enabled if all of its interceptor bindings are present on the bean */
        for (Annotation ann : getInterceptorBindings())
        {
            Class<? extends Annotation> bindingType = ann.annotationType();
            int index = bindingTypes.indexOf(bindingType);
            if (index < 0)
            {
                return false; /* at least one of this interceptors types is not in the beans bindingTypes */
            }

            if (!AnnotationUtil.isQualifierEqual(ann, annots.get(index)))

            {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Gets the interceptor class.
     * 
     * @return interceptor class
     */
    public Class<?> getClazz()
    {
        return clazz;
    }

    public Set<Interceptor<?>> getMetaInceptors()
    {
        Set<Interceptor<?>> set = new HashSet<Interceptor<?>>();

        Set<Annotation> keys = getInterceptorBindings();

        AnnotationManager annotationManager = webBeansContext.getAnnotationManager();

        for (Annotation key : keys)
        {
            Class<? extends Annotation> clazzAnnot = key.annotationType();
            Set<Annotation> declared = null;
            Annotation[] anns = null;

            if (webBeansContext.getBeanManagerImpl().hasInterceptorBindingType(clazzAnnot))
            {
                declared = webBeansContext.getBeanManagerImpl().getInterceptorBindingTypeMetaAnnotations(clazzAnnot);
                anns = new Annotation[declared.size()];
                anns = declared.toArray(anns);
            }

            else if (annotationManager.hasInterceptorBindingMetaAnnotation(clazzAnnot.getDeclaredAnnotations()))
            {
                anns = annotationManager.getInterceptorBindingMetaAnnotations(clazzAnnot.getDeclaredAnnotations());
            }

            /*
             * For example: @InterceptorBinding @Transactional @Action
             * public @interface ActionTransactional @ActionTransactional
             * @Production { }
             */

            if (anns != null && anns.length > 0)
            {
                // For example : @Transactional @Action Interceptor
                Set<Interceptor<?>> metas = webBeansContext.getWebBeansInterceptorConfig().findDeployedWebBeansInterceptor(anns, webBeansContext);
                set.addAll(metas);

                // For each @Transactional and @Action Interceptor
                for (Annotation ann : anns)
                {
                    Annotation[] simple = new Annotation[1];
                    simple[0] = ann;
                    metas = webBeansContext.getWebBeansInterceptorConfig().findDeployedWebBeansInterceptor(simple, webBeansContext);
                    set.addAll(metas);
                }

            }

        }

        return set;
    }

    public Set<Annotation> getInterceptorBindings()
    {
        Set<Annotation> set = new HashSet<Annotation>();
        Set<Class<? extends Annotation>> keySet = interceptorBindingSet.keySet();
        Iterator<Class<? extends Annotation>> itSet = keySet.iterator();

        while (itSet.hasNext())
        {
            set.add(interceptorBindingSet.get(itSet.next()));
        }

        return set;
    }

    private Method getMethod(InterceptionType type)
    {
        Method method = null;
        
        if(type.equals(InterceptionType.AROUND_INVOKE))
        {
            method = WebBeansUtil.checkAroundInvokeAnnotationCriterias(getClazz(),AroundInvoke.class);
        }

        else if(type.equals(InterceptionType.AROUND_TIMEOUT))
        {
            method = WebBeansUtil.checkAroundInvokeAnnotationCriterias(getClazz(),AroundTimeout.class);
        }
        
        else
        {
            Class<? extends Annotation> interceptorTypeAnnotationClazz =
                webBeansContext.getInterceptorUtil().getInterceptorAnnotationClazz(type);
            method = getWebBeansContext().getWebBeansUtil().checkCommonAnnotationCriterias(getClazz(),
                                                                                                     interceptorTypeAnnotationClazz,
                                                                                                     true);
        }
        
        return method;
    }

    
    @SuppressWarnings("unchecked")
    protected T createInstance(CreationalContext<T> creationalContext)
    {
        Context context = webBeansContext.getBeanManagerImpl().getContext(getScope());
        Object actualInstance = context.get((Bean<Object>) delegateBean, (CreationalContext<Object>)creationalContext);
        T proxy = (T) webBeansContext.getProxyFactory().createDependentScopedBeanProxy(delegateBean, actualInstance, creationalContext);
        
        return proxy;
    }

    public void setInjections(Object proxy, CreationalContext<?> creationalContext)
    {
        // Set injected fields
        ManagedBean<T> delegate = (ManagedBean<T>) delegateBean;

        Set<Field> injectedFields = delegate.getInjectedFromSuperFields();
        for (Field injectedField : injectedFields)
        {
            injectField(injectedField, proxy, creationalContext);
        }

        Set<Method> injectedMethods = delegate.getInjectedFromSuperMethods();
        for (Method injectedMethod : injectedMethods)
        {
            injectMethod(injectedMethod, proxy, creationalContext);
        }
        
        injectedFields = delegate.getInjectedFields();
        for (Field injectedField : injectedFields)
        {
            injectField(injectedField, proxy, creationalContext);            
        }
        

        injectedMethods = delegate.getInjectedMethods();
        for (Method injectedMethod : injectedMethods)
        {
            injectMethod(injectedMethod, proxy, creationalContext);            
        }        
    }
    
    private void injectField(Field field, Object instance, CreationalContext<?> creationalContext)
    {
        InjectableField f = new InjectableField(field, instance, delegateBean, creationalContext);
        f.doInjection();        
    }

    @SuppressWarnings("unchecked")
    private void injectMethod(Method method, Object instance, CreationalContext<?> creationalContext)
    {
        InjectableMethods m = new InjectableMethods(method, instance, delegateBean, creationalContext);
        m.doInjection();        
    }
    
    @Override
    public Set<Annotation> getQualifiers()
    {
        return delegateBean.getQualifiers();
    }

    @Override
    public String getName()
    {
        return delegateBean.getName();
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return delegateBean.getScope();
    }

    public Set<Type> getTypes()
    {
        return delegateBean.getTypes();
    }
    
    public Set<InjectionPoint> getInjectionPoints()
    {
        return delegateBean.getInjectionPoints();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "WebBeans Interceptor with class : " + "[" + clazz.getName() + "]";
    }

    @Override
    public boolean isNullable()
    {
        return delegateBean.isNullable();
    }

    @Override
    public boolean isSerializable()
    {
        return delegateBean.isSerializable();
    }

    @Override
    public Class<?> getBeanClass()
    {
        return delegateBean.getBeanClass();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return delegateBean.getStereotypes();
    }

    public Object intercept(InterceptionType type, T instance,InvocationContext ctx)
    {
        Method method = getMethod(type);
        try
        {
            method.invoke(instance, ctx);
        }
        catch (Exception e)
        {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new WebBeansException(e);
        }

        return null;
    }

    public boolean intercepts(InterceptionType type)
    {
        Method method = getMethod(type);

        return method != null ? true : false;
    }

    @Override
    public boolean isAlternative()
    {
        return delegateBean.isAlternative();
    }
    
    @Override
    public boolean isPassivationCapable()
    {
        return delegateBean.isPassivationCapable();
    }

    @Override
    public void validatePassivationDependencies()
    {
        delegateBean.validatePassivationDependencies();
    }    
    
    
}
