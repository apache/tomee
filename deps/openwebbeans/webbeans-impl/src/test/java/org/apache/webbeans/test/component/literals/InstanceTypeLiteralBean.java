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
package org.apache.webbeans.test.component.literals;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Named;

@Named("literalBean")
public class InstanceTypeLiteralBean
{
    public static interface IOrder<T>{}
    
    public static class StringOrder implements IOrder<String>{}
    
    public static class IntegerOrder implements IOrder<Integer>{}

    private @Inject Instance<IOrder<?>> instance;
    
    public Instance<?> produce(int type)
    {
        if(type == 0)
        {
            return instance.select(new TypeLiteral<IntegerOrder>(){}, new Annotation[0]);
        }
        else 
        {
            return instance.select(new TypeLiteral<StringOrder>(){}, new Annotation[0]);
        }
    }
}
