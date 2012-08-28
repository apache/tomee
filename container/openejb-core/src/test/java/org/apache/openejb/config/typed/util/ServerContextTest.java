/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.typed.util;

import junit.framework.TestCase;
import org.apache.openejb.config.typed.ActiveMQResourceAdapterBuilder;
import org.apache.openejb.config.typed.DataSourceBuilder;
import org.apache.openejb.config.typed.JmsConnectionFactoryBuilder;
import org.apache.openejb.config.typed.SecurityServiceBuilder;
import org.apache.openejb.config.typed.StatelessContainerBuilder;
import org.apache.openejb.config.typed.TransactionManagerBuilder;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class ServerContextTest extends TestCase {

    public void test() throws Exception {
        final ServerContext serverContext = new ServerContext();


        serverContext.createTransactionManager(new TransactionManagerBuilder()
                .withDefaultTransactionTimeout(3, TimeUnit.MINUTES)
                .withBufferSizeKb(1024)
                .withMaxBuffers(10));

        serverContext.createSecurityService(new SecurityServiceBuilder()
                .withDefaultUser("unknown"));

        serverContext.createContainer(new StatelessContainerBuilder()
                .withStrictPooling(true)
                .withMaxSize(11)
                .withMinSize(5)
                .withReplaceAged(true)
                .withMaxAge(1, TimeUnit.DAYS)
                .withIdleTimeout(30, TimeUnit.MINUTES)
                .withSweepInterval(3, TimeUnit.MINUTES)
        );

        serverContext.createResource(new DataSourceBuilder()
                .id("FooDataSource")
                .withJtaManaged(true)
                .withJdbcDriver("org.hsqldb.jdbcDriver")
                .withJdbcUrl(new URI("jdbc:hsqldb:mem:hsqldb"))
                .withAccessToUnderlyingConnectionAllowed(false)
                .withMaxActive(10)
                .withMaxIdle(5)
                .withMinEvictableIdleTime(15, TimeUnit.MINUTES)
                .withTimeBetweenEvictionRuns(5, TimeUnit.MINUTES)
        );

        serverContext.createResource(new ActiveMQResourceAdapterBuilder()
                .id("JmsResourceAdapter")
                .withDataSource("FooDataSource")
        );

        serverContext.createResource(new JmsConnectionFactoryBuilder()
                .id("FooJmsConnectionFactory")
                .withResourceAdapter("JmsResourceAdapter")
        );
    }
}
