/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.openjpa.persistence.validation;

import java.lang.annotation.ElementType;

import javax.persistence.spi.LoadState;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Path.Node;

import org.apache.openjpa.persistence.OpenJPAPersistenceUtil;

/**
 * OpenJPA optimized TraversableResolver Default implementation/behavior asks all discovered providers -
 * javax.persistence.Persistence.getPersistenceUtil().isLoaded( traversableObject, traversableProperty.getName());
 * 
 * @see javax.validation.TraversableResolver
 */
public class TraversableResolverImpl implements TraversableResolver {

    public TraversableResolverImpl() {
    }

    /* (non-Javadoc) isReachable() is called by the Validator before accessing
     * a property for validation or for cascading.
     * 
     * @see javax.validation.TraversableResolver#isReachable(java.lang.Object,
     *      javax.validation.Path.Node, java.lang.Class, javax.validation.Path,
     *      java.lang.annotation.ElementType)
     */
    public boolean isReachable(Object traversableObject,
        Node traversableProperty, Class<?> rootBeanType,
        Path pathToTraversableObject, ElementType elementType) {

        /*
         * OpenJPA optimized version of the default provider implementation,
         * which doesn't ask all the providers on the classpath about the obj.
         */
        if (OpenJPAPersistenceUtil.isLoaded(traversableObject, traversableProperty.getName()) == LoadState.NOT_LOADED) {
            return false;
        } else {
            // LoadState.LOADED or LoadState.UNKNOWN
            return true;
        }
    }

    /* (non-Javadoc) isCascadable() is called by the Validator after 
     * isReachable() returns true and before a property marked with @Valid 
     * is cascaded.
     * 
     * @see javax.validation.TraversableResolver#isCascadable(java.lang.Object,
     *      javax.validation.Path.Node, java.lang.Class, javax.validation.Path,
     *      java.lang.annotation.ElementType)
     */
    public boolean isCascadable(Object traversableObject,
        Node traversableProperty, Class<?> rootBeanType,
        Path pathToTraversableObject, ElementType elementType) {

        // BV Spec Section 3.5.2 says to always return true for JPA ???
        return true;
    }
}
