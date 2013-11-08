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
package org.apache.openjpa.jdbc.meta;

import java.util.Comparator;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.meta.Order;

/**
 * Order by value columns, which are the primary key columns in the case
 * of a relation field.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class JDBCValueOrder
    implements JDBCOrder {

    private final FieldMapping _fm;
    private final boolean _asc;
    private static final DBIdentifier SQL_ELEMENT = DBIdentifier.newColumn(Order.ELEMENT);

    public JDBCValueOrder(FieldMapping fm, boolean asc) {
        _fm = fm;
        _asc = asc;
    }

    public String getName() {
        return Order.ELEMENT;
    }

    public DBIdentifier getIdentifier() {
        return SQL_ELEMENT;
    }

    public boolean isAscending() {
        return _asc;
    }

    public Comparator<?> getComparator() {
        return null;
    }

    public boolean isInRelation() {
        return _fm.getElement().getTypeMetaData() != null;
    }

    public void order(Select sel, ClassMapping elem, Joins joins) {
        if (elem != null)
            sel.orderBy(elem.getPrimaryKeyColumns(), _asc, joins, false);
        else
            sel.orderBy(_fm.getElementMapping().getColumns(), _asc,
                joins, false);
    }
}
