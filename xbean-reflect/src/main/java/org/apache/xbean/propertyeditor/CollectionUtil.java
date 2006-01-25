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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.Properties;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.apache.xbean.ClassLoading;

/**
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public final class CollectionUtil {
    public static List toList(String text, PropertyEditor componentEditor) {
        if (text.length() == 0) {
            return null;
        }

        // text may be surrounded with [ and ]
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1).trim();
        }

        List list = new LinkedList();

        if (text.length() > 0) {
            StringTokenizer stok = new StringTokenizer(text, ",");
            while (stok.hasMoreTokens()) {
                String innerText = stok.nextToken();
                Object value = componentToObject(innerText, componentEditor);
                list.add(value);
            }
        }

        return list;
    }

    public static String toString(Collection values, PropertyEditor componentEditor) {
        if (values.size() == 0) {
            return "[]";
        }

        StringBuffer result = new StringBuffer();
        result.append("[");
        int i = 0;
        for (Iterator iterator = values.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            String text = componentToString(object, componentEditor);

            if (i > 0) {
                result.append(",");
            }
            result.append(text);
            i++;
        }
        result.append("]");
        return result.toString();
    }

    public static final Map toMap(String text, PropertyEditor keyEditor, PropertyEditor valueEditor) {
        Properties properties = new Properties();
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
            properties.load(stream);
        } catch (IOException e) {
            // any errors here are just a property exception
            throw new PropertyEditorException(e);
        }

        // run the properties through the editors
        Map map = new LinkedHashMap(properties.size());
        for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String keyText = (String) entry.getKey();
            String valueText = (String) entry.getValue();

            Object keyObject = componentToObject(keyText, keyEditor);
            Object valueObject = componentToObject(valueText, valueEditor);

            map.put(keyObject, valueObject);
        }
        return map;
    }

    public static final String toString(Map map, PropertyEditor keyEditor, PropertyEditor valueEditor) {
        // run the properties through the editors
        Properties properties = new Properties();
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object keyObject = entry.getKey();
            Object valueObject = entry.getValue();

            String keyText = componentToString(keyObject, keyEditor);
            String valueText = componentToString(valueObject, valueEditor);

            properties.setProperty(keyText, valueText);
        }

        // write using the properties object
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            properties.store(out, null);
            String text = new String(out.toByteArray());
            return text;
        } catch (IOException e) {
            // any errors here are just a property exception
            throw new PropertyEditorException(e);
        }
    }

    private static final String componentToString(Object value, PropertyEditor componentEditor) {
        if (value == null) {
            return null;
        }
        if (componentEditor instanceof Converter) {
            Converter converter = (Converter) componentEditor;
            Class type = converter.getType();
            if (!type.isInstance(value)) {
                throw new PropertyEditorException("Value is not an instance of " + ClassLoading.getClassName(type) + ": " + value.getClass().getName());
            }
            return converter.toString(value);
        } else {
            componentEditor.setValue(value);
            String text = componentEditor.getAsText();
            return text;
        }
    }

    private static final Object componentToObject(String text, PropertyEditor componentEditor) {
        if (text == null) {
            return null;
        }

        if (componentEditor instanceof Converter) {
            Converter converter = (Converter) componentEditor;
            Object value = converter.toObject(text.trim());
            return value;
        } else {
            componentEditor.setAsText(text);
            Object value = componentEditor.getValue();
            return value;
        }
    }
}
