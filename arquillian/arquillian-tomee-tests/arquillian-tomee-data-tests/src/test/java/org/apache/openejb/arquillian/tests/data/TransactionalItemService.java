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
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Calls {@link SimpleItemRepository} from inside CDI {@code @Transactional} methods, where the
 * JTA spec forbids any use of {@code UserTransaction}.
 */
@ApplicationScoped
public class TransactionalItemService {

    @Inject
    private SimpleItemRepository repository;

    @Transactional
    public Long insert(final String label) {
        return repository.insert(new SimpleItem(label)).getId();
    }

    @Transactional
    public String findLabel(final Long id) {
        return repository.findById(id).map(SimpleItem::getLabel).orElse(null);
    }

    /**
     * Loads the entity and saves it again within one transaction, so the instance handed to
     * {@code save} is the managed one {@code findById} returned.
     */
    @Transactional
    public void rename(final Long id, final String label) {
        final SimpleItem item = repository.findById(id).orElseThrow();
        item.setLabel(label);
        repository.save(item);
    }

    @Transactional
    public void delete(final Long id) {
        repository.deleteById(id);
    }
}
