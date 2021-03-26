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
package org.apache.openejb.junit5;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApplicationComposerPerAllExtension extends ApplicationComposerPerXYExtensionBase implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, ApplicationComposerPerAllExtension.class);

    public ApplicationComposerPerAllExtension() {
        this((Object[]) null);
    }

    public ApplicationComposerPerAllExtension(Object... modules) {
        super(modules);
    }

    @Override
    protected void validate(ExtensionContext context) {
        super.validate(context);
        if (isPerAll(context) && isPerMethodLifecycle(context)) {
            logger.info("Running PER_ALL in combination with TestInstance.Lifecycle.PER_METHOD.");
            logger.info("Please note, there are some limitations (N = amount of test methods):");
            logger.info("N = 1: Will work as expected.");
            logger.info("N > 1: Injections are lost after the first test method was executed.");
            logger.info("N > 1: Use CDI.current(), InitialContext or pure (http) client to implement the test.");
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        super.beforeAll(context);
        doInit(context);
        doStart(context);
        doInject(context);
        addAfterAllReleaser(context);
    }
}
