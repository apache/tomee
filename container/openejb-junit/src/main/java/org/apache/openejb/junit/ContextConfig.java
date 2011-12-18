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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.junit;

import org.apache.openejb.junit.OpenEjbRunner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ContextConfig {
    /**
     * Reference to InitialContext configuration resource file. It should be a resource
     * in the annotated class' classpath. If both a method/class has this value specified,
     * then the class' file will be loaded first, and thus properties in the method's file
     * will overwrite those from the class'.
     * <p/>
     * If the file is not found a FileNotFoundException will be raised.
     * <p/>
     * Using the {@link OpenEjbRunner} runner, this will be a reference to a standard properties file.
     */
    String configFile() default "";

    /**
     * Array of properties. The properties file has precedency in being loaded, so
     * any properties listed here will overwrite those loaded from the properties file.
     * <p/>
     * Again, a Method's properties are loaded after a class' properties, so will
     * overwrite those from the method.
     * <p/>
     * If you specify both a properties file and properties for both a method and a class,
     * first the class' file and properties will be loaded, then the method's file
     * and properties, so the precedency is effectively the following:
     * <ol>
     * <li>Class properties file
     * <li>Class properties
     * <li>Method properties file
     * <li>Method properties
     * </ol>
     * <p/>
     * If any property's names are null, then the property will simply be ignored.
     */
    Property[] properties() default {};
}
