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
package org.apache.openejb.entity.cmp.pkgenerator;

import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.tranql.cache.CacheRow;
import org.tranql.cache.DuplicateIdentityException;
import org.tranql.cache.InTxCache;
import org.tranql.identity.GlobalIdentity;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGeneratorException;
import org.tranql.pkgenerator.SequenceTablePrimaryKeyGenerator;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * @version $Revision$ $Date$
 */
public class SequenceTablePrimaryKeyGeneratorWrapper implements PrimaryKeyGenerator {
    private final TransactionManager transactionManager;
    private final ConnectionFactorySource connectionFactoryWrapper;
    private final String tableName;
    private final String sequenceName;
    private final int batchSize;
    private SequenceTablePrimaryKeyGenerator delegate;

    public SequenceTablePrimaryKeyGeneratorWrapper(TransactionManager transactionManager, ConnectionFactorySource connectionFactoryWrapper, String tableName, String sequenceName, int batchSize) {
        this.transactionManager = transactionManager;
        this.connectionFactoryWrapper = connectionFactoryWrapper;
        this.tableName = tableName;
        this.sequenceName = sequenceName;
        this.batchSize = batchSize;
    }

    public void doStart() throws Exception {
        DataSource dataSource = (DataSource) connectionFactoryWrapper.$getResource();
        delegate = new SequenceTablePrimaryKeyGenerator(transactionManager, dataSource, tableName, sequenceName, batchSize);
        delegate.initSequenceTable();
    }

    public void doStop() throws Exception {
        delegate = null;
    }

    public void doFail() {
        delegate = null;
    }

    public Object getNextPrimaryKey(CacheRow cacheRow) throws PrimaryKeyGeneratorException {
        return delegate.getNextPrimaryKey(cacheRow);
    }

    public CacheRow updateCache(InTxCache cache, GlobalIdentity id, CacheRow cacheRow) throws DuplicateIdentityException {
        return delegate.updateCache(cache, id, cacheRow);
    }
}
