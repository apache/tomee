/**
 *
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.propertyeditor;

import java.beans.PropertyEditor;
import java.util.Map;

/**
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public abstract class AbstractMapConverter extends AbstractConverter {
    private final PropertyEditor keyEditor;
    private final PropertyEditor valueEditor;

    public AbstractMapConverter(Class type) {
        super(type);
        this.keyEditor = new StringEditor();
        this.valueEditor = new StringEditor();
    }

    protected AbstractMapConverter(Class type, PropertyEditor keyEditor, PropertyEditor valueEditor) {
        super(type);
        this.keyEditor = keyEditor;
        this.valueEditor = valueEditor;
    }

    /**
     * Treats the text value of this property as an input stream that
     * is converted into a Property bundle.
     *
     * @return a Properties object
     * @throws PropertyEditorException An error occurred creating the Properties object.
     */
    protected final Object toObjectImpl(String text) {
        Map map = CollectionUtil.toMap(text, keyEditor, valueEditor);
        if (map == null) {
            return null;
        }
        Map finalMap = createMap(map);
        return finalMap;
    }

    protected abstract Map createMap(Map map);

    protected final String toStringImpl(Object value) {
        Map map = (Map) value;
        String text = CollectionUtil.toString(map, keyEditor, valueEditor);
        return text;
    }
}
