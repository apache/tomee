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
package org.apache.openjpa.lib.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * {@link ResourceBundleProvider} that uses Java's built-in resource
 * bundle lookup methods.
 *
 * @author Abe White
 */
class SimpleResourceBundleProvider implements ResourceBundleProvider {

    public ResourceBundle findResource(String name, Locale locale,
        ClassLoader loader) {
        ResourceBundle bundle = null;
        if (loader != null) {
            try {
                bundle = ResourceBundle.getBundle(name, locale, loader);
            } catch (Throwable t) {
            }
        }

        // try with the default class loader
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(name, locale);
            } catch (Throwable t) {
            }
        }

        return bundle;
    }
}
