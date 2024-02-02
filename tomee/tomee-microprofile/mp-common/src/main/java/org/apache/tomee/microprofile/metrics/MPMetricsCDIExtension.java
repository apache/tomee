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
package org.apache.tomee.microprofile.metrics;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

public class MPMetricsCDIExtension implements Extension {

//    private static final AtomicBoolean INIT = new AtomicBoolean(false);

    private void afterDeploymentValidation(@Observes final AfterDeploymentValidation avd, BeanManager bm) {

//        if (INIT.compareAndSet(false, true)) {
//            try {
//                final JmxRegistrar registrar = new JmxRegistrar();
//                registrar.init();
//
//            } catch (final IOException e) {
//                Logger.getInstance(LogCategory.OPENEJB, MPMetricsCDIExtension.class).error("Can't initialize Metrics Registrar: " + e.getMessage());
//            }
//        }
    }

}