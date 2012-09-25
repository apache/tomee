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
package org.apache.webbeans.spi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;


import javax.enterprise.inject.spi.Bean;
import org.apache.webbeans.spi.api.ResourceReference;

/**
 * This service is responsible for injecting 
 * the Java EE injectable resources. Please
 * see the Section 5 of the Java EE specification.
 *
 */
public interface ResourceInjectionService
{
    /**
     * Container where OWB is deployed must responsible
     * for injecting Java EE resources defined by this managed bean.
     * This includes, field and method injections. How to inject
     * those resources are defined by Java EE specification.
     * <p>
     * This is only used for ManagedBean classes. It is not
     * for injection Session Beans or any other Java EE components.
     * Because those are already injected by the related container,
     * for example EJB Container, Web Container etc.
     * </p>
     * @param managedBeanInstance managed bean instance
     */
    public void injectJavaEEResources(Object managedBeanInstance);
    
    /**
     * Gets resource for the given resource reference.
     * <p>
     * This method is used for getting individual resource references that
     * are defined by the ManagedBean producer fields. 
     * For example;
     * <p>
     * <code>
     * {@link @Produces} &#064;MyPersistenceContext PersistenceContext EntityManager manager;
     * </code>
     * </p>
     * <p>
     * See section 3.5 of the JSR-299 specification.
     * </p> 
     * </p>
     * @param <T> resource type, &#064;EJB, &#064;Resource, &#064;WebServiceRef,
     *        &#064;PersistenceContext or &#064;PersistenceUnit
     * @param resourceReference
     * @return resource for the given resource reference
     */
    public <X,T extends Annotation> X getResourceReference(ResourceReference<X,T> resourceReference); 
    
    /**
     * Any clear functionality.
     * <p>
     * This is called by the container at shutdown. Services
     * may clear its caches or any other useful functionality.
     * </p>
     */
    public void clear();

    /**
     * delegation of serialization behavior
     */
    public <T> void writeExternal(Bean<T> bean, T actualResource, ObjectOutput out) throws IOException;

    /**
     * delegation of serialization behavior
     */
    public <T> T readExternal(Bean<T> bean, ObjectInput out) throws IOException,
            ClassNotFoundException;
        
}
