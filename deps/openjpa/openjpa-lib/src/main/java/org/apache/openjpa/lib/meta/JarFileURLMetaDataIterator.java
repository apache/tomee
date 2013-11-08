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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.openjpa.lib.util.J2DoPrivHelper;

/**
 * Iterator over all metadata resources in a given resource addressed by a jar:file URL.
 * 
 */
public class JarFileURLMetaDataIterator implements MetaDataIterator, MetaDataFilter.Resource {
    private final MetaDataFilter _filter;
    private final JarFile _jarFile;
    private final JarEntry _jarTargetEntry;

    private int index = 0;
    private JarEntry _last = null;
    private final ArrayList<JarEntry> _entryList = new ArrayList<JarEntry>();

    public JarFileURLMetaDataIterator(URL url, MetaDataFilter filter) throws IOException {
        if (url == null) {
            _jarFile = null;
            _jarTargetEntry = null;
        } else {
            JarURLConnection jarURLConn = (JarURLConnection) url.openConnection();
            jarURLConn.setDefaultUseCaches(false);

            try {
                _jarFile = AccessController.doPrivileged(J2DoPrivHelper.getJarFileAction(jarURLConn));
                _jarTargetEntry = AccessController.doPrivileged(J2DoPrivHelper.getJarEntryAction(jarURLConn));

                if (_jarTargetEntry.isDirectory()) {
                    Enumeration<JarEntry> jarEntryEnum = _jarFile.entries();
                    while (jarEntryEnum.hasMoreElements()) {
                        JarEntry jarEntry = jarEntryEnum.nextElement();
                        if (jarEntry.getName().startsWith(_jarTargetEntry.getName())) {
                            _entryList.add(jarEntry);
                        }
                    }
                } else {
                    _entryList.add(_jarTargetEntry);
                }
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }
        }

        _filter = filter;
    }

    /**
     * Return whether there is another resource to iterate over.
     */
    public boolean hasNext() throws IOException {
        if (_entryList.size() <= index) {
            return false;
        }

        // Search for next metadata file
        while (index < _entryList.size()) {
            if (_filter != null && !_filter.matches(this)) {
                index++;
                continue;
            }
            break;
        }

        return (index < _entryList.size());
    }

    /**
     * Return the next metadata resource.
     */
    public Object next() throws IOException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        String ret = _entryList.get(index).getName();
        _last = _entryList.get(index);
        index++;

        return ret;
    }

    /**
     * Return the last-iterated metadata resource content as a stream.
     */
    public InputStream getInputStream() throws IOException {
        if (_last == null)
            throw new IllegalStateException();
        return _jarFile.getInputStream(_last);
    }

    /**
     * Return the last-iterated metadata resource content as a file, or null if not an extant file.
     */
    public File getFile() throws IOException {
        if (_last == null)
            throw new IllegalStateException();
        return null;
    }

    /**
     * Close the resources used by this iterator.
     */
    public void close() {
        try {
            if (_jarFile != null)
                _jarFile.close();
        } catch (IOException ioe) {
        }
    }

    // ////////////////////////////////////////
    // MetaDataFilter.Resource implementation
    // ////////////////////////////////////////

    /**
     * The name of the resource.
     */
    public String getName() {
        if (index < _entryList.size()) {
            return _entryList.get(index).getName();
        } else {
            return null;
        }
    }

    /**
     * Resource content.
     */
    public byte[] getContent() throws IOException {
        if (_entryList.size() <= index) {
            return new byte[0];
        }

        long size = _entryList.get(index).getSize();
        if (size == 0)
            return new byte[0];

        InputStream in = _jarFile.getInputStream(_entryList.get(index));
        byte[] content;
        if (size < 0) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int r; (r = in.read(buf)) != -1; bout.write(buf, 0, r))
                ;
            content = bout.toByteArray();
        } else {
            content = new byte[(int) size];
            int offset = 0;
            int read;
            while (offset < size && (read = in.read(content, offset, (int) size - offset)) != -1) {
                offset += read;
            }
        }
        in.close();
        return content;
    }
}
