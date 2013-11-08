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
package org.apache.openjpa.lib.meta;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;

import org.xml.sax.SAXException;

/**
 * Helps serialize metadata objects to package and class elements.
 *
 * @author Abe White
 * @nojavadoc
 * @see CFMetaDataParser
 */
public abstract class CFMetaDataSerializer extends XMLMetaDataSerializer {

    private String _package = null;

    /**
     * The default package for objects being serialized.
     */
    protected String getPackage() {
        return _package;
    }

    /**
     * The default package for objects being serialized.
     */
    protected void setPackage(String pkg) {
        _package = pkg;
    }

    /**
     * Helper method to group objects by package.
     * 
     * @return mapping of package name to a collection of objects in that
     *         package
     */
    protected Map<String, Collection<Object>> groupByPackage(
        Collection<Object> objs) throws SAXException {
        Map<String, Collection<Object>> packages =
            new LinkedHashMap<String, Collection<Object>>();
        String packageName;
        Collection<Object> packageObjs;
        Object obj;
        for (Iterator<Object> itr = objs.iterator(); itr.hasNext();) {
            obj = itr.next();
            packageName = getPackage(obj);
            packageObjs = packages.get(packageName);
            if (packageObjs == null) {
                packageObjs = new LinkedList<Object>();
                packages.put(packageName, packageObjs);
            }
            packageObjs.add(obj);
        }
        return packages;
    }

    /**
     * Return the package name of the given object, or null if not in a
     * package. Used by {@link #groupByPackage}. Returns null by default.
     */
    protected String getPackage(Object obj) {
        return null;
    }

    /**
     * Returns the given class name, stripping the package if it is not needed.
     */
    protected String getClassName(String name) {
        // check if in current package; make sure not in a sub-package
        if (_package != null && name.lastIndexOf('.') == _package.length()
            && name.startsWith(_package))
            return name.substring(_package.length() + 1);

        // check other known packages
        String[] packages = CFMetaDataParser.PACKAGES;
        for (int i = 0; i < packages.length; i++)
            if (name.startsWith(packages[i])
                && name.lastIndexOf('.') == packages[i].length() - 1)
                return name.substring(packages[i].length());
        return name;
    }
}
