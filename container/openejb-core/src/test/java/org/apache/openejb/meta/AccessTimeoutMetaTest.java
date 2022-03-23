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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.meta;

import org.junit.runner.RunWith;

import jakarta.ejb.AccessTimeout;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version $Rev$ $Date$
 */
@RunWith(MetaRunner.class)
public class AccessTimeoutMetaTest {

    @MetaTest(expected = ExpectedBean.class, actual = ActualBean.class)
    public void test() {
    }


    @AccessTimeout(value = 0)
    @Metatype
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AwaitNever {
    }

    @AccessTimeout(value = -1)
    @Metatype
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AwaitForever {
    }


    /**
     * Standard bean
     */
    @AccessTimeout(value = 0)
    public static class ExpectedBean implements Bean {

        @AccessTimeout(value = -1)
        public void method() {
        }
    }

    /**
     * Meta bean
     */
    @AwaitNever
    public static class ActualBean implements Bean {

        @AwaitForever
        public void method() {
        }
    }


    public static interface Bean {
    }

}