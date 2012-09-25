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
package org.apache.webbeans.newtests.decorators.multiple;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public class Decorator1 implements IOutputProvider
{

    @Inject @Delegate IOutputProvider op;
    
    @Inject RequestStringBuilder rsb;

    public void init()
    {
        System.out.println("decorator created!");
    }

    public String getOutput()
    {
        rsb.addOutput("Decorator1\n");
        return op.getOutput();
    }

    public String trace() 
    {
        return "Decorator1/trace," + op.trace();
    }
    public String otherMethod() 
    {
        return "Decorator1/otherMethod," + op.otherMethod();
    }

    @Override
    public String getDelayedOutput() throws InterruptedException
    {
        return op.getDelayedOutput();
    }
}
