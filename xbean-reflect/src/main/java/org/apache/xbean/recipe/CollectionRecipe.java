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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.xbean.ClassLoading;

/**
 * @version $Rev: 6685 $ $Date: 2005-12-28T00:29:37.967210Z $
 */
public class CollectionRecipe implements Recipe {
    private final List list;
    private final String type;

    public CollectionRecipe() {
        list = new ArrayList();
        type = ArrayList.class.getName();
    }

    public CollectionRecipe(String type) {
        list = new ArrayList();
        this.type = type;
    }

    public CollectionRecipe(Class type) {
        if (type == null) throw new NullPointerException("type is null");
        if (!RecipeHelper.hasDefaultConstructor(type)) throw new IllegalArgumentException("Type does not have a default constructor " + type);
        this.list = new ArrayList();
        this.type = type.getName();
    }

    public CollectionRecipe(Collection collection) {
        if (collection == null) throw new NullPointerException("collection is null");

        this.list = new ArrayList(collection.size());

        // If the specified set has a default constructor we will recreate the set, otherwise we use a the default
        if (RecipeHelper.hasDefaultConstructor(collection.getClass())) {
            this.type = collection.getClass().getName();
        } else if (collection instanceof SortedSet) {
            this.type = TreeSet.class.getName();
        } else if (collection instanceof Set) {
            this.type = LinkedHashSet.class.getName();
        } else {
            this.type = ArrayList.class.getName();
        }
        addAll(collection);
    }

    public CollectionRecipe(String type, Collection collection) {
        if (type == null) throw new NullPointerException("type is null");
        if (collection == null) throw new NullPointerException("collection is null");
        this.list = new ArrayList(collection.size());
        this.type = type;
        addAll(collection);
    }

    public CollectionRecipe(Class type, Collection collection) {
        if (type == null) throw new NullPointerException("type is null");
        if (!RecipeHelper.hasDefaultConstructor(type)) throw new IllegalArgumentException("Type does not have a default constructor " + type);
        if (collection == null) throw new NullPointerException("collection is null");
        this.list = new ArrayList(collection.size());
        this.type = type.getName();
        addAll(collection);
    }

    public CollectionRecipe(CollectionRecipe collectionRecipe) {
        if (collectionRecipe == null) throw new NullPointerException("setRecipe is null");
        this.type = collectionRecipe.type;
        list = new ArrayList(collectionRecipe.list);
    }

    public Object create(ClassLoader classLoader) {
        Class setType = null;
        try {
            setType = ClassLoading.loadClass(type, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ConstructionException("Type class could not be found: " + type);
        }

        if (!RecipeHelper.hasDefaultConstructor(setType)) {
            throw new ConstructionException("Type does not have a default constructor " + type);
        }

        Object o;
        try {
            o = setType.newInstance();
        } catch (Exception e) {
            throw new ConstructionException("Error while creating set instance: " + type);
        }

        if(!(o instanceof Collection)) {
            throw new ConstructionException("Specified set type does not implement the Collection interface: " + type);
        }

        Collection instance = (Collection) o;
        int i =0;
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object value = iterator.next();
            if (value instanceof Recipe) {
                Recipe recipe = (Recipe) value;
                try {
                    value = recipe.create(classLoader);
                } catch (ConstructionException e) {
                    e.setPrependAttributeName("[" + type + " item " + i + "]");
                    throw e;
                }
            }
            instance.add(value);
            i++;
        }
        return instance;
    }

    public void add(Boolean value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Character value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Byte value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Short value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Integer value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Long value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Float value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Double value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(String value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void add(Recipe value) {
        if (value == null) throw new NullPointerException("value is null");
        list.add(value);
    }

    public void addAll(Collection value) {
        if (value == null) throw new NullPointerException("value is null");
        for (Iterator iterator = value.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o instanceof Boolean) {
                add((Boolean)o);
            } else if (o instanceof Character) {
                add((Character)o);
            } else if (o instanceof Byte) {
                add((Byte)o);
            } else if (o instanceof Short) {
                add((Short)o);
            } else if (o instanceof Integer) {
                add((Integer)o);
            } else if (o instanceof Long) {
                add((Long)o);
            } else if (o instanceof Float) {
                add((Float)o);
            } else if (o instanceof Double) {
                add((Double)o);
            } else if (o instanceof String) {
                add((String)o);
            } else if (o instanceof Recipe) {
                add((Recipe)o);
            } else {
                add(new ValueRecipe(o));
            }
        }
    }

}
