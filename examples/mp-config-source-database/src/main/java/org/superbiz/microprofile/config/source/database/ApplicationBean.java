/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.microprofile.config.source.database;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class ApplicationBean {
    @Inject
    @ConfigProperty(name = "application.currency")
    private String applicationCurrrency;
    @Inject
    @ConfigProperty(name = "application.country")
    private String applicationCountry;

    public void init(@Observes @Initialized(ApplicationScoped.class) final Object init) {
        System.out.println("applicationCurrrency = " + applicationCurrrency);
        System.out.println("applicationCountry = " + applicationCountry);
    }

    public String getApplicationCurrrency() {
        return applicationCurrrency;
    }

    public String getApplicationCountry() {
        return applicationCountry;
    }
}
