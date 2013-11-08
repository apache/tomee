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
package org.apache.openjpa.lib.rop;

import org.apache.openjpa.lib.util.Closeable;

/**
 * Interface that allows lazy/custom instantiation of input objects.
 * {@link ResultList} objects do not necessarily load in data all
 * at once. Instead, they may lazily load objects as necessary. So,
 * the lifespan of a {@link ResultObjectProvider} instance is
 * related to how the application deals with processing the
 * {@link ResultList} created with a given
 * {@link ResultObjectProvider} instance.
 *
 * @author Marc Prud'hommeaux
 * @author Patrick Linskey
 * @author Abe White
 */
public interface ResultObjectProvider extends Closeable {

    /**
     * Return true if this provider supports random access.
     */
    public boolean supportsRandomAccess();

    /**
     * Open the result. This will be called before
     * {@link #next}, {@link #absolute}, or {@link #size}.
     */
    public void open() throws Exception;

    /**
     * Instantiate the current result object. This method will only be
     * called after {@link #next} or {@link #absolute}.
     */
    public Object getResultObject() throws Exception;

    /**
     * Advance the input to the next position. Return <code>true</code> if
     * there is more data; otherwise <code>false</code>.
     */
    public boolean next() throws Exception;

    /**
     * Move to the given 0-based position. This method is
     * only called for providers that support random access.
     * Return <code>true</code> if there is data at this position;
     * otherwise <code>false</code>. This may be invoked in place of
     * {@link #next}.
     */
    public boolean absolute(int pos) throws Exception;

    /**
     * Return the number of items in the input, or {@link Integer#MAX_VALUE}
     * if the size in unknown.
     */
    public int size() throws Exception;

    /**
     * Reset this provider. This is an optional operation. If supported,
     * it should move the position of the provider to before the first
     * element. Non-random-access providers may be able to support this
     * method by re-acquiring all resources as if the result were just opened.
     */
    public void reset() throws Exception;

    /**
     * Free the resources associated with this provider.
     */
    public void close() throws Exception;

    /**
     * Any checked exceptions that are thrown will be passed to this method.
     * The provider should re-throw the exception as an appropriate unchecked
     * exception.
     */
    public void handleCheckedException(Exception e);
}
