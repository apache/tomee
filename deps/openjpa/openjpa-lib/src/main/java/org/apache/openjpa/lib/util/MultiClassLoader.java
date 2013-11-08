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

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class loader type that can be configured to delegate to multiple
 * internal class loaders.
 * The {@link #THREAD_LOADER} constant is a marker that will be replaced
 * with the context loader of the current thread.
 *
 * @author Abe White
 */
public class MultiClassLoader extends ClassLoader {

    /**
     * Marker that will be replaced with the context loader of the current
     * thread whenever it is discovered in the class loader list.
     */
    public static final ClassLoader THREAD_LOADER = null;

    /**
     * The standard system class loader.
     */
    public static final ClassLoader SYSTEM_LOADER =
        AccessController.doPrivileged(
            J2DoPrivHelper.getSystemClassLoaderAction());

    private List<ClassLoader> _loaders = new ArrayList<ClassLoader>(5);

    /**
     * Constructor; initializes the loader with an empty list of delegates.
     */
    public MultiClassLoader() {
        super(null);
    }

    /**
     * Construct with the class loaders of another multi loader.
     */
    public MultiClassLoader(MultiClassLoader other) {
        super(null);
        addClassLoaders(other);
    }

    /**
     * Returns true if the list contains the given class loader or marker.
     */
    public boolean containsClassLoader(ClassLoader loader) {
        return _loaders.contains(loader);
    }

    /**
     * Return an array of all contained class loaders.
     */
    public ClassLoader[] getClassLoaders() {
        ClassLoader[] loaders = new ClassLoader[size()];
        ClassLoader loader;
        Iterator<ClassLoader> itr = _loaders.iterator();
        for (int i = 0; i < loaders.length; i++) {
            loader = itr.next();
            if (loader == THREAD_LOADER)
                loader = AccessController.doPrivileged(
                    J2DoPrivHelper.getContextClassLoaderAction());
            loaders[i] = loader;
        }
        return loaders;
    }

    /**
     * Return the class loader at the given index.
     */
    public ClassLoader getClassLoader(int index) {
        ClassLoader loader = (ClassLoader) _loaders.get(index);
        if (loader == THREAD_LOADER)
            loader = AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction());
        return loader;
    }

    /**
     * Add the given class loader to the set of loaders that will be tried.
     *
     * @return true if the loader was added, false if already in the list
     */
    public boolean addClassLoader(ClassLoader loader) {
        if (_loaders.contains(loader))
            return false;
        return _loaders.add(loader);
    }

    /**
     * Add the given class loader at the specified index.
     *
     * @return true if the loader was added, false if already in the list
     */
    public boolean addClassLoader(int index, ClassLoader loader) {
        if (_loaders.contains(loader))
            return false;
        _loaders.add(index, loader);
        return true;
    }

    /**
     * Set the class loaders of this loader to those of the given loader.
     */
    public void setClassLoaders(MultiClassLoader multi) {
        clear();
        addClassLoaders(multi);
    }

    /**
     * Adds all class loaders from the given multi loader starting at the
     * given index.
     *
     * @return true if any loaders were added, false if all already in list
     */
    public boolean addClassLoaders(int index, MultiClassLoader multi) {
        if (multi == null)
            return false;

        // use iterator so that the thread loader is not resolved
        boolean added = false;
        for (Iterator<ClassLoader> itr = multi._loaders.iterator(); itr.hasNext();) {
            if (addClassLoader(index, (ClassLoader) itr.next())) {
                index++;
                added = true;
            }
        }
        return added;
    }

    /**
     * Adds all the class loaders from the given multi loader.
     *
     * @return true if any loaders were added, false if all already in list
     */
    public boolean addClassLoaders(MultiClassLoader multi) {
        if (multi == null)
            return false;

        // use iterator so that the thread loader is not resolved
        boolean added = false;
        for (Iterator<ClassLoader> itr = multi._loaders.iterator(); itr.hasNext();)
            added = addClassLoader((ClassLoader) itr.next()) || added;
        return added;
    }

    /**
     * Remove the given loader from the list.
     *
     * @return true if removed, false if not in list
     */
    public boolean removeClassLoader(ClassLoader loader) {
        return _loaders.remove(loader);
    }

    /**
     * Clear the list of class loaders.
     */
    public void clear() {
        _loaders.clear();
    }

    /**
     * Return the number of internal class loaders.
     */
    public int size() {
        return _loaders.size();
    }

    /**
     * Return true if there are no internal class laoders.
     */
    public boolean isEmpty() {
        return _loaders.isEmpty();
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ClassLoader loader;
        for (Iterator<ClassLoader> itr = _loaders.iterator(); itr.hasNext();) {
            loader = (ClassLoader) itr.next();
            if (loader == THREAD_LOADER)
                loader = AccessController.doPrivileged(
                    J2DoPrivHelper.getContextClassLoaderAction());
            try {
                return Class.forName(name, false, loader);
            } catch (Throwable t) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    protected URL findResource(String name) {
        ClassLoader loader;
        URL rsrc;
        for (Iterator<ClassLoader> itr = _loaders.iterator(); itr.hasNext();) {
            loader = (ClassLoader) itr.next();
            if (loader == THREAD_LOADER)
                loader = AccessController.doPrivileged(
                    J2DoPrivHelper.getContextClassLoaderAction());

            if (loader == null) // skip 
                continue;

            rsrc = AccessController.doPrivileged(
                J2DoPrivHelper.getResourceAction(loader, name)); 
            if (rsrc != null)
                return rsrc;
        }
        return null;
    }

    protected Enumeration<URL> findResources(String name) throws IOException {
        ClassLoader loader;
        Enumeration<URL> rsrcs;
        URL rsrc;
        Vector<URL> all = new Vector<URL>();
        for (Iterator<ClassLoader> itr = _loaders.iterator(); itr.hasNext();) {
            loader = itr.next();
            if (loader == THREAD_LOADER)
                loader = AccessController.doPrivileged(
                    J2DoPrivHelper.getContextClassLoaderAction());

            if (loader == null) // skip
                continue;

            try {
                rsrcs = AccessController.doPrivileged(
                    J2DoPrivHelper.getResourcesAction(loader, name)); 
                while (rsrcs.hasMoreElements()) {
                    rsrc = rsrcs.nextElement();
                    if (!all.contains(rsrc))
                        all.addElement(rsrc);
                }
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }                
        }
        return all.elements();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof MultiClassLoader))
            return false;
        return ((MultiClassLoader) other)._loaders.equals(_loaders);
    }

    public int hashCode() {
        return _loaders.hashCode();
    }
}
