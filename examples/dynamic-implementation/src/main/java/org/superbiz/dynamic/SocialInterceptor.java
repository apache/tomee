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

package org.superbiz.dynamic;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class SocialInterceptor {

    @AroundInvoke
    public Object around(InvocationContext context) throws Exception {
        String mtd = context.getMethod().getName();
        String address;
        if (mtd.toLowerCase().contains("facebook")) {
            address = "http://www.facebook.com";
        } else if (mtd.toLowerCase().contains("twitter")) {
            address = "http://twitter.com";
        } else {
            address = "no website for you";
        }

        System.out.println("go on " + address);
        return context.proceed();
    }
}
