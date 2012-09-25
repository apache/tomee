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
package org.apache.webbeans.component.creation;


import org.apache.webbeans.component.AbstractOwbBean;

/**
 * Contract for {@link javax.enterprise.inject.spi.Bean} creation.
 * 
 * <p>
 * Common operations on ManagedBean, ProducerField and ProducerMethods.
 * </p>
 * 
 * @version $Rev$ $Date$
 *
 */
public interface BeanCreator<T>
{
    /**
     * MetaDataProvider. 
     */
    public enum MetaDataProvider
    {
        //Default meta-data is used
        DEFAULT,
        //Third party overriden meta-data will be used. 
        //It is overriden with event ProcessAnnotatedType
        THIRDPARTY
    }
    
    /**
     * Check conditions on creating bean instance.
     */
    public void checkCreateConditions();
    
    /**
     * Define serializable.
     */
    public void defineSerializable();
    
    /**
     * Define stereptypes.
     */
    public void defineStereoTypes();
    
    /**
     * Define api type.
     */
    public void defineApiType();
    
    /**
     * Returns metadata provider.
     * 
     * @return metadata provider
     */
    public MetaDataProvider getMetaDataProvider();
    
    /**
     * Sets meta-data provider.
     * 
     * @param metadataProvider metadata provider
     */
    public void setMetaDataProvider(MetaDataProvider metadataProvider);
        
    /**
     * Define scope type of the bean.
     * 
     * <p>
     * Check passivation related controls.
     * </p>
     */
    public void defineScopeType(String errorMessage, boolean allowLazyInit);
    
    /**
     * Defines qualifier.
     */
    public void defineQualifier();
    
    /**
     * Defines bean name.
     * 
     * @param defaultName default bean name
     */
    public void defineName(String defaultName);   
    
    /**
     * Gets bean.
     * 
     * @return bean instance
     */
    public AbstractOwbBean<T> getBean();    
}