/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.cdi;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class StartupOnCdiBeanTest {
    @Module
    public Beans beans() {
        return new Beans()
                .managedClass(InitMeASAP.class.getName())
                .managedClass(InitMeLater.class.getName());
    }

    @Inject
    private BeanManager bm;

    @Test
    public void run() throws InterruptedException {
        value = "too late";
        final InitMeASAP instance = InitMeASAP.class.cast(bm.getReference(bm.resolve(bm.getBeans(InitMeASAP.class)), InitMeASAP.class, bm.createCreationalContext(null)));
        final InitMeLater lazy = InitMeLater.class.cast(bm.getReference(bm.resolve(bm.getBeans(InitMeLater.class)), InitMeLater.class, bm.createCreationalContext(null)));
        assertEquals("boot", instance.getInit());
        assertEquals("too late", lazy.getInit());
    }

    public static String value = "boot";

    @Startup
    @ApplicationScoped
    public static class InitMeASAP {
        private String init;

        @PostConstruct
        public void setTrue() {
            init = value;
        }

        public String getInit() {
            return init;
        }
    }

    @ApplicationScoped
    public static class InitMeLater {
        private String init;

        @PostConstruct
        public void setTrue() {
            init = value;
        }

        @PreDestroy
        public void resetForDebug() {
            init = value;
        }

        public String getInit() {
            return init;
        }
    }
}
