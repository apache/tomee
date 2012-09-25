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
package org.apache.webbeans.newtests.subclassing;

import org.junit.Test;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import junit.framework.Assert;


public class TestSubclassCreation
{

    @Test
    public void testSubclassCreation() throws Exception
    {
        Class<ClassToGetIntercepted> ctgiClass = createSubClass(ClassToGetIntercepted.class);
        ClassToGetIntercepted ctgi = ctgiClass.newInstance();
        
        Assert.assertNotNull(ctgi);
        String x = ctgi.getResult();
        Assert.assertEquals("X", x);

    }
    
    
    private <T> Class<T> createSubClass(Class<T> cls)
    throws NotFoundException, CannotCompileException
    {
        ClassPool pool = ClassPool.getDefault();
        
        // first we need to load the class via javassist
        CtClass origClass = pool.get(cls.getName());
        
        // we create a new subclass with a certain postfix
        CtClass subClass = pool.makeClass(cls.getName() + "_intcpted", origClass);
        
        overrideInterceptedMethods(subClass);
        
        // now let's get the real class from it
        @SuppressWarnings("unchecked")
        Class<T> interceptedClass = subClass.toClass();
        
        return interceptedClass;
    }


    private void overrideInterceptedMethods(CtClass subClass)
    throws CannotCompileException
    {
        CtMethod[] allMethods = subClass.getMethods();
        
        for (CtMethod method : allMethods)
        {
            overrideInterceptedMethod(subClass, method);
        }
    }


    private void overrideInterceptedMethod(CtClass subClass, CtMethod method)
    throws CannotCompileException {
        if (!method.visibleFrom(subClass) ||
            ((method.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) > 0))
        {
            // we cannot delegate non visible, final or static methods
            return;
        }
        
        String methodName = method.getLongName();
        if (methodName.startsWith("java.lang.Object."))
        {
            // we also have to skip methods we derive from 'java.lang.Object'
            return;
        }
        
        CtMethod overridenMethod = CtNewMethod.delegator(method, subClass);
        subClass.addMethod(overridenMethod);
        
        overridenMethod.insertBefore("{System.out.println(\"juuubel!:\");};");
    }
}
