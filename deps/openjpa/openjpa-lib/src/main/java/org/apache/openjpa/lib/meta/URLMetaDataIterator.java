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

import org.apache.openjpa.lib.util.J2DoPrivHelper;

/**
 * Iterator over the metadata resource represented by a URL.
 *
 * @author Abe White
 * @nojavadoc
 */
public class URLMetaDataIterator implements MetaDataIterator {

    private final URL _url;
    private boolean _iterated = false;

    /**
     * Constructor; supply resource URL.
     */
    public URLMetaDataIterator(URL url) {
        _url = url;
    }

    public boolean hasNext() {
        return _url != null && !_iterated;
    }

    public URL next() throws IOException {
        if (!hasNext())
            throw new IllegalStateException();

        _iterated = true;
        return _url;
    }

    public InputStream getInputStream() throws IOException {
        if (!_iterated)
            throw new IllegalStateException();
        if (_url == null)
            return null;
        try {
            return AccessController.doPrivileged(
                J2DoPrivHelper.openStreamAction(_url));
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    public File getFile() {
        if (!_iterated)
            throw new IllegalStateException();
        if (_url == null)
            return null;
        File file = new File(URLDecoder.decode(_url.getPath()));
        return ((AccessController.doPrivileged(
            J2DoPrivHelper.existsAction(file))).booleanValue()) ? file:null;
    }

    public void close() {
    }
}

