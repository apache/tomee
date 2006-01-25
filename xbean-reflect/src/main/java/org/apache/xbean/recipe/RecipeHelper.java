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

import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;

/**
 * @version $Rev: 6687 $ $Date: 2005-12-28T21:08:56.733437Z $
 */
public final class RecipeHelper {
    private RecipeHelper() {
    }

    public static boolean hasDefaultConstructor(Class type) {
        if (!Modifier.isPublic(type.getModifiers())) {
            return false;
        }
        Constructor[] constructors = type.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            if (Modifier.isPublic(constructor.getModifiers()) &&
                    constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSimpleType(Object o) {
        return  o == null ||
                o instanceof Boolean ||
                o instanceof Character ||
                o instanceof Byte ||
                o instanceof Short ||
                o instanceof Integer ||
                o instanceof Long ||
                o instanceof Float ||
                o instanceof Double ||
                o instanceof String ||
                o instanceof Recipe;

    }
}
