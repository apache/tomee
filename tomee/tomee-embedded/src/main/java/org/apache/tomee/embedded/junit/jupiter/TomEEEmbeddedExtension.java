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
import org.junit.platform.commons.util.AnnotationUtils;

import java.util.List;

public class TomEEEmbeddedExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final TomEEEmbeddedBase BASE = new TomEEEmbeddedBase();

    // the container of PER_JVM is never closed, so the other modes can't be used once it is started
    private static volatile boolean perJvm;

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final ExtensionMode mode = getMode(context);
        validate(mode);

        if (mode != ExtensionMode.PER_EACH) {
            BASE.start(context.getRequiredTestClass());
            if (mode == ExtensionMode.PER_JVM) {
                perJvm = true;
            }
            if (isPerClass(context)) {
                doInject(context);
            }
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        if (getMode(context) == ExtensionMode.PER_ALL) {
            BASE.close();
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final boolean perEach = getMode(context) == ExtensionMode.PER_EACH;
        if (perEach) {
            BASE.start(context.getRequiredTestClass());
        }
        if (perEach || !isPerClass(context)) {
            doInject(context);
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        if (getMode(context) == ExtensionMode.PER_EACH) {
            BASE.close();
        }
    }

    private void validate(final ExtensionMode mode) {
        if (perJvm && mode != ExtensionMode.PER_JVM) {
            throw new OpenEJBRuntimeException("Cannot run PER_JVM in combination with PER_ALL, PER_EACH or AUTO");
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

    // resolves AUTO to the mode matching the lifecycle of the test instance
    ExtensionMode getMode(final ExtensionContext context) {
        final ExtensionMode mode = context.getTestClass()
                .flatMap(test -> AnnotationUtils.findAnnotation(test, RunWithTomEEEmbedded.class))
                .map(RunWithTomEEEmbedded::mode)
                .orElse(ExtensionMode.AUTO);
        if (mode == ExtensionMode.AUTO) {
            return isPerClass(context) ? ExtensionMode.PER_ALL : ExtensionMode.PER_EACH;
        }
        return mode;
    }

    boolean isPerClass(final ExtensionContext context) {
        return context.getTestInstanceLifecycle()
                .map(it -> it.equals(TestInstance.Lifecycle.PER_CLASS))
                .orElse(false);
    }
}
