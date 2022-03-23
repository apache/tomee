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
package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(ValidationRunner.class)
public class CheckCdiEnabledTest {
    @Keys(@Key(value = "cdi.notEnabled", type = KeyType.WARNING))
    public EjbModule cdiShouldBeOn() throws OpenEJBException {
        return new EjbModule(new EjbJar())
            .finder(new AnnotationFinder(new ClassesArchive(Bean1.class, Bean2.class)));
    }

    public static class Bean1 {
    }

    public static class Bean2 {
        @Inject
        private Bean1 bean1;
    }
}
