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
package jug.routing;

import jug.domain.Subject;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// hack for OpenJPA
// it initializes lazily datasource (buildSchema) so simply call it here
// for hibernate it works without this hack
@ApplicationScoped
public class DataSourceInitializer {

    @PersistenceContext(unitName = "client1")
    private EntityManager client1;

    @PersistenceContext(unitName = "client2")
    private EntityManager client2;

    private boolean invoked = false;

    public void init() {
        if (invoked) {
            return;
        }

        client1.find(Subject.class, 0);
        client2.find(Subject.class, 0);
        invoked = true;
    }
}
