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
package org.apache.openjpa.jdbc.meta.strats;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Strategy for classes that aren't mapped.
 *
 * @author Abe White
 */
public class NoneClassStrategy
    extends AbstractClassStrategy {

    public static final String ALIAS = "none";

    private static final NoneClassStrategy _instance = new NoneClassStrategy();

    private static final Localizer _loc = Localizer.forPackage
        (NoneClassStrategy.class);

    /**
     * Return the singleton instance.
     */
    public static NoneClassStrategy getInstance() {
        return _instance;
    }

    /**
     * Hide constructor.
     */
    private NoneClassStrategy() {
    }

    public String getAlias() {
        return ALIAS;
    }

    public void setClassMapping(ClassMapping owner) {
    }

    public void insert(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        throwFlushException(sm);
    }

    public void update(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        throwFlushException(sm);
    }

    public void delete(OpenJPAStateManager sm, JDBCStore store, RowManager rm)
        throws SQLException {
        throwFlushException(sm);
    }

    /**
     * Throw appropriate exception on attempt to flush an unmapped object.
     */
    private static void throwFlushException(OpenJPAStateManager sm) {
        throw new InvalidStateException(_loc.get("flush-virtual",
            sm.getMetaData(), sm.getObjectId())).
            setFailedObject(sm.getManagedInstance()).
            setFatal(true);
    }
}
