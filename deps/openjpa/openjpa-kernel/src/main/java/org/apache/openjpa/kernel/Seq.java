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

import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Internal OpenJPA sequence interface.
 *
 * @author Abe White
 */
public interface Seq
    extends Closeable {

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_NONTRANSACTIONAL = 1;
    public static final int TYPE_TRANSACTIONAL = 2;
    public static final int TYPE_CONTIGUOUS = 3;

    /**
     * Set the type of sequence.
     */
    public void setType(int type);

    /**
     * Return the next value in the sequence.
     *
     * @param ctx the current context
     * @param cls if this is a datastore identity sequence, the
     * persistent class the identity value is for; else null
     */
    public Object next(StoreContext ctx, ClassMetaData cls);

    /**
     * Return the current value of the sequence, or null if not available.
     *
     * @param ctx the current context
     * @param cls if this is a datastore identity sequence, the
     * persistent class the identity value is for; else null
     */
    public Object current(StoreContext ctx, ClassMetaData cls);

    /**
     * Allocate additional values efficiently.
     *
     * @param ctx the current context
     * @param cls if this is a datastore identity sequence, the
     * persistent class the identity value is for; else null
     */
    public void allocate(int additional, StoreContext ctx, ClassMetaData cls);

    /**
     * Free resources used by this sequence.
     */
    public void close ();
}
