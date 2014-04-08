/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import junit.framework.TestCase;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

public class PersistenceContextAnnFactoryTest extends TestCase {
    public void test() throws Exception {
        Field useAsm = PersistenceContextAnnFactory.class.getDeclaredField("useAsm");
        useAsm.setAccessible(true);
        useAsm.set(null, true);
        
        PersistenceContextAnnFactory factory = new PersistenceContextAnnFactory();
        factory.addAnnotations(Foo.class);

        for (PersistenceContext annotation : Foo.class.getAnnotation(PersistenceContexts.class).value()) {
            assertEq(annotation, factory.create(annotation, null));
        }
        
        PersistenceContext annotation = Foo.class.getAnnotation(PersistenceContext.class);
        assertEq(annotation, factory.create(annotation, null));

        for (Field field : Foo.class.getFields()) {
            annotation = field.getAnnotation(PersistenceContext.class);
            assertEq(annotation, factory.create(annotation, new AnnotationDeployer.FieldMember(field)));
        }

        for (Method method : Foo.class.getMethods()) {
            annotation = method.getAnnotation(PersistenceContext.class);
            if (annotation != null) {
                assertEq(annotation, factory.create(annotation, new AnnotationDeployer.MethodMember(method)));
            }
        }
    }

    private static void assertEq(PersistenceContext annotation, PersistenceContextAnn wrapper) {
        if (annotation.name().length() > 0) {
            assertEquals(annotation.name(), wrapper.name());
        }
        assertEquals(annotation.unitName(), wrapper.unitName());
        assertEquals(annotation.type().toString(), wrapper.type());

        Map<String,String> properties = new HashMap<String,String>();
        for (PersistenceProperty property : annotation.properties()) {
            properties.put(property.name(), property.value());
        }
        assertEquals(properties, wrapper.properties());
    }

    @PersistenceContexts({
            @PersistenceContext(name = "classPCs1", unitName = "CPCs1u", type = PersistenceContextType.EXTENDED, properties = {
                    @PersistenceProperty(name = "classPCs1-1", value = "CPCs1"),
                    @PersistenceProperty(name = "classPCs1-2", value = "CPCs2")
            }),
            @PersistenceContext(name = "classPCs2", unitName = "CPCs2u", type = PersistenceContextType.EXTENDED, properties = {
                    @PersistenceProperty(name = "classPCs2-1", value = "CPCs1"),
                    @PersistenceProperty(name = "classPCs2-2", value = "CPCs2")
            })
    })
    @PersistenceContext(name = "class", unitName = "cu", type = PersistenceContextType.EXTENDED, properties = {
            @PersistenceProperty(name = "class1", value = "c1"),
            @PersistenceProperty(name = "class2", value = "c2")
    })
    public static class Foo {
        @PersistenceContext(name = "field", unitName = "fu", type = PersistenceContextType.EXTENDED, properties = {
                @PersistenceProperty(name = "field1", value = "f1"),
                @PersistenceProperty(name = "field2", value = "f1")
        })
        public Object field;

        @PersistenceContext(name = "method", unitName = "mu", type = PersistenceContextType.EXTENDED, properties = {
                @PersistenceProperty(name = "method1", value = "m1"),
                @PersistenceProperty(name = "method2", value = "m2")
        })
        public void method() {}

        @PersistenceContext(unitName = "myfu", type = PersistenceContextType.EXTENDED, properties = {
                @PersistenceProperty(name = "myField1", value = "myf1"),
                @PersistenceProperty(name = "myField2", value = "myf1")
        })
        public Object myField;

        @PersistenceContext(unitName = "mymu", type = PersistenceContextType.EXTENDED, properties = {
                @PersistenceProperty(name = "myMethod1", value = "mym1"),
                @PersistenceProperty(name = "myMethod2", value = "mym2")
        })
        public void setMyMethod() {}
    }
}
