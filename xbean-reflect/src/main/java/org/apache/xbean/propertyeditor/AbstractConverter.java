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

import java.beans.PropertyEditorSupport;

import org.apache.xbean.ClassLoading;

/**
 * A base class for converters.  This class handles all converter methods, and redirects all conversion requests to
 * toStringImpl and toObjectImpl.  These methods can assume that the supplied value or text is never null, and that
 * type checking has been applied to the value.
 *
 * @version $Rev: 6680 $
 */
public abstract class AbstractConverter extends PropertyEditorSupport implements Converter {
    private final Class type;

    /**
     * Creates an abstract converter for the specified type.
     *
     * @param type type of the property editor
     */
    protected AbstractConverter(Class type) {
        super();
        if (type == null) throw new NullPointerException("type is null");
        this.type = type;
    }

    public final Class getType() {
        return type;
    }

    public final String getAsText() {
        Object value = super.getValue();
        String text = toString(value);
        return text;
    }

    public final void setAsText(String text) {
        Object value = toObject(text.trim());
        super.setValue(value);
    }

    public final Object getValue() {
        Object value = super.getValue();
        return value;
    }

    public final void setValue(Object value) {
        if (value == null) {
            super.setValue(null);
        }
        if (!type.isInstance(value)) {
            throw new PropertyEditorException("Value is not an instance of " + ClassLoading.getClassName(type));
        }
        super.setValue(value);
    }

    public final String toString(Object value) {
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new PropertyEditorException("Value is not an instance of " + ClassLoading.getClassName(type) + ": " + value.getClass().getName());
        }
        return toStringImpl(value);
    }

    public final Object toObject(String text) {
        if (text == null) {
            return null;
        }

        Object value = toObjectImpl(text.trim());
        return value;
    }

    /**
     * Converts the supplied object to text.  The supplied object will always be an instance of the editor type, and
     * specifically will never be null or a String (unless this is the String editor).
     *
     * @param value an instance of the editor type
     * @return the text equivalent of the value
     */
    protected String toStringImpl(Object value) {
        String text = value.toString();
        return text;
    }

    /**
     * Converts the supplied text in to an instance of the editor type.  The text will never be null, and trim() will
     * already have been called.
     *
     * @param text the text to convert
     * @return an instance of the editor type
     */
    protected abstract Object toObjectImpl(String text);
}
