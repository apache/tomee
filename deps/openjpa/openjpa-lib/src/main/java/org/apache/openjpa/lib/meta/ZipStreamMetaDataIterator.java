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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Iterator over all metadata resources in a given zip input stream.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ZipStreamMetaDataIterator
    implements MetaDataIterator, MetaDataFilter.Resource {

    private final ZipInputStream _stream;
    private final MetaDataFilter _filter;
    private ZipEntry _entry = null;
    private ZipEntry _last = null;
    private byte[] _buf = null;

    /**
     * Constructor; supply zip stream and optional metadata filter.
     */
    public ZipStreamMetaDataIterator(ZipInputStream stream,
        MetaDataFilter filter) {
        _stream = stream;
        _filter = filter;
    }

    public boolean hasNext() throws IOException {
        if (_stream == null)
            return false;
        if (_entry != null)
            return true;

        // close last rsrc
        if (_buf == null && _last != null)
            _stream.closeEntry();
        _last = null;
        _buf = null;

        // search for next file
        ZipEntry entry;
        while (_entry == null && (entry = _stream.getNextEntry()) != null) {
            _entry = entry;
            if (_filter != null && !_filter.matches(this)) {
                _entry = null;
                _stream.closeEntry();
            }
        }
        return _entry != null;
    }

    public String next() throws IOException {
        if (!hasNext())
            throw new NoSuchElementException();
        String ret = _entry.getName();
        _last = _entry;
        _entry = null;
        return ret;
    }

    public InputStream getInputStream() {
        if (_last == null)
            throw new IllegalStateException();

        if (_buf != null)
            return new ByteArrayInputStream(_buf);
        return new NoCloseInputStream();
    }

    public File getFile() {
        return null;
    }

    public void close() {
        try {
            _stream.close();
        } catch (IOException ioe) {
        }
    }

    //////////////////////////////////////////
    // MetaDataFilter.Resource implementation
    //////////////////////////////////////////

    public String getName() {
        return _entry.getName();
    }

    public byte[] getContent() throws IOException {
        // buffer content so that future calls to getInputStream can read
        // the same data
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int r; (r = _stream.read(buf)) != -1; bout.write(buf, 0, r)) ;
        _buf = bout.toByteArray();
        _stream.closeEntry();
        return _buf;
    }

    /**
     * Non-closing input stream used to make sure the underlying zip
     * stream is not closed.
     */
    private class NoCloseInputStream extends InputStream {

        public int available() throws IOException {
            return _stream.available();
        }

        public int read() throws IOException {
            return _stream.read();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return _stream.read(b, off, len);
        }

        public void close() {
        }
    }
}

