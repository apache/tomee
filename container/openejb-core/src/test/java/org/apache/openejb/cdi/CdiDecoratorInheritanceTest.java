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
package org.apache.openejb.cdi;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

@RunWith(ApplicationComposer.class) // OWB-861
public class CdiDecoratorInheritanceTest extends TestCase {
    @Inject
    private AService service;

    @Test
    public void checkItWorks() {
        assertEquals("the decorator", service.name());
    }

    @Module
    @Classes(cdi = true, value = {BaseDecorator.class, BaseModuleDecorator.class, ServiceDecorator.class, TheService.class}, cdiDecorators = {ServiceDecorator.class})
    public EjbJar jar() {
        return new EjbJar("cdi-decorator-inheritance");
    }

    public static abstract class BaseDecorator implements AService {

    }

    public static abstract class BaseModuleDecorator extends BaseDecorator {

    }

    @Decorator
    public static class ServiceDecorator extends BaseModuleDecorator {
        @Delegate
        @Inject
        private AService service;

        @Override
        public String name() {
            return service.name() + "decorator";
        }
    }

    public static class TheService implements AService {
        @Override
        public String name() {
            return "the ";
        }
    }

    public static interface AService {
        String name();
    }
}
