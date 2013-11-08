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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Stores the class name along with each database object record.
 *
 * @author Abe White
 */
public class ClassNameDiscriminatorStrategy
    extends InValueDiscriminatorStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (ClassNameDiscriminatorStrategy.class);

    public static final String ALIAS = "class-name";

    public String getAlias() {
        return ALIAS;
    }

    protected int getJavaType() {
        return JavaTypes.STRING;
    }

    protected Object getDiscriminatorValue(ClassMapping cls) {
        return cls.getDescribedType().getName();
    }

    protected Class getClass(Object val, JDBCStore store)
        throws ClassNotFoundException {
        ClassLoader loader = getClassLoader(store);
        return Class.forName((String) val, true, loader);
    }

    public void loadSubclasses(JDBCStore store)
        throws SQLException, ClassNotFoundException {
        if (isFinal) {
            disc.setSubclassesLoaded(true);
            return;
        }

        Column col = disc.getColumns()[0];
        DBDictionary dict = store.getDBDictionary();
        JDBCFetchConfiguration fetch = store.getFetchConfiguration();
        SQLBuffer select = dict.toSelect(new SQLBuffer(dict).append(col),
            fetch, new SQLBuffer(dict).append(col.getTable()), null, null,
            null, null, true, false, 0, Long.MAX_VALUE);

        Log log = disc.getMappingRepository().getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("load-subs", col.getTable().getFullName()));

        ClassLoader loader = getClassLoader(store);
        Connection conn = store.getConnection();
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = select.prepareStatement(conn);
            dict.setTimeouts(stmnt, fetch, false);
            rs = stmnt.executeQuery();
            String className;
            while (rs.next()) {
                className = dict.getString(rs, 1);
                if (StringUtils.isEmpty(className))
                    throw new ClassNotFoundException(_loc.get("no-class-name",
                        disc.getClassMapping(), col).getMessage());
                Class.forName(className, true, loader);
            }
            disc.setSubclassesLoaded(true);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException se) {
                }
            if (stmnt != null)
                try {
                    stmnt.close();
                } catch (SQLException se) {
                }
            try {
                conn.close();
            } catch (SQLException se) {
            }
        }
    }

    /**
     * Return the class loader to use for loading class names.
     */
    private ClassLoader getClassLoader(JDBCStore store) {
        return store.getConfiguration().getClassResolverInstance().
            getClassLoader(disc.getClassMapping().getDescribedType(),
                store.getContext().getClassLoader());
    }
}
