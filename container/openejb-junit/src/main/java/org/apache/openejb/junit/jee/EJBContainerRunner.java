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

import org.apache.openejb.junit.jee.statement.ShutingDownStatement;
import org.apache.openejb.junit.jee.statement.StartingStatement;
import org.apache.openejb.junit.jee.transaction.TransactionRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EJBContainerRunner extends BlockJUnit4ClassRunner {
    private StartingStatement startingStatement;

    public EJBContainerRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement withBeforeClasses(final Statement statement) {
        startingStatement = new StartingStatement(super.withBeforeClasses(statement), getTestClass().getJavaClass());
        return startingStatement;
    }

    @Override
    protected Statement withAfterClasses(final Statement statement) {
        return new ShutingDownStatement(super.withAfterClasses(statement), startingStatement);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        final List<FrameworkMethod> methods = new ArrayList<>(super.computeTestMethods()); // copy cause of junit 4.12 which is umodifiable
        Collections.shuffle(methods); // real tests should manage shuffle ordering
        return methods;
    }

    @Override
    protected List<TestRule> getTestRules(final Object target) {
        final List<TestRule> rules = new ArrayList<TestRule>();
        rules.add(new InjectRule(target, startingStatement));
        rules.add(new TransactionRule());
        rules.addAll(getTestClass().getAnnotatedFieldValues(target, Rule.class, TestRule.class));
        return rules;
    }
}

