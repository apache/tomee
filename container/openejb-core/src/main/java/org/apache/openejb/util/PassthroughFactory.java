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
package org.apache.openejb.util;

import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.openejb.InjectionProcessor;

/**
 * @version $Rev$ $Date$
*/
public class PassthroughFactory {

    /**
     * xbean-reflect seems to sometimes get confused.
     * Despite explicitly setting the 'static Object create(Object)'
     * method as the factory method, sometimes xbean instead would
     * invoke the 'static ObjectRecipe recipe(Object)' method.
     *
     * Splitting the two methods into different classes seems to
     * eliminate the chances that xbean-reflect will pick the wrong
     * static method.
     */
    public static class Create {
        public static Object create(Object instance) {
            return instance;
        }
    }

    public static ObjectRecipe recipe(Object instance) {
        ObjectRecipe recipe = new ObjectRecipe(PassthroughFactory.Create.class);
        recipe.setFactoryMethod("create");

        String param = "instance"+recipe.hashCode();

        recipe.setConstructorArgNames(new String[]{param});
        recipe.setProperty(param, instance);

        return recipe;
    }
}
