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

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import jakarta.enterprise.context.Dependent;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class WeatherDayStatusFallbackHandler implements FallbackHandler<String> {

    private static final Logger LOGGER = Logger.getLogger(WeatherDayStatusFallbackHandler.class.getName());

    @Override
    public String handle(ExecutionContext executionContext) {
        LOGGER.log(Level.SEVERE, "Fallback was triggered due a fail");
        return "Hi, today is a sunny day!";
    }
}
