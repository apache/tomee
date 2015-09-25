/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.superbiz.resource.jmx.factory;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

public class Editors {

    private Editors() {
        // no-op
    }

    public static PropertyEditor get(final Class<?> type) {
        final PropertyEditor editor = PropertyEditorManager.findEditor(type);

        if (editor != null) {
            return editor;
        }

        final Class<Editors> c = Editors.class;

        try {
            final Class<?> editorClass = c.getClassLoader().loadClass(c.getName().replace("Editors", type.getSimpleName() + "Editor"));

            PropertyEditorManager.registerEditor(type, editorClass);

            return PropertyEditorManager.findEditor(type);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }


}