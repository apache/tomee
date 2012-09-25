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
package org.apache.webbeans.decorator;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.apache.webbeans.proxy.MethodHandler;

public class AbstractDecoratorMethodHandler implements MethodHandler, Serializable
{
  
    private static final long serialVersionUID = 1L;

    public AbstractDecoratorMethodHandler()
    {
//        new Exception().fillInStackTrace().printStackTrace();
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        final Method proceed = proxy.getClass().getMethod("_$$" + method.getName(), method.getParameterTypes());
        return proceed.invoke(proxy, args);
    }

    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
    {
        //Don't attempt to call it if the method doesn't exist
        if(proceed != null)
        {
            return proceed.invoke(self,args);
        }

        //Throw the exception so the DelegateHandler will continue the loop
        throw new NoSuchMethodException();
    }
    
    private  void writeObject(ObjectOutputStream s) throws IOException
    {
        //TODO: abstract decorator could not be serialized yet.
        s.writeLong(serialVersionUID);
    }    
    
    private  void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
    {
        //TODO: abstract decorator could not be serialized yet.
        if(s.readLong() == serialVersionUID) 
        {
        } 
        else 
        {
        }
    }    
    

}
