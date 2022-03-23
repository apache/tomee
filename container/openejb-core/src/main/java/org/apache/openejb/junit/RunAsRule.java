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
package org.apache.openejb.junit;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import jakarta.annotation.security.RunAs;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class RunAsRule extends BeanContextBaseRule implements TestRule {
    private final ThreadLocal<String> role = new ThreadLocal<>();

    public RunAsRule() {
        this(null);
    }

    public RunAsRule(final Object o) {
        super(o);
    }

    public void role(final String role) {
        this.role.set(role);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final RunAs annotation = description.getAnnotation(RunAs.class);
                final As as = description.getAnnotation(As.class);
                String currentRole = role.get();
                role.remove(); // no more needed
                if (annotation == null && as == null && currentRole == null) {
                    base.evaluate();
                    return;
                }

                final BeanContext beanContext = getBeanContext();
                if (currentRole == null) {
                    if (annotation == null) {
                        currentRole = as.value();
                    } else {
                        currentRole = annotation.value();
                    }
                }
                final String runAs = beanContext.getRunAs();
                final String runAsUser = beanContext.getRunAsUser();
                beanContext.setRunAs(currentRole);
                final ThreadContext old = ThreadContext.enter(new ThreadContext(beanContext, null));
                try {
                    base.evaluate();
                } finally {
                    // reset for next test
                    ThreadContext.exit(old);
                    beanContext.setRunAs(runAs);
                    beanContext.setRunAsUser(runAsUser);
                }
            }
        };
    }

    @Target({ElementType.METHOD, ElementType.TYPE}) // cause @RunAs doesn't support method
    @Retention(RetentionPolicy.RUNTIME)
    public @interface As {
        String value();
    }
}
