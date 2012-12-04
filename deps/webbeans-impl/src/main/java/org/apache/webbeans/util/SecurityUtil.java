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
package org.apache.webbeans.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/** @deprecated  use SecurityService instead */
public class SecurityUtil
{

    private static final int METHOD_CLASS_GETDECLAREDMETHODS = 0x04;

    public static <T> Method[] doPrivilegedGetDeclaredMethods(Class<T> clazz)
    {
        if (System.getSecurityManager() == null)
        {
            return clazz.getDeclaredMethods();
        }

        Object obj = AccessController.doPrivileged(
                new PrivilegedActionForClass(clazz, null, METHOD_CLASS_GETDECLAREDMETHODS));
        return (Method[])obj;
    }

    protected static class PrivilegedActionForClass implements PrivilegedAction<Object>
    {
        private Class<?> clazz;

        protected PrivilegedActionForClass(Class<?> clazz, Object parameters, int method)
        {
            this.clazz = clazz;
        }

        public Object run()
        {
            try
            {
                return clazz.getDeclaredMethods();
            }
            catch (Exception exception)
            {
                return exception;
            }
        }

    }
}
