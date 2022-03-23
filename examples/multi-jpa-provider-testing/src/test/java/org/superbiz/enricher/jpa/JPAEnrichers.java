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
package org.superbiz.enricher.jpa;

import org.jboss.shrinkwrap.api.spec.WebArchive;

// use it with tomee remote adapter
// in embedded mode simply put all provider in appclassloader
// otherwise arquillian will be fooled by src/main/java
public final class JPAEnrichers {

    private JPAEnrichers() {
        // no-op
    }

    public static WebArchive addJPAProvider(final WebArchive war) {
        final String provider = System.getProperty("jakarta.persistence.provider");
        if (provider != null && provider.contains("hibernate")) {
            new HibernateEnricher().process(war);
        } else { // default
            new OpenJPAEnricher().process(war);
        }
        return war;
    }
}
