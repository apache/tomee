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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.openejb.archive;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

public final class SimpleArchive {
    private SimpleArchive() {
        // no-op
    }

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "start.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(ASingleton.class);
    }

    @Startup
    @Singleton
    public static class ASingleton {
        public static boolean ok = false;

        @PostConstruct
        public void init() {
            ok = true;
        }
    }
}
