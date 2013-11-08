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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.MultiClassLoader;

/**
 * Iterator over a given metadata resource.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ResourceMetaDataIterator implements MetaDataIterator {

    private List<URL> _urls = null;
    private int _url = -1;

    /**
     * Constructor; supply the resource to parse.
     */
    public ResourceMetaDataIterator(String rsrc) throws IOException {
        this(rsrc, null);
    }

    /**
     * Constructor; supply the resource to parse.
     */
    public ResourceMetaDataIterator(String rsrc, ClassLoader loader)
        throws IOException {
        if (loader == null) {
            MultiClassLoader multi = AccessController
                .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
            multi.addClassLoader(MultiClassLoader.SYSTEM_LOADER);
            multi.addClassLoader(MultiClassLoader.THREAD_LOADER);
            multi.addClassLoader(getClass().getClassLoader());
            loader = multi;
        }

        try {
            Enumeration<URL> e = AccessController.doPrivileged(
                J2DoPrivHelper.getResourcesAction(loader, rsrc));
            while (e.hasMoreElements()) {
                if (_urls == null)
                    _urls = new ArrayList<URL>(3);
                _urls.add(e.nextElement());
            }
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    public boolean hasNext() {
        return _urls != null && _url + 1 < _urls.size();
    }

    public URL next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return _urls.get(++_url);
    }

    public InputStream getInputStream() throws IOException {
        if (_url == -1 || _url >= _urls.size())
            throw new IllegalStateException();
        try {
            return AccessController.doPrivileged(
                J2DoPrivHelper.openStreamAction(_urls.get(_url)));
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    public File getFile() throws IOException {
        if (_url == -1 || _url >= _urls.size())
            throw new IllegalStateException();
        File file = new File(URLDecoder.decode((_urls.get(_url)).getFile()));
        return ((AccessController.doPrivileged(
            J2DoPrivHelper.existsAction(file))).booleanValue()) ? file :null;
    }

    public void close() {
    }
}

