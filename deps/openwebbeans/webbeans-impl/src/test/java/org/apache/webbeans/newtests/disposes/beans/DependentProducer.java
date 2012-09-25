/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.disposes.beans;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;

import org.apache.webbeans.newtests.disposes.common.DependentModel;
import org.apache.webbeans.newtests.disposes.common.HttpHeader;

public class DependentProducer
{
    public static int disposerCount = 0;

    @Produces @Dependent @HttpHeader
    public static DependentModel dproduce()
    {
        System.out.println("produced DependentModel from DepenentProducer");
        return new DependentModel();
    }
    
    
    public static void ddispose(@Disposes @HttpHeader DependentModel model)
    {
        DependentProducer.disposerCount++;
        System.out.println("disposed DependentModel from DependentDisposer");
    }
}
