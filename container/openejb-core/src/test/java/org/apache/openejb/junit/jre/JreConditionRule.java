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
package org.apache.openejb.junit.jre;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit 4 rule that skips a test method when the running JRE feature version is
 * outside the range declared by {@link EnabledForJreRange} on the method. Skipping is
 * implemented by throwing {@link AssumptionViolatedException}, matching surefire's
 * "skipped" category.
 */
public final class JreConditionRule implements TestRule {

    @Override
    public Statement apply(final Statement base, final Description description) {
        final EnabledForJreRange range = description.getAnnotation(EnabledForJreRange.class);
        if (range == null) {
            return base;
        }

        final int current = Runtime.version().feature();
        if (current < range.min() || current > range.max()) {
            return new Statement() {
                @Override
                public void evaluate() {
                    throw new AssumptionViolatedException(
                            "Requires Java " + range.min() + ".." + range.max()
                                    + ", running on " + current);
                }
            };
        }
        return base;
    }
}
