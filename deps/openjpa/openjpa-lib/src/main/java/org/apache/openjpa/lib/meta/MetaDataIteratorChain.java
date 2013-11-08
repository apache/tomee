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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Metadata iterator that combines several iterators.
 *
 * @author Abe White
 * @nojavadoc
 */
public class MetaDataIteratorChain implements MetaDataIterator {

    private List<MetaDataIterator> _itrs = null;
    private int _cur = -1;
    private MetaDataIterator _itr = null;

    /**
     * Default constructor.
     */
    public MetaDataIteratorChain() {
    }

    /**
     * Combine two iterators.
     */
    public MetaDataIteratorChain(MetaDataIterator itr1, MetaDataIterator itr2) {
        _itrs = new ArrayList<MetaDataIterator>(2);
        _itrs.add(itr1);
        _itrs.add(itr2);
    }

    /**
     * Add an iterator to the chain.
     */
    public void addIterator(MetaDataIterator itr) {
        if (_cur != -1)
            throw new IllegalStateException();
        if (_itrs == null)
            _itrs = new ArrayList<MetaDataIterator>(4);
        _itrs.add(itr);
    }

    public boolean hasNext() throws IOException {
        if (_itrs == null)
            return false;
        if (_cur == -1)
            _cur = 0;

        MetaDataIterator itr;
        for (; _cur < _itrs.size(); _cur++) {
            itr = (MetaDataIterator) _itrs.get(_cur);
            if (itr.hasNext()) {
                _itr = itr;
                return true;
            }
        }
        _itr = null;
        return false;
    }

    public Object next() throws IOException {
        if (!hasNext())
            throw new NoSuchElementException();
        return _itr.next();
    }

    public InputStream getInputStream() throws IOException {
        if (_itr == null)
            throw new IllegalStateException();
        return _itr.getInputStream();
    }

    public File getFile() throws IOException {
        if (_itr == null)
            throw new IllegalStateException();
        return _itr.getFile();
    }

    public void close() {
        if (_itrs != null) {
            for(MetaDataIterator mdi: _itrs) {
                mdi.close();
            }
        }
    }
}
