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
package org.apache.openjpa.persistence;

import org.apache.openjpa.kernel.DelegatingSeq;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents a store sequence.
 *
 * @author Abe White
 * @since 0.4.1
 * @nojavadoc
 */
public class GeneratorImpl
	implements Generator {

    private final DelegatingSeq _seq;
    private final String _name;
    private final StoreContext _ctx;
    private final ClassMetaData _meta;

    /**
     * Constructor; supply delegate.
     */
    public GeneratorImpl(Seq seq, String name, StoreContext ctx,
        ClassMetaData meta) {
        _seq = new DelegatingSeq(seq, PersistenceExceptions.TRANSLATOR);
        _name = name;
        _ctx = ctx;
        _meta = meta;
    }

    /**
     * Delegate.
     */
    public Seq getDelegate() {
        return _seq.getDelegate();
    }

    public String getName() {
        return _name;
    }

    public Object next() {
        return _seq.next(_ctx, _meta);
    }

    public Object current() {
        return _seq.current(_ctx, _meta);
    }

    public void allocate(int additional) {
        _seq.allocate(additional, _ctx, _meta);
    }

    public int hashCode() {
        return ((_seq == null) ? 0  : _seq.hashCode());
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if ((other == null) || (other.getClass() != this.getClass()))
            return false;
        if (_seq == null)
            return false;

        return _seq.equals(((GeneratorImpl) other)._seq);
    }
}
