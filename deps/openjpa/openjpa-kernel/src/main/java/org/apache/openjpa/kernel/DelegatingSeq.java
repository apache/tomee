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
package org.apache.openjpa.kernel;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

///////////////////////////////////////////////////////////////
// NOTE: when adding a public API method, be sure to add it to 
// JDO and JPA facades!
///////////////////////////////////////////////////////////////

/**
 * Delegating sequence that can also perform exception translation for use
 * in facades.
 *
 * @author Abe White
 * @nojavadoc
 */
public class DelegatingSeq
    implements Seq {

    private final Seq _seq;
    private final DelegatingSeq _del;
    private final RuntimeExceptionTranslator _trans;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingSeq(Seq seq) {
        this(seq, null);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingSeq(Seq seq, RuntimeExceptionTranslator trans) {
        _seq = seq;
        if (seq instanceof DelegatingSeq)
            _del = (DelegatingSeq) seq;
        else
            _del = null;
        _trans = trans;
    }

    /**
     * Return the direct delegate.
     */
    public Seq getDelegate() {
        return _seq;
    }

    /**
     * Return the native delegate.
     */
    public Seq getInnermostDelegate() {
        return (_del == null) ? _seq : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingSeq)
            other = ((DelegatingSeq) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public void setType(int type) {
        try {
            _seq.setType(type);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object next(StoreContext ctx, ClassMetaData meta) {
        try {
            return _seq.next(ctx, meta);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object current(StoreContext ctx, ClassMetaData meta) {
        try {
            return _seq.current(ctx, meta);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void allocate(int additional, StoreContext ctx, ClassMetaData meta) {
        try {
            _seq.allocate(additional, ctx, meta);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void close() {
        try {
            _seq.close();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}
}
