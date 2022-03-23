/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.spi.JsonbProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
// todo add a qualifier here so we isolate our instance from what applications would do
public class JsonbProducer {

    private static final Logger log = Logger.getLogger(MPJWTCDIExtension.class.getName());

    @Produces
    public Jsonb create() {
        return JsonbProvider.provider().create().build();
    }

    public void close(@Disposes final Jsonb jsonb) {
        try {
            jsonb.close();

        } catch (final Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
    }
}