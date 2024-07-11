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
package org.apache.tomee.microprofile.opentracing;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import org.apache.openejb.loader.SystemInstance;

public class MPOpenTracingCDIExtension implements Extension {

    /**
     * This event is used to add the TracerProducer bean to the application
     *
     * @param bbd         BeanDiscoveryEvent passed in by CDI
     * @param beanManager the BeanManager reference
     */
    public void observeBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd, final BeanManager beanManager) {
        final String mpScan = SystemInstance.get().getOptions().get("tomee.mp.scan", "none");

        if (mpScan.equals("none")) {
            SystemInstance.get().setProperty(MPOpenTracingCDIExtension.class.getName() + ".active", "false");
            return;
        }
        bbd.addAnnotatedType(beanManager.createAnnotatedType(TracerProducer.class), "TracerProducer");
    }

}