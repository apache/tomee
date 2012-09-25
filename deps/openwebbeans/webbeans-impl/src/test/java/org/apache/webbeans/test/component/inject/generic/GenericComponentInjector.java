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
package org.apache.webbeans.test.component.inject.generic;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.apache.webbeans.test.component.inject.parametrized.Persistent;
import org.apache.webbeans.test.component.inject.parametrized.PersistentSuper;

public class GenericComponentInjector<Y extends Persistent>
{
    private @Inject @Default GenericComponent<?> injection1;
    
    private @Inject @Default GenericComponent<? extends PersistentSuper> injection2;
    
    private @Inject @Default GenericComponent<Persistent> injection3;
 
    private @Inject @Default GenericComponent<Y> injection4;

    public GenericComponent<?> getInjection1()
    {
        return injection1;
    }

    public void setInjection1(GenericComponent<?> injection1)
    {
        this.injection1 = injection1;
    }

    public GenericComponent<? extends PersistentSuper> getInjection2()
    {
        return injection2;
    }

    public void setInjection2(GenericComponent<? extends PersistentSuper> injection2)
    {
        this.injection2 = injection2;
    }

    public GenericComponent<Persistent> getInjection3()
    {
        return injection3;
    }

    public void setInjection3(GenericComponent<Persistent> injection3)
    {
        this.injection3 = injection3;
    }

    public GenericComponent<Y> getInjection4()
    {
        return injection4;
    }

    public void setInjection4(GenericComponent<Y> injection4)
    {
        this.injection4 = injection4;
    }
    
    

}
