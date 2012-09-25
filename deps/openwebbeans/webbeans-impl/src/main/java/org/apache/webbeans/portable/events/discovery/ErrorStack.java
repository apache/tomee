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
package org.apache.webbeans.portable.events.discovery;

import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.webbeans.logger.WebBeansLoggerFacade;

/**
 * Error stack.
 * @version $Rev$ $Date$
 *
 */
public class ErrorStack
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(ErrorStack.class);
    
    private Stack<Throwable> errorStack = new Stack<Throwable>();
    
    public ErrorStack()
    {
        
    }
    
    public void pushError(Throwable e)
    {
        errorStack.addElement(e);
    }

    public Throwable[] popErrors()
    {
        Throwable[] list = new Throwable[errorStack.size()];
        list = errorStack.toArray(list);
        
        return list;
    }
    
    public void logErrors()
    {
        if(!errorStack.isEmpty())
        {
            Iterator<Throwable> it = errorStack.iterator();
            while(it.hasNext())
            {
                Throwable t = it.next();
                logger.log(Level.SEVERE, t.getMessage(), t);
            }
        }
    }
    
    public void clear()
    {
        errorStack.clear();
    }
    
    public boolean hasErrors()
    {
        return !errorStack.isEmpty();
    }
}
