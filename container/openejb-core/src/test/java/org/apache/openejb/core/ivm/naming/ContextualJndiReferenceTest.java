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
package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.Test;

import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

public class ContextualJndiReferenceTest {
    @Test
    public void propagateUnwrapping() throws NamingException {
        SystemInstance.get().setComponent(ContainerSystem.class, new CoreContainerSystem(new IvmJndiFactory()));
        final ContextualJndiReference ref = new ContextualJndiReference("foo");
        ref.setDefaultValue(new Reference() {
            @Override
            public Object getObject() throws NamingException {
                return "yeah";
            }
        });
        assertEquals("yeah", ref.getObject());
        SystemInstance.reset();
    }
}
