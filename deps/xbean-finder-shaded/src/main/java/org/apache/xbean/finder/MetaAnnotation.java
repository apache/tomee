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
package org.apache.xbean.finder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
* @version $Rev$ $Date$
*/
public class MetaAnnotation<T extends Annotation> {
    private final T annotation;
    private final int depth;

    private final List<MetaAnnotation<T>> conflicts = new ArrayList<MetaAnnotation<T>>();

    MetaAnnotation(T annotation, int depth) {
        this.annotation = annotation;
        this.depth = depth;
    }

    public T get() {
        return annotation;
    }

    public int getDepth() {
        return depth;
    }

    public List<MetaAnnotation<T>> getConflicts() {
        return conflicts;
    }
}
