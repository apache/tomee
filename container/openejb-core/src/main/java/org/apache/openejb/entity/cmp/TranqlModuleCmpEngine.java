/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
