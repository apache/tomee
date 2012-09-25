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
package org.apache.webbeans.newtests.decorators.common;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;

@Decorator
public class ConversationDecorator implements Conversation
{
    private @Inject @Delegate Conversation conversation;
    
    public static boolean CALLED = false;

    @Override
    public void begin()
    {        
        CALLED = true;
    }

    @Override
    public void begin(String id)
    {
        
        
    }

    @Override
    public void end()
    {
        
        
    }

    @Override
    public String getId()
    {
        
        return null;
    }

    @Override
    public long getTimeout()
    {
        
        return 0;
    }

    @Override
    public boolean isTransient()
    {
        
        return false;
    }

    @Override
    public void setTimeout(long milliseconds)
    {
        
        
    }

}
