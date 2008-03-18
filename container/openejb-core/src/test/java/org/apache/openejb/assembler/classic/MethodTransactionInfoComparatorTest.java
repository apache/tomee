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

/**
 * @version $Rev$ $Date$
 */
public class MethodTransactionInfoComparatorTest extends TestCase {

    public void testOrdering() throws Exception {

        ArrayList<MethodTransactionInfo> expected = new ArrayList<MethodTransactionInfo>();

        MethodTransactionInfo info = null;
        MethodInfo method = null;

        // Package Interceptors (aka Default Interceptors) ///////////////
        info = add(expected, new MethodTransactionInfo());
        info.transAttribute = "Required";
        method = new MethodInfo();
        method.ejbName = "*";
        method.className = "*";
        info.methods.add(method);

        // Class Interceptors ////////////////////////////////////////////
        info = add(expected, new MethodTransactionInfo());
        info.transAttribute = "Required";
        method = new MethodInfo();
        method.ejbName = "PingEJB";
        method.className = "*";
        method.methodName = "*";
        info.methods.add(method);

        // Method Interceptors (no params) ///////////////////////////////
        info = add(expected, new MethodTransactionInfo());
        info.transAttribute = "Required";
        method = new MethodInfo();
        method.ejbName = "PingEJB";
        method.className = "*";
        method.methodName = "ping";
        info.methods.add(method);

        // Method Interceptors (params)    ///////////////////////////////
        info = add(expected, new MethodTransactionInfo());
        info.transAttribute = "Required";
        method = new MethodInfo();
        method.ejbName = "PingEJB";
        method.className = "*";
        method.methodName = "ping";
        method.methodParams = new ArrayList<String>();
        method.methodParams.add("java.lang.String");
        info.methods.add(method);

        ArrayList<MethodTransactionInfo> actual = new ArrayList<MethodTransactionInfo>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual, new MethodTransactionBuilder.MethodTransactionComparator());

        for (int i = 0; i < actual.size(); i++) {
            MethodTransactionInfo a = actual.get(i);
            MethodTransactionInfo e = expected.get(i);
            assertSame(e, a);
        }
    }


    private MethodTransactionInfo add(List list, MethodTransactionInfo bindingInfo) {
        list.add(bindingInfo);
        return bindingInfo;
    }
}
