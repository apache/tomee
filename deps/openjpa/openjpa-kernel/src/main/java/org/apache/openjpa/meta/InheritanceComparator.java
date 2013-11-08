/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.meta;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator that keeps classes in inheritance order.
 *
 * @author Abe White
 * @nojavadoc
 */
public class InheritanceComparator
    implements Comparator, Serializable {

    private Class _base = Object.class;

    /**
     * Set the least-derived type possible; defaults to <code>null</code>.
     */
    public void setBase(Class base) {
        _base = base;
    }

    public Class getBase() {
        return _base;
    }

    /**
     * Subclasses can override this method to extract the class to compare
     * on from the elements of the collection.
     */
    protected Class toClass(Object elem) {
        return (Class) elem;
    }

    public int compare(Object o1, Object o2) {
        if (o1 == o2)
            return 0;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;

        Class c1 = toClass(o1);
        Class c2 = toClass(o2);
        if (c1 == c2)
            return 0;
        if (c1 == null)
            return -1;
        if (c2 == null)
            return 1;

        int i1 = levels(c1);
        int i2 = levels(c2);
        if (i1 == i2) {
            // sort simple interfaces as well as simple order test will fail.
            if (c1.isAssignableFrom(c2))
                return -1;
            if (c2.isAssignableFrom(c1))
                return 1;
            return c1.getName().compareTo(c2.getName());
        }
        if (i1 < i2)
            return -1;
        else if (i1 > i2)
            return 1;
        else
            return 0;
    }

    /**
     * Count the levels of inheritance between this class and our base class.
     */
    private int levels(Class to) {
        if (to.isInterface())
            return to.getInterfaces().length;
        for (int i = 0; to != null; i++, to = to.getSuperclass())
            if (to == _base)
                return i;
        return Integer.MAX_VALUE;
    }
}
