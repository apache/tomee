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
package org.apache.openjpa.jdbc.kernel.exps;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.meta.XMLMetaData;

/**
 * The simplified public view of any non-operator in a query filter,
 * including constants, variables, and object fields.
 *
 * @author Abe White
 */
public interface FilterValue {

    /**
     * Return the expected type of this value.
     */
    public Class getType();

    /**
     * Return the number of SQL elements in this value. Usually 1.
     */
    public int length();

    /**
     * Append the first SQL element for this value to the given buffer.
     */
    public void appendTo(SQLBuffer buf);

    /**
     * Append the <code>index</code>th SQL element for this value to the
     * given buffer.
     */
    public void appendTo(SQLBuffer buf, int index);

    /**
     * Return the alias to use for the given column (this includes the table
     * alias prefix, if any).
     */
    public String getColumnAlias(Column col);

    /**
     * Return the alias to use for the given column (this includes the table
     * alias prefix, if any).
     */
    public String getColumnAlias(String col, Table table);

    /**
     * Transform the given value into its datastore equivalent.
     */
    public Object toDataStoreValue(Object val);

    /**
     * Return true if this value represents a literal or parameter.
     */
    public boolean isConstant();

    /**
     * If this is a constant, return its value, else return null.
     */
    public Object getValue();

    /**
     * If this is a constant, returns its value as it would be represented
     * in the database in this context, else return null.
     */
    public Object getSQLValue();

    /**
     * Return true if this value represents a persistent field traversal,
     * such as 'this', 'address.street', or 'projectVariable.title'.
     */
    public boolean isPath();

    /**
     * If this is a path to a persistent object, return its class mapping,
     * else return null.
     */
    public ClassMapping getClassMapping();

    /**
     * If this is a path to a persistent field, return its mapping, else
     * return null.
     */
    public FieldMapping getFieldMapping();
    
    /**
     * If this is an XPath, return it,
     * else return null;
     */
    public PCPath getXPath();
    
    /**
     * If this is an XPath, return XML mapping metadata,
     * else return null;
     */
    public XMLMetaData getXmlMapping();

    /**
     * return true if CAST is required for this filter value
     * else return false.
     */
    public boolean requiresCast();
}
