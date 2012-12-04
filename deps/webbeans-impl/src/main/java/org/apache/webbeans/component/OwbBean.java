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
package org.apache.webbeans.component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.*;
import org.apache.webbeans.config.WebBeansContext;

/**
 * OWB specific extension of the {@link Bean} interface.
 * It is used internally. Do not use it. Instead use {@link AbstractOwbBean}
 * for extension.
 * 
 * @version $Rev: 1363092 $ $Date: 2012-07-18 22:15:55 +0200 (mer., 18 juil. 2012) $
 * <T> bean class
 */
public interface OwbBean<T> extends Bean<T>
{
    /**
     * Sets bean scope type annotation.
     * 
     * @param scopeType bean scope type annotation
     */
    public void setImplScopeType(Annotation scopeType);    

    /**
     * Returns bean type.
     * 
     * @return webbeans type
     * @see WebBeansType
     */
    public WebBeansType getWebBeansType();
    
    /**
     * Create an instance.
     * @param creationalContext creaitonal context
     * @return instance
     */
    public T createNewInstance(CreationalContext<T> creationalContext);

    /**
     * Destroys instance.
     * @param instance instance
     * @param creationalContext creational
     */
    public void destroyCreatedInstance(T instance, CreationalContext<T> creationalContext);
    
    /**
     * Adds qualifier.
     * 
     * @param qualifier bean qualifier
     */
    public void addQualifier(Annotation qualifier);
    
    /**
     * Returns true if bean is capable of
     * serializable, false otherwise.
     * 
     * @return true if bean is serializable
     */
    public boolean isSerializable();    

    /**
     * Adds new stereotype annotation.
     * 
     * @param stereoType stereotype annotation
     */
    public void addStereoType(Annotation stereoType);

    /**
     * Adds new api type.
     * 
     * @param apiType api type
     */
    public void addApiType(Class<?> apiType);
    
    /**
     * Adds new injection point.
     * 
     * @param injectionPoint injection point
     */
    public void addInjectionPoint(InjectionPoint injectionPoint);

    /**
     * Gets stereotypes annotations.
     */
    public Set<Annotation> getOwbStereotypes();

    /**
     * Sets name of the bean.
     * 
     * @param name bean name
     */
    public void setName(String name);
    
    /**
     * Gets injection points for given member.
     * <p>
     * For example, if member is field, it gets all
     * injected field's injection points of bean.
     * </p>
     * @param member java member
     * @return injection points for given member
     */
    public List<InjectionPoint> getInjectionPoint(Member member);

    /**
     * Returns bean class type
     * @return bean class type
     */
    public Class<T> getReturnType();

    /**
     * Sets serializable flag.
     * @param serializable flag
     */
    public void setSerializable(boolean serializable);

    /**
     * Set nullable flag.
     * @param nullable flag
     */
    public void setNullable(boolean nullable);
    
    /**
     * Set specialized flag.
     * @param specialized flag
     */
    public void setSpecializedBean(boolean specialized);
    
    /**
     * Returns true if bean is a specialized bean, false otherwise.
     * @return true if bean is a specialized bean
     */
    public boolean isSpecializedBean();
    
    /**
     * Set enableed flag.
     * @param enabled flag
     */
    public void setEnabled(boolean enabled);    
    
    /**
     * Bean is enabled or not.
     * @return true if enabled
     */    
    public boolean isEnabled();
    
    /**
     * Gets id of the bean.
     * @return id of the bean
     */
    public String getId();
    
    /**
     * True if passivation capable false otherwise.
     * @return true if this bean is passivation capable
     */
    public boolean isPassivationCapable();
    
    /**
     * This determines if this bean is really a dependent bean,
     * and as such always creats a freshl instance for each
     * InjectionPoint. A BeanManagerBean is e.g. not a dependent bean.
     * @return <code>true</code> if this is a dependent bean
     */
    public boolean isDependent();
    
    /**
     * If bean is passivation capable, it validate all of its dependencies.
     * @throws org.apache.webbeans.exception.WebBeansConfigurationException if not satisfy passivation dependencies
     */
    public void validatePassivationDependencies();

    public WebBeansContext getWebBeansContext();
}
