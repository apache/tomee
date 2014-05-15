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

package org.apache.openejb.cdi;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.web.context.SessionContextManager;

import java.lang.reflect.Field;
import java.util.Map;

public class UpdatableSessionContextManager extends SessionContextManager {
    private static final Field scField;

    static {
        try {
            scField = SessionContextManager.class.getDeclaredField("sessionContexts");
            scField.setAccessible(true);
        } catch (final NoSuchFieldException e) {
            throw new OpenEJBRuntimeException("sessionContexts attribute of SessionContextManager not found, you probably use a not compatible version of OWB");
        }
    }

    private final Map<String, SessionContext> contextById;

    public UpdatableSessionContextManager() {
        try {
            contextById = (Map<String, SessionContext>) scField.get(this);
        } catch (final IllegalAccessException e) {
            throw new OpenEJBRuntimeException("can't get session contexts", e);
        }
    }

    public void updateSessionIdMapping(final String oldId, final String newId) {
        if (oldId == null) {
            return;
        }

        final SessionContext sc = getSessionContextWithSessionId(oldId);
        if (sc == null) {
            return;
        }

        addNewSessionContext(newId, sc);
        contextById.remove(oldId);
    }
}
