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
package org.openejb.entity.cmp;

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
