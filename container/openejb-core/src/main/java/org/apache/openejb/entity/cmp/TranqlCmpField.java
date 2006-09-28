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

import org.tranql.cache.CacheRow;
import org.tranql.cache.InTxCache;
import org.tranql.ejb.CMPFieldTransform;

/**
 * @version $Revision$ $Date$
 */
public class TranqlCmpField implements CmpField, Comparable {
    private final String name;
    private final Class type;
    private final CMPFieldTransform field;

    public TranqlCmpField(String name, Class type, CMPFieldTransform field) {
        this.name = name;
        this.type = type;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public Object getValue(CmpInstanceContext ctx) {
        CacheRow cacheRow = (CacheRow) ctx.getCmpData();
        if (cacheRow == null) throw new NullPointerException("cacheRow is null");

        InTxCache inTxCache = (InTxCache) ctx.getEjbTransactionData().getCmpTxData();
        if (inTxCache == null) throw new NullPointerException("inTxCache is null");

        Object value = field.get(inTxCache, cacheRow);
        return value;
    }

    public void setValue(CmpInstanceContext ctx, Object value) {
        CacheRow cacheRow = (CacheRow) ctx.getCmpData();
        if (cacheRow == null) throw new NullPointerException("cacheRow is null");

        InTxCache inTxCache = (InTxCache) ctx.getEjbTransactionData().getCmpTxData();
        if (inTxCache == null) throw new NullPointerException("inTxCache is null");

        field.set(inTxCache, cacheRow, value);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof TranqlCmpField)) {
            return false;
        }
        return name.equals(((TranqlCmpField) object).name);
    }

    public int compareTo(Object object) {
        TranqlCmpField cmpField = (TranqlCmpField) object;
        return name.compareTo(cmpField.name);
    }
}
