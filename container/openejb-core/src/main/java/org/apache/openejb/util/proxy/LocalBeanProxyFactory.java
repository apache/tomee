/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util.proxy;

import org.apache.openejb.AppClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class LocalBeanProxyFactory {

    public static Object newProxyInstance(AppClassLoader cl, Class interfce, java.lang.reflect.InvocationHandler h) throws IllegalArgumentException {
        try {
            Class proxyCls = new LocalBeanProxyGeneratorImpl().createProxy(interfce, cl);
            Constructor constructor = proxyCls.getConstructor(java.lang.reflect.InvocationHandler.class);
            Object object = constructor.newInstance(h);
            return object;
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

}
