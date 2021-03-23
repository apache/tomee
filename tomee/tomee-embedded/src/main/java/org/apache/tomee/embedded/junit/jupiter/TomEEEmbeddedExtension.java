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
package org.apache.tomee.embedded.junit.jupiter;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.tomee.embedded.junit.TomEEEmbeddedBase;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;

import java.util.List;

public class TomEEEmbeddedExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final TomEEEmbeddedBase BASE = new TomEEEmbeddedBase();

    @Override
    public void afterAll(ExtensionContext context) {
        if (isPerClass(context)) {
            BASE.close();
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        if (isPerClass(context)) {
            BASE.start(context.getRequiredTestInstance());
            doInject(context);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (!isPerClass(context)) {
            BASE.close();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (!isPerClass(context)) {
            BASE.start(context.getRequiredTestInstance());
            doInject(context);
        }
    }

    private void doInject(final ExtensionContext extensionContext) {
        TestInstances oTestInstances = extensionContext.getTestInstances()
                .orElseThrow(() -> new OpenEJBRuntimeException("No test instances available for the given extension context."));

        List<Object> testInstances = oTestInstances.getAllInstances();

        testInstances.forEach(t -> {
            try {
                BASE.composerInject(t);
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        });
    }

    boolean isPerClass(final ExtensionContext context) {
        return context.getTestInstanceLifecycle()
                .map(it -> it.equals(TestInstance.Lifecycle.PER_CLASS))
                .orElse(false);
    }
}
