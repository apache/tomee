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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xbean.ClassLoading;

/**
 * @version $Rev: 6687 $ $Date: 2005-12-28T21:08:56.733437Z $
 */
public class MapRecipe implements Recipe {
    private final List entries;
    private final String type;

    public MapRecipe() {
        type = LinkedHashMap.class.getName();
        entries = new ArrayList();
    }

    public MapRecipe(String type) {
        this.type = type;
        entries = new ArrayList();
    }

    public MapRecipe(Class type) {
        this.type = type.getName();
        if (!RecipeHelper.hasDefaultConstructor(type)) throw new IllegalArgumentException("Type does not have a default constructor " + type);
        entries = new ArrayList();
    }

    public MapRecipe(Map map) {
        if (map == null) throw new NullPointerException("map is null");

        entries = new ArrayList(map.size());

        // If the specified set has a default constructor we will recreate the set, otherwise we use a LinkedHashMap or TreeMap
        if (RecipeHelper.hasDefaultConstructor(map.getClass())) {
            this.type = map.getClass().getName();
        } else if (map instanceof SortedMap) {
            this.type = TreeMap.class.getName();
        } else {
            this.type = LinkedHashMap.class.getName();
        }
        putAll(map);
    }

    public MapRecipe(String type, Map map) {
        if (map == null) throw new NullPointerException("map is null");
        this.type = type;
        entries = new ArrayList(map.size());
        putAll(map);
    }

    public MapRecipe(Class type, Map map) {
        if (map == null) throw new NullPointerException("map is null");
        if (!RecipeHelper.hasDefaultConstructor(type)) throw new IllegalArgumentException("Type does not have a default constructor " + type);
        this.type = type.getName();
        entries = new ArrayList(map.size());
        putAll(map);
    }

    public MapRecipe(MapRecipe mapRecipe) {
        if (mapRecipe == null) throw new NullPointerException("mapRecipe is null");
        this.type = mapRecipe.type;
        entries = new ArrayList(mapRecipe.entries);
    }

    public Object create(ClassLoader classLoader) {
        Class mapType = null;
        try {
            mapType = ClassLoading.loadClass(type, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ConstructionException("Type class could not be found: " + type);
        }

        if (!RecipeHelper.hasDefaultConstructor(mapType)) {
            throw new ConstructionException("Type does not have a default constructor " + type);
        }

        Object o;
        try {
            o = mapType.newInstance();
        } catch (Exception e) {
            throw new ConstructionException("Error while creating set instance: " + type);
        }

        if(!(o instanceof Map)) {
            throw new ConstructionException("Specified map type does not implement the Map interface: " + type);
        }

        Map instance = (Map) o;
        for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
            Object[] entry = (Object[]) iterator.next();

            Object key = entry[0];
            if (key instanceof Recipe) {
                Recipe recipe = (Recipe) key;
                try {
                    key = recipe.create(classLoader);
                } catch (ConstructionException e) {
                    e.setPrependAttributeName("[" + type + " " + key + "]");
                    throw e;
                }
            }

            Object value = entry[1];
            if (value instanceof Recipe) {
                Recipe recipe = (Recipe) value;
                try {
                    value = recipe.create(classLoader);
                } catch (ConstructionException e) {
                    e.setPrependAttributeName("[" + type + " " + key + "]");
                    throw e;
                }
            }

            instance.put(key, value);
        }
        return instance;
    }

    public void put(Object key, Object value) {
        if (key == null) throw new NullPointerException("key is null");
        if (!RecipeHelper.isSimpleType(key)) {
            key = new ValueRecipe(key);
        }
        if (!RecipeHelper.isSimpleType(value)) {
            value = new ValueRecipe(value);
        }
        entries.add(new Object[] { key, value});
    }

    public void putAll(Map map) {
        if (map == null) throw new NullPointerException("map is null");
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            put(key, value);
        }
    }
}
