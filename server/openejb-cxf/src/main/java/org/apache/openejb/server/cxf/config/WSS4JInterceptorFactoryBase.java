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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cxf.config;

import org.apache.xbean.recipe.ExecutionContext;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Recipe;
import org.apache.xbean.recipe.RecipeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class WSS4JInterceptorFactoryBase {

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected Map<String, Object> getAndDestroyMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }

        // avoid warnings
        final Recipe recipe = ExecutionContext.getContext().getStack().getLast();
        if (ObjectRecipe.class.isInstance(recipe)) {
            final ObjectRecipe or = ObjectRecipe.class.cast(recipe);
            if (or.getUnsetProperties() != null) {
                or.getUnsetProperties().clear();
            }
        }

        return map;
    }
}
