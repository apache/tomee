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
package org.apache.openejb.junit5.order;

import org.apache.openejb.junit5.SingleAppComposerJVMTest;
import org.apache.openejb.junit5.SingleAppComposerTest;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class AppComposerTestClassOrderer implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(AppComposerTestClassOrderer::getOrder));
    }

    /*
     * SingleAppComposerTest before SingleAppComposerJVMTest (same JVM test)
     */
    private static int getOrder(ClassDescriptor classDescriptor) {
        if (classDescriptor.getTestClass().equals(SingleAppComposerTest.class)) {
            return 2;
        }

        if (classDescriptor.getTestClass().equals(SingleAppComposerJVMTest.class)) {
            return 3;
        }

        return 1;
    }
}
