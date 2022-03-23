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
package org.apache.openejb.bval;

import org.apache.bval.cdi.BValExtension;
import org.apache.openejb.util.ContainerClassesFilter;

import jakarta.enterprise.inject.spi.AnnotatedType;

public class BValCdiFilter implements BValExtension.AnnotatedTypeFilter {
    private final ContainerClassesFilter delegate = new ContainerClassesFilter();

    @Override
    public boolean accept(final AnnotatedType<?> annotatedType) {
        final String name = annotatedType.getJavaClass().getName();
        if (name.startsWith("org.apache.openejb.")) {
            final String sub = name.substring("org.apache.openejb.".length());
            return !sub.startsWith("cdi.transactional") && !sub.startsWith("resource.activemq.jms2");
        }
        return delegate.accept(name);
    }

}
