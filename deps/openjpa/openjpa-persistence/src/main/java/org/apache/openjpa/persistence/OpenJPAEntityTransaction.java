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

import javax.persistence.EntityTransaction;

/**
 * Extension of the JPA {@link EntityTransaction} interface.
 *
 * @since 1.0.0
 * @published
 */
public interface OpenJPAEntityTransaction
    extends EntityTransaction {

    /**
     * Issue a commit and then start a new transaction. This is identical to:
     * <pre> manager.commit (); manager.begin ();
     * </pre> except that the entity manager's internal atomic lock is utilized,
     * so this method can be safely executed from multiple threads.
     *
     * @see javax.persistence.EntityTransaction#commit()
     * @see javax.persistence.EntityTransaction#begin()
     */
    public void commitAndResume();

    /**
     * Issue a rollback and then start a new transaction. This is identical to:
     * <pre> manager.rollback (); manager.begin ();
     * </pre> except that the entity manager's internal atomic lock is utilized,
     * so this method can be safely executed from multiple threads.
     *
     * @see javax.persistence.EntityTransaction#rollback()
     * @see javax.persistence.EntityTransaction#begin()
     */
    public void rollbackAndResume();

    /**
     * Mark the current transaction for rollback with the specified cause
     * of the rollback.
     *
     * @since 0.9.7
     */
    public void setRollbackOnly(Throwable cause);

    /**
     * Returns the Throwable that caused the transaction to be
     * marked for rollback.
     *
     * @return the Throwable, or null if none was given
     *
     * @since 0.9.7
     */
    public Throwable getRollbackCause();
}
