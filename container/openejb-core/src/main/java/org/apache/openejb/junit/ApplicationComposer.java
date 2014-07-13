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

import java.util.List;

import org.apache.openejb.testing.ApplicationComposers;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationComposer extends BlockJUnit4ClassRunner {
    private final ApplicationComposers delegate;

    public ApplicationComposer(final Class<?> klass) throws InitializationError {
        super(klass);
        delegate = new ApplicationComposers(klass);
    }

    @Override
    protected List<MethodRule> rules(final Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add(new MethodRule() {
            public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
                return new DeployApplication(target, base, delegate);
            }
        });
        return rules;
    }
}
