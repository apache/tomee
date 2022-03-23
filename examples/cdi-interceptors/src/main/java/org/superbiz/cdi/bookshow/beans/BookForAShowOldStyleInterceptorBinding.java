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
package org.superbiz.cdi.bookshow.beans;

import org.superbiz.cdi.bookshow.interceptorbinding.Log;
import org.superbiz.cdi.bookshow.interceptors.BookForAShowLoggingInterceptor;

import jakarta.ejb.Stateful;
import jakarta.interceptor.Interceptors;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CDI supports binding an interceptor using @Interceptors
 * Not recommended though. Has its disadvantages
 * Cannot be disabled easily
 * Order dependent on how it is listed in class
 * Instead, create interceptor bindings using @InterceptorBinding and bind them
 * See {@link Log}, {@link BookForAShowOneInterceptorApplied}, {@link BookForAShowLoggingInterceptor}
 */
@Interceptors(BookForAShowLoggingInterceptor.class)
@Stateful
public class BookForAShowOldStyleInterceptorBinding implements Serializable {

    private static final long serialVersionUID = 6350400892234496909L;

    public List<String> getMoviesList() {
        List<String> moviesAvailable = new ArrayList<String>();
        moviesAvailable.add("KungFu Panda 2");
        moviesAvailable.add("Kings speech");
        return moviesAvailable;
    }

    public Integer getDiscountedPrice(int ticketPrice) {
        return ticketPrice - 50;
    }
    // assume more methods are present
}
