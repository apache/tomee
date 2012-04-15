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
package org.apache.xbean.finder;

import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Test;

import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class FinderSelectTest {

    @Test
    public void test() throws Exception {

        final AnnotationFinder all = new AnnotationFinder(new ClassesArchive(Red.class, Green.class, Blue.class));

        final AnnotationFinder finder = all.select(Red.class.getName());
        final List<Class<?>> classes = finder.findAnnotatedClasses(Color.class);

        for (Class<?> aClass : classes) {
            System.out.println(aClass);
        }

    }



    @java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE})
    @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface Color {
    }


    @Color
    public static class Red {

    }

    @Color
    public static class Green {

    }

    @Color
    public static class Blue {

    }
}
