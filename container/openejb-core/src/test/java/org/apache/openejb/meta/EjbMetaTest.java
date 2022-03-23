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

import jakarta.ejb.EJB;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version $Rev$ $Date$
 */
@RunWith(MetaRunner.class)
public class EjbMetaTest {

    @MetaTest(expected = ExpectedBean.class, actual = ActualBean.class)
    public void test() {
    }


    @EJB(name = "orange", beanInterface = Bean.class)
    @Metatype
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Orange {
    }

    @Metatype
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Yellow {
        public static class $ {

            @Yellow
            @EJB(name = "yellow")
            public void method() {
            }
        }
    }

    @Metatype
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Red {
        public static class $ {

            @Red
            @EJB(name = "red")
            public Object field;
        }
    }

    /**
     * Standard bean
     */
    @EJB(name = "orange", beanInterface = Bean.class)
    public static class ExpectedBean implements Bean {

        @EJB(name = "red")
        private Bean red;

        @EJB(name = "yellow")
        public void setYellow(final Bean yellow) {

        }
    }

    /**
     * Meta bean
     */
    @Orange
    public static class ActualBean implements Bean {

        @EJB(name = "red")
        private Bean red;

        @EJB(name = "yellow")
        public void setYellow(final Bean yellow) {
        }
    }


    public static interface Bean {
    }

}