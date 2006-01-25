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
package org.apache.xbean.recipe;

import org.apache.xbean.propertyeditor.PropertyEditors;
import org.apache.xbean.propertyeditor.PropertyEditorException;
import org.apache.xbean.ClassLoading;


/**
 * @version $Rev: 6689 $ $Date: 2006-01-02T06:48:49.815187Z $
 */
public class ValueRecipe implements Recipe {
    private final String type;
    private final String value;

    public ValueRecipe(Class type, String value) {
        if (type == null) throw new NullPointerException("type is null");
        if (!PropertyEditors.canConvert(type)) {
            throw new IllegalArgumentException("No converter available for " + ClassLoading.getClassName(type));
        }
        this.type = type.getName();
        this.value = value;
    }

    public ValueRecipe(String type, String value) {
        if (type == null) throw new NullPointerException("type is null");
        this.type = type;
        this.value = value;
    }

    public ValueRecipe(Object value) {
        if (value == null) throw new NullPointerException("value is null");
        this.type = value.getClass().getName();
        this.value = PropertyEditors.toString(value);
    }

    public ValueRecipe(ValueRecipe valueRecipe) {
        if (valueRecipe == null) throw new NullPointerException("valueRecipe is null");
        this.type = valueRecipe.type;
        this.value = valueRecipe.value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Object create(ClassLoader classLoader) {
        if (value == null) {
            return null;
        }

        try {
            return PropertyEditors.getValue(type, value, classLoader);
        } catch (PropertyEditorException e) {
            throw new ConstructionException(e);
        }
    }
}
