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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorBindingInfoComparatorTest extends TestCase {
    private ArrayList<InterceptorBindingInfo> expected;

    public void testHighLevelOrdering() throws Exception {

        ArrayList<InterceptorBindingInfo> expected = new ArrayList<InterceptorBindingInfo>();

        InterceptorBindingInfo info = null;

        // Package Interceptors (aka Default Interceptors) ///////////////
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "*";
        info.interceptors.add("DefaultInterceptorUno");

        // Class Interceptors ////////////////////////////////////////////
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.interceptors.add("BeanOneClassInterceptorUno");

        // Method Interceptors (no params) ///////////////////////////////
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanTwo";
        info.interceptors.add("BeanTwoMethodInterceptorDos");
        info.method = new NamedMethodInfo();
        info.method.methodName = "ping";

        // Method Interceptors (params)    ///////////////////////////////
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanTwo";
        info.interceptors.add("BeanTwoMethodInterceptorDos");
        info.method = new NamedMethodInfo();
        info.method.methodName = "ping";
        info.method.methodParams = new ArrayList<String>();
        info.method.methodParams.add("java.lang.String");

        ArrayList<InterceptorBindingInfo> actual = new ArrayList<InterceptorBindingInfo>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual, new InterceptorBindingBuilder.IntercpetorBindingComparator());

        for (int i = 0; i < actual.size(); i++) {
            InterceptorBindingInfo a = actual.get(i);
            InterceptorBindingInfo e = expected.get(i);
            assertSame(e, a);
        }
    }

    public void testInnerLevelOrdering() {

        ArrayList<InterceptorBindingInfo> expected = new ArrayList<InterceptorBindingInfo>();
        ArrayList<InterceptorBindingInfo> actual = new ArrayList<InterceptorBindingInfo>(7);
        for (int i = 0; i < 7; i++) actual.add(null);

        InterceptorBindingInfo info = null;

        // Addition (class-level)
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.interceptors.add("BeanOneClassInterceptorUno");
        actual.set(3, info);

        // Addition (class-level)
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.interceptors.add("BeanOneClassInterceptorDos");
        actual.set(4, info);

        // Addition (class-level) + Exclusion (default level)
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.excludeDefaultInterceptors = true;
        info.interceptors.add("BeanOneClassInterceptorTres");
        actual.set(5, info);

        // Addition
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.interceptors.add("BeanOneClassInterceptorQuatro");
        actual.set(6, info);

        // Exclusion (class level) + Addition (class-level)
        // [this would be pointless, but good to test]
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.excludeClassInterceptors = true;
        info.interceptors.add("BeanOneClassInterceptorCinco");
        actual.set(2, info);

        // Exclusion (class and default levels)
        // [excluding both is top of the exclusions at this level]
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.excludeDefaultInterceptors = true;
        info.excludeClassInterceptors = true;
        actual.set(1, info);

        // Excplicit Order (class level and default level)
        // [trumps all bindings at this level and above]
        info = add(expected, new InterceptorBindingInfo());
        info.ejbName = "BeanOne";
        info.interceptorOrder.add("TotalOrderingDefaultInterceptor");
        actual.set(0, info);

        Collections.sort(actual, new InterceptorBindingBuilder.IntercpetorBindingComparator());

        for (int i = 0; i < actual.size(); i++) {
            InterceptorBindingInfo a = actual.get(i);
            InterceptorBindingInfo e = expected.get(i);
            assertSame(e, a);
        }
    }

    private InterceptorBindingInfo add(List list, InterceptorBindingInfo bindingInfo) {
        list.add(bindingInfo);
        return bindingInfo;
    }
}
