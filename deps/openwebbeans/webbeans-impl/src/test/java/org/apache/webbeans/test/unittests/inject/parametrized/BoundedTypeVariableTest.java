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
package org.apache.webbeans.test.unittests.inject.parametrized;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.inject.parametrized.BoundedTypeVariableComponent;
import org.apache.webbeans.util.ClassUtil;
import org.junit.Assert;
import org.junit.Test;

public class BoundedTypeVariableTest extends TestContext
{
    public BoundedTypeVariableTest()
    {
        super(BoundedTypeVariableTest.class.getName());
    }

    @Test
    public void testBoundedTypeVariableTest()
    {
        try
        {
            Field field1 = BoundedTypeVariableComponent.class.getField("field1");
            Field field2 = BoundedTypeVariableComponent.class.getField("field2");
            Field field3 = BoundedTypeVariableComponent.class.getField("field3");
            Field field4 = BoundedTypeVariableComponent.class.getField("field4");
            Field field5 = BoundedTypeVariableComponent.class.getField("field5");
            Field field6 = BoundedTypeVariableComponent.class.getField("field6");
            Field field7 = BoundedTypeVariableComponent.class.getField("field7");
            
            ParameterizedType pt = (ParameterizedType)field1.getGenericType();
            Type argument = pt.getActualTypeArguments()[0];
            
            Assert.assertFalse(ClassUtil.isUnboundedTypeVariable(argument));
            
            pt = (ParameterizedType)field2.getGenericType();
            argument = pt.getActualTypeArguments()[0];
                       
            Assert.assertFalse(ClassUtil.isUnboundedTypeVariable(argument));
            
            pt = (ParameterizedType)field3.getGenericType();
            argument = pt.getActualTypeArguments()[0];
                       
            Assert.assertTrue(ClassUtil.isUnboundedTypeVariable(argument));
            
            pt = (ParameterizedType)field4.getGenericType();
            argument = pt.getActualTypeArguments()[0];
                       
            Assert.assertFalse(ClassUtil.isUnboundedTypeVariable(argument));
            
            argument = field5.getGenericType();
                       
            Assert.assertFalse(ClassUtil.isUnboundedTypeVariable(argument));

            Type[] arguments = ((ParameterizedType)field6.getGenericType()).getActualTypeArguments();
            
            for(Type arg : arguments)
            {
                Assert.assertTrue(ClassUtil.isUnboundedTypeVariable(arg));
            }
            
            pt = (ParameterizedType)field7.getGenericType();
            argument = pt.getActualTypeArguments()[0];
                       
            Assert.assertFalse(ClassUtil.isUnboundedTypeVariable(argument));
            
            
        }catch(Exception e)
        {
            fail("BoundedWildCardTest");
        }
        
    }
}
