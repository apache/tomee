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

import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.inject.parametrized.Dao;
import org.apache.webbeans.test.component.inject.parametrized.UserDao;
import org.apache.webbeans.test.component.inject.parametrized.WithTypeVariable;
import org.apache.webbeans.util.ClassUtil;
import org.junit.Assert;
import org.junit.Test;

public class GenericClassTest extends TestContext
{
    public GenericClassTest()
    {
        super(GenericClassTest.class.getName());
    }

    @Test
    public void testGenericClasses()
    {
        try
        {
            Field t = Dao.class.getField("t");
            Field raw = Dao.class.getField("raw");
            Field check22 = Dao.class.getField("check22");
            Field check22Bound = Dao.class.getField("check22WithBound");
            Field check4 = WithTypeVariable.class.getField("check4");
            
            Assert.assertFalse(ClassUtil.isAssignable(t.getGenericType() , raw.getGenericType()));
            Assert.assertTrue(ClassUtil.isAssignable(t.getGenericType() , check4.getGenericType()));
            Assert.assertTrue(ClassUtil.isAssignable(t.getGenericType() , check22.getGenericType()));
            Assert.assertTrue(ClassUtil.isAssignable(t.getGenericType() , check22Bound.getGenericType()));
            
        }catch(Exception e)
        {
            e.printStackTrace();
            fail("testGenericClasses");
        }
    }
    
    @Test
    public void testGenericClasses2()
    {
        try
        {
            Field f1 = UserDao.class.getField("field1");
            Field f2 = UserDao.class.getField("field2");
            Field f3 = UserDao.class.getField("field3");
            Field f4 = UserDao.class.getField("field4");
            
            Assert.assertTrue(ClassUtil.isAssignable(f1.getGenericType() , f2.getGenericType()));
            Assert.assertTrue(ClassUtil.isAssignable(f1.getGenericType() , f3.getGenericType()));
            Assert.assertTrue(ClassUtil.isAssignable(f1.getGenericType() , f4.getGenericType()));
            
        }catch(Exception e)
        {
            e.printStackTrace();
            fail("testGenericClasses");
        }
    }
    
}
