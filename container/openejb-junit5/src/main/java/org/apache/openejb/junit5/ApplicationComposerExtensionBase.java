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

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import java.util.Optional;

public abstract class ApplicationComposerExtensionBase {

    boolean isPerClassLifecycle(final ExtensionContext context) {
        return isPerTestInstanceLifecycle(context, TestInstance.Lifecycle.PER_CLASS);
    }

    boolean isPerMethodLifecycle(final ExtensionContext context) {
        return isPerTestInstanceLifecycle(context, TestInstance.Lifecycle.PER_METHOD);
    }

    boolean isPerTestInstanceLifecycle(final ExtensionContext context, TestInstance.Lifecycle lifecycle) {
        return context.getTestInstanceLifecycle()
                .map(it -> it.equals(lifecycle))
                .orElse(false);
    }

    protected boolean isPerEach(final ExtensionContext context) {
        return checkMode(context, ExtensionMode.PER_EACH);
    }

    boolean isPerAll(final ExtensionContext context) {
        return checkMode(context, ExtensionMode.PER_ALL);
    }

    boolean isPerJvm(final ExtensionContext context) {
        return checkMode(context, ExtensionMode.PER_JVM);
    }

    boolean isPerDefault(final ExtensionContext context) {
        return checkMode(context, ExtensionMode.AUTO);
    }

    boolean checkMode(final ExtensionContext context, ExtensionMode extensionMode ) {
       return extensionMode == getModeFromAnnotation(context);
    }

    ExtensionMode getModeFromAnnotation(final ExtensionContext context) {
        return context.getTestClass()
                .flatMap(test -> AnnotationUtils.findAnnotation(test, RunWithApplicationComposer.class))
                .map(RunWithApplicationComposer::mode)
                .orElse(ExtensionMode.AUTO);
    }
}
