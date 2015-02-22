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
package org.apache.openejb.junit.jee;

import org.apache.openejb.junit.jee.statement.InjectStatement;
import org.apache.openejb.junit.jee.statement.StartingStatement;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class InjectRule implements TestRule {
    private final StartingStatement startingStatement;
    private final Object test;

    public InjectRule(final Object test) {
        this.test = test;
        this.startingStatement = null;
    }

    public InjectRule(final Object target, final EJBContainerRule rule) {
        this(target, rule.getStartingStatement());
    }

    public InjectRule(final Object target, final StartingStatement startingStatement) {
        this.test = target;
        this.startingStatement = startingStatement;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new InjectStatement(base, test.getClass(), test, startingStatement);
    }
}
