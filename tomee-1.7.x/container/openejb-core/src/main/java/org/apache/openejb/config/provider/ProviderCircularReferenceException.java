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

package org.apache.openejb.config.provider;

import org.apache.openejb.util.Join;

import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class ProviderCircularReferenceException extends IllegalStateException {

    private final Set<ID> ids;

    public ProviderCircularReferenceException(final Set<ID> ids) {
        super(String.format("Circular reference: %s -> %s", Join.join(" -> ", ids), ids.iterator().next()));
        this.ids = ids;
    }

    public Set<ID> getIds() {
        return ids;
    }
}
