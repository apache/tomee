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
package org.superbiz.rest;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

@RequestScoped
public class WeatherGateway {

    private static final Logger LOGGER = Logger.getLogger(WeatherGateway.class.getName());

    @Timeout(50)
    @Fallback(fallbackMethod = "statusOfWeekByMetEireann")
    public String statusOfDayByAccuWeather(){
        return longProcessingTask();
    }

    public String statusOfWeekByMetEireann(){
        LOGGER.log(Level.WARNING, "MetEireann backup service has been requested due to AccuWeather timeout");
        return "Beautiful day";
    }

    private String longProcessingTask(){
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING,"AccuWeather task has been interrupted.");
        }
        return null;
    }
}
