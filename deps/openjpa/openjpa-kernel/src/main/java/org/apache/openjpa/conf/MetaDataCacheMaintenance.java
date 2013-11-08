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
package org.apache.openjpa.conf;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.openjpa.kernel.Bootstrap;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.conf.MapConfigurationProvider;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;

/**
 * Performs maintenance tasks on the metadata caches accessible via the
 * {@link CacheMarshaller} architecture.
 *
 * @since 1.1.0
 */
public class MetaDataCacheMaintenance {

    private final BrokerFactory factory;
    private final OpenJPAConfiguration conf;
    private final boolean devpath;
    private Log log;

    public static void main(String[] args) {
        Options opts = new Options();
        args = opts.setFromCmdLine(args);
        boolean devpath = opts.getBooleanProperty("scanDevPath", "ScanDevPath",
            true);

        ConfigurationProvider cp = new MapConfigurationProvider(opts);
        BrokerFactory factory = Bootstrap.newBrokerFactory(cp, null);
        try {
            MetaDataCacheMaintenance maint = new MetaDataCacheMaintenance(
                factory, devpath);

            if (args.length != 1) {
                usage();
                return;
            }

            if ("store".equals(args[0]))
                maint.store();
            else if ("dump".equals(args[0]))
                maint.dump();
            else
                usage();
        } finally {
            factory.close();
        }
    }

    /**
     * @deprecated logging is routed to the logging system now.
     */
    public MetaDataCacheMaintenance(BrokerFactory factory, boolean devpath,
        boolean verbose) {
        this(factory, devpath);
    }

    /**
     * @param factory The {@link BrokerFactory} for which cached metadata
     * should be built.
     * @param devpath Whether or not to scan the development environment paths
     * to find persistent types to store.
     */
    public MetaDataCacheMaintenance(BrokerFactory factory, boolean devpath) {
        this.factory = factory;
        this.conf = factory.getConfiguration();
        this.devpath = devpath;
        this.log = conf.getLog(OpenJPAConfiguration.LOG_TOOL);
    }

    public void setLog(Log log) {
        this.log = log;
    }

    private static int usage() {
        // START - ALLOW PRINT STATEMENTS
        System.err.println("Usage: java MetaDataCacheMaintenance " +
            "[-scanDevPath t|f] [-<openjpa.PropertyName> value] store | dump");
        // STOP - ALLOW PRINT STATEMENTS
        return -1;
    }

    /**
     * The metadata repository for the factory that this instance was
     * constructed with will be serialized, along with any query
     * compilations etc. that have been created for the factory.
     */
    public void store() {
        MetaDataRepository repos = conf.getMetaDataRepositoryInstance();
        repos.setSourceMode(MetaDataRepository.MODE_ALL);
        Collection types = repos.loadPersistentTypes(devpath, null);
        for (Iterator iter = types.iterator(); iter.hasNext(); )
            repos.getMetaData((Class) iter.next(), null, true);

        loadQueries();

        log.info("The following data will be stored: ");
        log(repos, conf.getQueryCompilationCacheInstance());

        CacheMarshallersValue.getMarshallerById(conf, getClass().getName())
            .store(new Object[] {
                repos, conf.getQueryCompilationCacheInstance()
            });
    }

    private void loadQueries() {
        Broker broker = factory.newBroker();
        try {
            QueryMetaData[] qmds =
                conf.getMetaDataRepositoryInstance().getQueryMetaDatas();
            for (int i = 0; i < qmds.length; i++)
                loadQuery(broker, qmds[i]);
        } finally {
            broker.close();
        }
    }

    private void loadQuery(Broker broker, QueryMetaData qmd) {
        try {
            Query q = broker.newQuery(qmd.getLanguage(), null);
            qmd.setInto(q);
            q.compile();
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.warn("Skipping named query " + qmd.getName() + ": "
                    + e.getMessage(), e);
            } else {
                log.warn("Skipping named query " + qmd.getName() + ": "
                    + e.getMessage());
            }
        }
    }

    public void dump() {
        Object[] os = (Object[])
            CacheMarshallersValue.getMarshallerById(conf, getClass().getName())
            .load();
        if (os == null) {
            log.info("No cached data was found");
            return;
        }
        MetaDataRepository repos = (MetaDataRepository) os[0];
        Map qcc = (Map) os[1];

        log.info("The following data was found: ");
        log(repos, qcc);
    }

    private void log(MetaDataRepository repos, Map qcc) {
        ClassMetaData[] metas = repos.getMetaDatas();
        log.info("  Types: " + metas.length);
        if (log.isTraceEnabled())
            for (int i = 0; i < metas.length; i++)
                log.trace("    " + metas[i].getDescribedType().getName());

        QueryMetaData[] qmds = repos.getQueryMetaDatas();
        log.info("  Queries: " + qmds.length);
        if (log.isTraceEnabled())
            for (int i = 0; i < qmds.length; i++)
                log.trace("    " + qmds[i].getName() + ": "
                    + qmds[i].getQueryString());

        SequenceMetaData[] smds = repos.getSequenceMetaDatas();
        log.info("  Sequences: " + smds.length);
        if (log.isTraceEnabled())
            for (int i = 0; i < smds.length; i++)
                log.trace("    " + smds[i].getName());

        log.info("  Compiled queries: "
            + (qcc == null ? "0" : "" + qcc.size()));
        if (log.isTraceEnabled() && qcc != null)
            for (Iterator iter = qcc.keySet().iterator(); iter.hasNext(); )
                log.trace("    " + iter.next());
    }
}
