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
import java.util.Map;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Version strategy that delegates to the suerpclass version.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SuperclassVersionStrategy
    extends AbstractVersionStrategy {

    public void afterLoad(OpenJPAStateManager sm, JDBCStore store) {
        vers.getClassMapping().getPCSuperclassMapping().getVersion().
            afterLoad(sm, store);
    }

    public boolean checkVersion(OpenJPAStateManager sm, JDBCStore store,
        boolean updateVersion)
        throws SQLException {
        return vers.getClassMapping().getPCSuperclassMapping().getVersion().
            checkVersion(sm, store, updateVersion);
    }

    public int compareVersion(Object v1, Object v2) {
        return vers.getClassMapping().getPCSuperclassMapping().getVersion().
            compareVersion(v1, v2);
    }

    public Map getBulkUpdateValues() {
        return vers.getClassMapping().getPCSuperclassMapping().getVersion()
            .getBulkUpdateValues();
    }
}
