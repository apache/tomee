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

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version $Rev$ $Date$
 */
@RunWith(MetaRunner.class)
public class TransactionAttributeMetaTest {

    @MetaTest(expected = ExpectedBean.class, actual = ActualBean.class)
    public void test() {
    }


    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Metatype
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TxSupports {
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Metatype
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TxRequiresNew {
    }


    /**
     * Standard bean
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public static class ExpectedBean implements Bean {

        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public void method() {
        }
    }

    /**
     * Meta bean
     */
    @TxSupports
    public static class ActualBean implements Bean {

        @TxRequiresNew
        public void method() {
        }
    }


    public static interface Bean {
    }

}