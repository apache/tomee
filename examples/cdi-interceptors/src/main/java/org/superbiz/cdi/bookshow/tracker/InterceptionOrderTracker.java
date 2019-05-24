/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cdi.bookshow.tracker;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for the test.
 * Keeps track of methods intercepted during one testXXX run
 * Keeps track of interceptors applied during one textXXX run
 */
public class InterceptionOrderTracker {

    /*
     * Contains method names that were intercepted by the interceptors
     */
    private static List<String> methodsInterceptedList = new ArrayList<String>();
    /*
     * Contains the name of the interceptor class that intercepted a method
     */
    private static List<String> interceptedByList = new ArrayList<String>();

    public static List<String> getInterceptedByList() {
        return interceptedByList;
    }

    public static void setInterceptedByList(List<String> interceptedByList) {
        InterceptionOrderTracker.interceptedByList = interceptedByList;
    }

    public static List<String> getMethodsInterceptedList() {
        return methodsInterceptedList;
    }
}
