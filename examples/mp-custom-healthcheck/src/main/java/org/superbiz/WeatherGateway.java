/*
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
package org.superbiz;

import java.util.concurrent.atomic.AtomicInteger;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class WeatherGateway {

    private static final AtomicInteger API_COUNTER_CALLS = new AtomicInteger();

    /**
     * This method simulates only one successful call to an
     * external service, after the first attempt, exceptions will be thrown.
     * @return A Weather message
     * @throws WeatherException After the first attempt.
     */
    public String statusOfDay() throws WeatherException {
        if(API_COUNTER_CALLS.addAndGet(1 ) <= 1) {
            return "Hi, today is a sunny day!";
        }
        throw new WeatherException("Weather Service is unavailable");
    }

    /**
     * This method simulates successful calls to an external service while {@see statusOfDay}
     * is not executed for the first time. Once {@see statusOfDay} is executed,
     * the variable {@see API_COUNTER_CALLS} will be incremented and this method start behaving
     * as if the external service unavailable.
     *
     * incrementing {@see API_COUNTER_CALLS}
     * @return Status of API
     * @throws WeatherException After the first execution of the {@see statusOfDay} method.
     */
    public WeatherApiStatus getApiStatus() throws WeatherException {
        if(API_COUNTER_CALLS.get() == 0) {
            WeatherApiStatus weatherApiStatus = new WeatherApiStatus();
            weatherApiStatus.setUrl("http://api.openweathermap.org/data/2.5/");
            weatherApiStatus.setVersion("2.5");
            weatherApiStatus.setMessage("Your account will become unavailable soon due to limitation of " +
                    "your subscription type. Remaining API calls are 1");
            return weatherApiStatus;
        }else{
            throw new WeatherException("Your account is temporary blocked due to exceeding of requests limitation of " +
                    "your subscription type. Please choose the proper subscription http://openweathermap.org/price");
        }
    }
}
