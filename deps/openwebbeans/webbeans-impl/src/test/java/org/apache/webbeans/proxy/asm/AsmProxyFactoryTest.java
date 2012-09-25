/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.webbeans.proxy.asm;

import java.io.File;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class AsmProxyFactoryTest
    extends TestCase
{
    public void testGetProxyClass()
        throws Exception
    {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final Class proxyClass = AsmProxyFactory.getProxyClass(classLoader, Foo.class);

        proxyClass.getDeclaredConstructor();
        proxyClass.getDeclaredConstructor(File.class);
    }


    public static class Foo
    {
        private final File file;

        public Foo(File file)
        {
            this.file = file;
        }

        public Foo()
        {
            this.file = null;
        }
    }

    public static class Bar extends Foo
    {
        public Bar(File file)
        {
            super(file);
        }

        public Bar()
        {
        }
    }

    public static abstract class Baz extends Bar
    {

    }
}
