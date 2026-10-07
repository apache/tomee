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
package org.apache.openejb.arquillian.tests.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

/**
 * Reads the inserted item back through {@link SimpleItemRepository} after the inserting
 * transaction commits. The container catches and logs any exception a transactional observer
 * throws, so the outcome is recorded for the test to assert on.
 */
@ApplicationScoped
public class ItemInsertedObserver {

    @Inject
    private SimpleItemRepository repository;

    private String label;
    private Exception failure;

    public void onItemInserted(@Observes(during = TransactionPhase.AFTER_SUCCESS) final ItemInserted event) {
        try {
            label = repository.findById(event.id()).map(SimpleItem::getLabel).orElse(null);
        } catch (final Exception e) {
            failure = e;
        }
    }

    public String getLabel() {
        return label;
    }

    public Exception getFailure() {
        return failure;
    }
}
