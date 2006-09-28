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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tranql.cache.CacheRow;
import org.tranql.cache.DuplicateIdentityException;
import org.tranql.cache.InTxCache;
import org.tranql.identity.GlobalIdentity;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGeneratorException;
import org.tranql.pkgenerator.SQLPrimaryKeyGenerator;
import org.tranql.ql.QueryBindingImpl;
import org.tranql.sql.jdbc.binding.BindingFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @version $Revision$ $Date$
 */
public class SQLPrimaryKeyGeneratorWrapper implements PrimaryKeyGenerator {
    private static final Log log = LogFactory.getLog(SQLPrimaryKeyGeneratorWrapper.class);

    private final String initSql;
    private final String sql;
    private final Class returnType;
    private PrimaryKeyGenerator delegate;
    private final DataSource dataSource;

    public SQLPrimaryKeyGeneratorWrapper(String initSql, String sql, Class returnType, DataSource dataSource) {
        this.initSql = initSql;
        this.sql = sql;
        this.returnType = returnType;
        this.dataSource = dataSource;
    }

    public void doStart() throws Exception {

        Connection c = dataSource.getConnection();
        try {
            PreparedStatement updateStatement = c.prepareStatement(initSql);
            try {
                updateStatement.execute();
            } catch (SQLException e) {
                log.warn("Can not initialize SQLPrimaryKeyGeneratorWrapper with query " + initSql, e);
                //ignore... Already existing?
            } finally {
                updateStatement.close();
            }
        } finally {
            c.close();
        }

        delegate = new SQLPrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, returnType)));
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
