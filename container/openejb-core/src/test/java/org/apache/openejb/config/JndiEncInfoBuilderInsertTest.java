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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.InjectableInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JndiEncInfoBuilderInsertTest {
    final JndiEncInfoBuilder builder = new JndiEncInfoBuilder(new AppInfo());
    final List<InjectableInfo> global = new ArrayList<>();
    final List<InjectableInfo> app = new ArrayList<>();
    final List<InjectableInfo> module = new ArrayList<>();
    final List<InjectableInfo> comp = new ArrayList<>();

    private void insert(final String referenceName) {
        final InjectableInfo injectableInfo = new InjectableInfo();
        injectableInfo.referenceName = referenceName;
        this.builder.insert(injectableInfo, this.global, this.app, this.module, this.comp);
    }

    private String[] getNames(final List<InjectableInfo> infoList) {
        final List<String> names = new ArrayList<>();
        for (final InjectableInfo info : infoList) {
            names.add(info.referenceName);
        }
        return names.toArray(new String[names.size()]);
    }

    private String[] getSafeArray(final String[] arr) {
        if (arr == null) {
            return new String[0];
        }
        return arr;
    }

    private void assertIsEqual(final String[] globalRefs, final String[] appRefs, final String[] moduleRefs, final String[] compRefs) {
        Assert.assertArrayEquals(getSafeArray(globalRefs), getNames(this.global));
        Assert.assertArrayEquals(getSafeArray(appRefs), getNames(this.app));
        Assert.assertArrayEquals(getSafeArray(moduleRefs), getNames(this.module));
        Assert.assertArrayEquals(getSafeArray(compRefs), getNames(this.comp));
    }

    String[] getArray(final String... strings) {
        return strings;
    }

    @Test
    public void test() throws Exception {
        insert("MyCompReference");
        assertIsEqual(
            null,
            null,
            null,
            getArray("comp/env/MyCompReference")
        );

        insert("java:global/MyGlobalReference");
        assertIsEqual(
            getArray("global/MyGlobalReference"),
            null,
            null,
            getArray("comp/env/MyCompReference")
        );

        insert("java:app/MyAppReference");
        assertIsEqual(
            getArray("global/MyGlobalReference"),
            getArray("app/MyAppReference"),
            null,
            getArray("comp/env/MyCompReference")
        );

        insert("java:module/MyModuleReference");
        assertIsEqual(
            getArray("global/MyGlobalReference"),
            getArray("app/MyAppReference"),
            getArray("module/MyModuleReference"),
            getArray("comp/env/MyCompReference")
        );

        insert("java:comp/MyCompReference2");
        assertIsEqual(
            getArray("global/MyGlobalReference"),
            getArray("app/MyAppReference"),
            getArray("module/MyModuleReference"),
            getArray("comp/env/MyCompReference", "comp/MyCompReference2")
        );

        insert("java:unknown/MyUnknownReference");
        assertIsEqual(
            getArray("global/MyGlobalReference"),
            getArray("app/MyAppReference"),
            getArray("module/MyModuleReference"),
            getArray("comp/env/MyCompReference", "comp/MyCompReference2")
        );
    }
}
