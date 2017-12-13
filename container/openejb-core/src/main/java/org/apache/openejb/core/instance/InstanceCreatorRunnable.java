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
package org.apache.openejb.core.instance;

public final class InstanceCreatorRunnable implements Runnable {
    private final long maxAge;
    private final long iteration;
    private final double maxAgeOffset;
    private final long min;
    private final InstanceManagerData data;
    private final InstanceManager.StatelessSupplier supplier;

    public InstanceCreatorRunnable(final long maxAge, final long iteration, final long min, final double maxAgeOffset,
                                   final InstanceManagerData data, final InstanceManager.StatelessSupplier supplier) {
        this.maxAge = maxAge;
        this.iteration = iteration;
        this.min = min;
        this.maxAgeOffset = maxAgeOffset;
        this.data = data;
        this.supplier = supplier;
    }

    @Override
    public void run() {
        final InstanceManager.Instance obj = supplier.create();
        if (obj != null) {
            final long offset = maxAge > 0 ? (long) (maxAge / maxAgeOffset * min * iteration) % maxAge : 0l;
            data.getPool().add(obj, offset);
        }
    }
}