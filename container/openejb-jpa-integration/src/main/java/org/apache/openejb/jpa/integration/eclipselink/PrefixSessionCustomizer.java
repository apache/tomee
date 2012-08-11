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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jpa.integration.eclipselink;

import org.apache.openejb.jpa.integration.JPAThreadContext;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.sessions.Session;

import java.util.Properties;

public class PrefixSessionCustomizer implements SessionCustomizer {
    @Override
    public void customize(final Session session) throws Exception {
        if (JPAThreadContext.infos.containsKey("properties")) {
            final String prefix = ((Properties) JPAThreadContext.infos.get("properties")).getProperty("openejb.jpa.table_prefix");
            for (ClassDescriptor cd : session.getDescriptors().values()) {
                for (DatabaseTable table : cd.getTables()) {
                    table.setName(prefix + table.getName());
                }
            }
        }
    }
}
