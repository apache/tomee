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
package org.apache.openejb.tck.microprofile.metrics;

import com.jayway.restassured.RestAssured;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Metrics TCK expect the deployed test archives to be in the root context. In here, we just set the RestAssured path
 * so the test archives are not required to be deployed in the / context root.
 */
public class MicroProfileMetricsTCKObserver {
    public void AfterDeploy(@Observes final AfterDeploy afterDeploy) {
        RestAssured.basePath = "microprofile-metrics";
    }
}
