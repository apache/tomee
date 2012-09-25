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
package org.apache.webbeans.test.component.producer.primitive;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.annotation.binding.Binding2;

public class PrimitiveProducer
{
    public PrimitiveProducer()
    {
        
    }

    @Produces
    @Binding1
    @Binding2
    @Dependent
    public Float getSalary()
    {
        return 56.7f;
    }
}
