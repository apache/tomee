/**
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

import org.apache.openejb.junit.context.OpenEjbTestContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OpenEjbExtension implements BeforeEachCallback, AfterEachCallback {

    private final String role;
    private OpenEjbTestContext classTestContext;

    public OpenEjbExtension() {
        this(null);
    }

    public OpenEjbExtension(String role) {
        this.role = role;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        classTestContext = newTestContext(extensionContext, role);
        classTestContext.configureTest(extensionContext.getTestInstance().get());
    }

    public OpenEjbTestContext newTestContext(ExtensionContext extensionContext, final String roleName) {
        if (!extensionContext.getTestMethod().isPresent()) {
            if (classTestContext == null) {
                classTestContext = new OpenEjbTestContext(extensionContext.getTestClass().get());
            }
            return classTestContext;
        } else {
            return new OpenEjbTestContext(extensionContext.getTestMethod().get(), roleName);
        }
    }


    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (classTestContext != null) {
            classTestContext.close();
        }
    }
}
