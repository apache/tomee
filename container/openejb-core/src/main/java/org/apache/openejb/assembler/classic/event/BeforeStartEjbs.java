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
package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.BeanContext;
import org.apache.openejb.observer.Event;

import java.util.Collections;
import java.util.List;

/**
 * IMPORTANT NOTE: when using this event it should be compared to BeforeAppInfoBuilderEvent which is likely better.
 * Main reason to use BeforeStartEjbs is the need of reflection (to do filtering for instance). All other cases shouldn't use it.
 */
@Event
public class BeforeStartEjbs {
    private final List<BeanContext> ejbs;

    public BeforeStartEjbs(final List<BeanContext> allDeployments) {
        this.ejbs = allDeployments;
    }

    public List<BeanContext> getEjbs() {
        return Collections.unmodifiableList(ejbs);
    }

    @Override
    public String toString() {
        return "BeforeStartEjbs{" +
                "#ejbs=" + ejbs.size() +
                '}';
    }
}
