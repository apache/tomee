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
package org.apache.openejb.junit.jee.statement;

import org.apache.openejb.Injector;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.junit.jee.resources.TestResource;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.TestInstance;
import org.junit.runners.model.Statement;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

public class InjectStatement extends Statement {
    private final StartingStatement startingStatement;
    private final Object test;
    private final Statement statement;
    private final Class<?> clazz;

    public InjectStatement(final Statement stat, final Class<?> clazz, final Object o, final StartingStatement startingStatement) {
        this.statement = stat;
        this.clazz = clazz;
        this.test = o;
        this.startingStatement = startingStatement;
    }

    @Override
    public void evaluate() throws Throwable {
        if (startingStatement != null) {
            Class<?> clazz = this.clazz;
            while (!Object.class.equals(clazz)) {
                for (final Field field : clazz.getDeclaredFields()) {
                    final TestResource resource = field.getAnnotation(TestResource.class);
                    if (resource != null) {
                        if (Context.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, startingStatement.getContainer().getContext());
                        } else if (Hashtable.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, startingStatement.getProperties());
                        } else if (EJBContainer.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, startingStatement.getContainer());
                        } else {
                            throw new OpenEJBException("can't inject field '" + field.getName() + "'");
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        if (test != null) {
            SystemInstance.get().setComponent(TestInstance.class, new TestInstance(test.getClass(), test));
            SystemInstance.get().getComponent(FallbackPropertyInjector.class); // force eager init (MockitoInjector initialize eveything in its constructor)
            Injector.inject(test);
        }
        if (statement != null) {
            statement.evaluate();
        }
    }
}
