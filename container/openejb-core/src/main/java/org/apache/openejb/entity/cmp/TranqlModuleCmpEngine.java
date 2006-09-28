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
package org.apache.openejb.entity.cmp;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.openejb.proxy.ProxyInfo;
import org.tranql.builder.IdentityDefinerBuilder;
import org.tranql.builder.SQLQueryBuilder;
import org.tranql.cache.CacheTable;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBSchema;
import org.tranql.intertxcache.Cache;
import org.tranql.intertxcache.FrontEndCache;
import org.tranql.intertxcache.FrontEndCacheDelegate;
import org.tranql.intertxcache.FrontEndToCacheAdaptor;
import org.tranql.sql.SQLSchema;

/**
 * @version $Revision$ $Date$
 */
public class TranqlModuleCmpEngine implements ModuleCmpEngine {
    private EJBSchema ejbSchema;
    private GlobalSchema globalSchema;
    private SQLSchema sqlSchema;
    private TransactionManager transactionManager;
    private final TranqlCommandBuilder tranqlCommandBuilder;
    private final FrontEndCacheDelegate cacheDelegate = new FrontEndCacheDelegate();

    public TranqlModuleCmpEngine(ModuleSchema moduleSchema,
                                 TransactionManager transactionManager,
                                 ConnectionProxyFactory connectionFactory,
                                 ClassLoader classLoader) throws Exception {
        if (moduleSchema == null) {
            throw new NullPointerException("moduleSchema is null");
        }
        if (connectionFactory == null) {
            throw new NullPointerException("connectionFactory is null");
        }
        if (classLoader == null) {
            throw new NullPointerException("classLoader is null");
        }

        this.transactionManager = transactionManager;

        DataSource dataSource = (DataSource) connectionFactory.$getResource();
        if (dataSource == null) {
            throw new IllegalStateException("Connection factory returned a null data source");
        }

        TranqlSchemaBuilder tranqlSchemaBuilder = new TranqlSchemaBuilder(moduleSchema,
                dataSource,
                transactionManager,
                classLoader);
        tranqlSchemaBuilder.buildSchema();
        this.globalSchema = tranqlSchemaBuilder.getGlobalSchema();
        this.ejbSchema = tranqlSchemaBuilder.getEjbSchema();
        this.sqlSchema = tranqlSchemaBuilder.getSqlSchema();

        SQLQueryBuilder queryBuilder = new SQLQueryBuilder(ejbSchema, sqlSchema, globalSchema);
        IdentityDefinerBuilder identityDefinerBuilder = new IdentityDefinerBuilder(ejbSchema, globalSchema);
        tranqlCommandBuilder = new TranqlCommandBuilder(globalSchema, transactionManager, identityDefinerBuilder, queryBuilder);
    }

    public EjbCmpEngine getEjbCmpEngine(String ejbName, Class beanClass, ProxyInfo proxyInfo) throws Exception {
        EJB ejb = ejbSchema.getEJB(ejbName);
        if (ejb == null) {
            throw new IllegalArgumentException("Schema does not contain EJB: " + ejbName);
        }

        CacheTable cacheTable = globalSchema.getCacheTable(ejbName);
        Cache cache = cacheTable.getCacheFactory().factory();
        FrontEndCache frontEndCache = new FrontEndToCacheAdaptor(transactionManager, cache);
        cacheDelegate.addFrontEndCache(ejbName, frontEndCache);

        return new TranqlEjbCmpEngine(ejb, beanClass, proxyInfo, frontEndCache, cacheTable, tranqlCommandBuilder);
    }

    public QueryManager getQueryManager() {
        return new TranqlQueryManager(ejbSchema, sqlSchema, globalSchema);
    }
}
