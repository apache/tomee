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
package org.apache.tomee.microprofile.tck.metrics;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.Arrays;

/**
 * Metrics TCK expect the deployed test archives to be in the root context. In here, we just set the RestAssured path
 * so the test archives are not required to be deployed in the / context root.
 */
public class MicroProfileMetricsTCKObserver {
    public void AfterDeploy(@Observes final AfterDeploy afterDeploy) {
        RestAssured.filters(Arrays.asList(new SingleRequestFilter()));
        RestAssured.basePath = "microprofile-metrics";
        System.setProperty("context.root", "");
    }

    /*
     * This forces a very short wait before calls to the /metrics endpoint. The Tomcat response is sent to the client
     * ever-so-slightly before the metrics valve finishes completing. This ensures that the metrics are updated before
     * the prometheus scrape takes place.
     */
    private static class SingleRequestFilter implements Filter {
        @Override
        public Response filter(final FilterableRequestSpecification filterableRequestSpecification, final FilterableResponseSpecification filterableResponseSpecification, final FilterContext filterContext) {
            try {
                if (filterableRequestSpecification.getURI().contains("/microprofile-metrics/metrics")) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return filterContext.next(filterableRequestSpecification, filterableResponseSpecification);
        }
    }
}
