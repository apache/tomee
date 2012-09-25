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
package org.apache.webbeans.deployment.stereotype;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Stereotype model contract.
 * 
 * @version $Rev$ $Date$
 *
 */
public interface IStereoTypeModel
{
    /**
     * Returns name of the stereotype. As default,
     * its class name.
     * @return the name
     */
    public String getName();

    /**
     * Sets name.
     * @param name the name to set
     */
    public void setName(String name);

    /**
     * Gets default deployment type.
     * @return the defaultDeploymentType
     */
    public Annotation getDefaultDeploymentType();

    /**
     * Sets default deployment type.
     * @return the defaultScopeType
     */
    public Annotation getDefaultScopeType();

    /**
     * Returns set of interceptor binding that are
     * definen by the stereotype.
     * @return set of interceptor bindings.
     */
    public Set<Annotation> getInterceptorBindingTypes();

    /**
     * Returns a set of inherited stereotypes.
     * @return set of inherited stereotypes.
     */
    public Set<Annotation> getInheritedStereoTypes();

}
