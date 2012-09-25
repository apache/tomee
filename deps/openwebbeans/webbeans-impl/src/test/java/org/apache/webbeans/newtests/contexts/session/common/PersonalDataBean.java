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
package org.apache.webbeans.newtests.contexts.session.common;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.newtests.injection.circular.beans.CircularDependentScopedBean;
import org.apache.webbeans.test.component.event.normal.Transactional;

@Named("org.apache.webbeans.newtests.contexts.session.common.PersonalDataBean")
@SessionScoped
public class PersonalDataBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static boolean POST_CONSTRUCT = false;
    
    public static boolean PRE_DESTROY = false;

    private @Inject CircularDependentScopedBean dependentInstance;

    public PersonalDataBean()
    {
        
    }
    
    @Transactional
    public void business(){}
    
    @PostConstruct
    public void postConstruct()
    {
        POST_CONSTRUCT = true;
    }

    @PreDestroy
    public void preDestroy()
    {
        PRE_DESTROY = true;
    }
    
    public PersonalDataBean getInstance()
    {
        return this;
    }

    public CircularDependentScopedBean getDependentInstance()
    {
        return dependentInstance;
    }

    public void setDependentInstance(CircularDependentScopedBean dependentInstance)
    {
        this.dependentInstance = dependentInstance;
    }
}
