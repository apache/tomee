/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.common.util.ClassHelper;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.reflection.Reflections;

public final class CxfHacks {
    public static void initCxfClassHelper() {
        if (!Boolean.parseBoolean(SystemInstance.get().getProperty("openejb.cxf.ClassHelper.patch", "true"))) {
            return;
        }
        try {
            Reflections.set(ClassHelper.class, null, "HELPER", new OpenEJBClassHelper());
        } catch (final Throwable throwable) {
            // no more a big deal normally since CXF uses our ClassUnwrapper
            Logger.getInstance(LogCategory.CXF, CxfHacks.class).info("Can't set OpenEJBClassHelper.");
        }
    }

    private CxfHacks() {
        // no-op
    }

    public static class OpenEJBClassHelper extends ClassHelper {
        @Override
        protected Class<?> getRealClassInternal(final Object o) {
            return getRealClassFromClassInternal(o.getClass());
        }

        @Override
        protected Class<?> getRealClassFromClassInternal(final Class<?> cls) {
            Class<?> c = cls;
            while (c.getName().contains("$$")) {
                c = c.getSuperclass();
            }
            return c == Object.class ? cls : c;
        }

        @Override
        protected Object getRealObjectInternal(final Object o) { // can we do anything here? sure we can unwrap ejb but wouldn't mean anything
            return o;
        }
    }
}
