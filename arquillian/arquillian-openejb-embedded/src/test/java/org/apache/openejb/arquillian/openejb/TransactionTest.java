/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.OpenEJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class TransactionTest {
    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap.create(WebArchive.class, TransactionTest.class.getSimpleName().concat(".war"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("ejb-jar.xml"));
    }

    @Test
    @Transactional(TransactionMode.DISABLED)
    public void noTx() throws SystemException {
        assertNull(currentTransaction());
    }

    @Test
    public void noTxAtAll() throws SystemException {
        assertNull(currentTransaction());
    }

    @Test
    @Transactional(TransactionMode.COMMIT)
    public void txCommit() throws SystemException {
        assertNotNull(currentTransaction());
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void txRollback() throws SystemException {
        assertNotNull(currentTransaction());
    }

    private Transaction currentTransaction() throws SystemException {
        return OpenEJB.getTransactionManager().getTransaction();
    }
}
