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

import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.ObjectNotFoundException;
import org.apache.openjpa.util.StoreException;

/**
 * Abstract implementation of {@link PCResultObjectProvider}
 * that implements {@link ResultObjectProvider#getResultObject}
 * by assembling the necessary information about the object to be loaded.
 *
 * @author Patrick Linskey
 */
public abstract class AbstractPCResultObjectProvider
    implements PCResultObjectProvider {

    /**
     * The {@link StoreContext} that this result object
     * provider will load objects into.
     */
    protected final StoreContext ctx;

    /**
     * Create a new provider for loading PC objects from the input
     * into <code>ctx</code>.
     */
    public AbstractPCResultObjectProvider(StoreContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Return the context this provider was constructed with.
     */
    public StoreContext getContext() {
        return ctx;
    }

    public void initialize(OpenJPAStateManager sm, PCState state,
        FetchConfiguration fetch)
        throws Exception {
        sm.initialize(getPCType(), state);
        load(sm, fetch);
    }

    public Object getResultObject()
        throws Exception {
        Class type = getPCType();
        MetaDataRepository repos = ctx.getConfiguration().
            getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getMetaData
            (type, ctx.getClassLoader(), true);

        Object oid = getObjectId(meta);
        Object res = ctx.find(oid, null, null, this, 0);
        if (res == null)
            throw new ObjectNotFoundException(oid);
        return res;
    }

    /**
     * Implement this method to extract the object id value from the
     * current record of the input.
     */
    protected abstract Object getObjectId(ClassMetaData meta)
        throws Exception;

    /**
     * Implement this method to extract the type of the pc stored
     * in the current record of the input.
     */
    protected abstract Class getPCType()
        throws Exception;

    /**
     * Load data from the current input record into the given state
     * manager. Remember to call {@link OpenJPAStateManager#setVersion} to set
     * the optimistic versioning information, if it has any.
     */
    protected abstract void load(OpenJPAStateManager sm, 
        FetchConfiguration fetch)
        throws Exception;

    /**
     * Override if desired. Does nothing by default.
     */
    public void open()
        throws Exception {
    }

    /**
     * Override if desired. Returns false by default.
     *
     * @see ResultObjectProvider#supportsRandomAccess
     */
    public boolean supportsRandomAccess() {
        return false;
    }

    /**
     * Implement this method to advance the input.
     *
     * @see ResultObjectProvider#next
     */
    public abstract boolean next()
        throws Exception;

    /**
     * Override if desired. Throws an exception by default.
     *
     * @see ResultObjectProvider#absolute
     */
    public boolean absolute(int pos)
        throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Override if desired. Returns {@link Integer#MAX_VALUE} by default.
     *
     * @see ResultObjectProvider#size
     */
    public int size()
        throws Exception {
        return Integer.MAX_VALUE;
    }

    /**
     * Override if desired. Throws an exception by default.
     *
     * @see ResultObjectProvider#reset
     */
    public void reset()
        throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Override if desired. Does nothing by default.
     *
     * @see ResultObjectProvider#close
     */
    public void close()
        throws Exception {
    }

    /**
     * Throws a {@link StoreException} by default.
     */
    public void handleCheckedException(Exception e) {
        throw new StoreException (e);
	}
}
