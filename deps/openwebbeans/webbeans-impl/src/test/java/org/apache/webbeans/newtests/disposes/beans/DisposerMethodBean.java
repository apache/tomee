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
import javax.inject.Named;

import org.apache.webbeans.newtests.concepts.alternatives.common.Pencil;

@Named("org.apache.webbeans.newtests.disposes.beans.DisposerMethodBean")
public class DisposerMethodBean
{
    public static boolean OK = false;
    
    @Produces @Dependent @Named
    public DisposeModel produce(@New DisposeModel model)
    {
        model.setValue(true);
        
        return model;
    }
    
    
    public void dispose(@Disposes DisposeModel model, @New Pencil pencil)
    {
        OK = model.isValue();
    }
}
