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
package org.apache.webbeans.test.component;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.annotation.binding.SingletonBinding;
import org.apache.webbeans.test.component.service.ITyped2;

@SessionScoped
@SingletonBinding
@Named("singletonInstance")
public class Singleton implements Serializable
{
    private ITyped2<String, Object> typed2 = null;

    @Inject
    public void inject(@Binding1 ITyped2<String, Object> typed2)
    {
        this.typed2 = typed2;
    }

    public String logDebug()
    {
        return "debug";
    }

    public String logInfoo()
    {
        return "info";
    }

    @SuppressWarnings("unchecked")
    public ITyped2 getType()
    {
        return typed2;
    }

}
