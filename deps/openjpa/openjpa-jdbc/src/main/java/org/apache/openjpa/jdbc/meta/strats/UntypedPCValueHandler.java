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

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.ImplHelper;

/**
 * Handler for unknown persistence-capable object fields that stores
 * stringified oids.
 *
 * @nojavadoc
 */
public class UntypedPCValueHandler
    extends AbstractValueHandler
    implements RelationId {

    private static final Localizer _loc = Localizer.forPackage
        (UntypedPCValueHandler.class);
    private static final UntypedPCValueHandler _instance =
        new UntypedPCValueHandler();

    /**
     * Singleton instance.
     */
    public static UntypedPCValueHandler getInstance() {
        return _instance;
    }

    /**
     * @deprecated
     */
    public Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt) {
        DBDictionary dict = vm.getMappingRepository().getDBDictionary();
        DBIdentifier colName = DBIdentifier.newColumn(name, dict != null ? dict.delimitAll() : false);
        return map(vm, colName, io, adapt);
    }

    public Column[] map(ValueMapping vm, DBIdentifier name, ColumnIO io,
        boolean adapt) {
        Column col = new Column();
        col.setIdentifier(name);
        col.setJavaType(JavaTypes.STRING);
        col.setRelationId(true);
        return new Column[]{ col };
    }

    public boolean isVersionable(ValueMapping vm) {
        return true;
    }

    public boolean objectValueRequiresLoad(ValueMapping vm) {
        return true;
    }

    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        // in the past we've been lenient about being able to translate objects
        // from other persistence contexts, so try to get sm directly from
        // instance before asking our context
        if (ImplHelper.isManageable(val)) {
            PersistenceCapable pc = ImplHelper.toPersistenceCapable(val,
                store.getConfiguration());
            if (pc.pcGetStateManager() != null)
                return pc.pcGetStateManager();
        }
        return RelationStrategies.getStateManager(val, store.getContext());
    }

    public Object toObjectValue(ValueMapping vm, Object val,
        OpenJPAStateManager sm, JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException {
        if (val == null)
            return null;

        String str = (String) val;
        int idx = str.indexOf(':');
        if (idx == -1)
            throw new StoreException(_loc.get("oid-invalid", str, vm));
        String clsName = str.substring(0, idx);
        String oidStr = str.substring(idx + 1);

        StoreContext ctx = store.getContext();
        ClassLoader loader = store.getConfiguration().
            getClassResolverInstance().getClassLoader(vm.getType(),
            ctx.getClassLoader());
        Class cls = null;
        try {
            cls = Class.forName(clsName, true, loader);
        } catch (ClassNotFoundException cnfe) {
            throw new StoreException(cnfe);
        }

        Object oid = ctx.newObjectId(cls, oidStr);
        return store.find(oid, vm, fetch);
    }

    public Object toRelationDataStoreValue(OpenJPAStateManager sm, Column col) {
        if (sm == null || sm.getObjectId() == null)
            return null;
        return sm.getMetaData().getDescribedType().getName() + ":"
            + sm.getObjectId().toString();
    }
}
