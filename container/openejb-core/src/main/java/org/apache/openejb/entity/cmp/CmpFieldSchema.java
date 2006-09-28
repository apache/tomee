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

import java.io.Serializable;


/**
 * @version $Revision$ $Date$
 */
public class CmpFieldSchema implements Serializable {
    private static final long serialVersionUID = 9215428353013330791L;
    private final String fieldName;
    private String columnName;
    private String fieldTypeName;
    private boolean pkField;
    private String sqlType;
    private String typeConverterClassName;
    private boolean virtual;
    private String prefetchGroup;

    public CmpFieldSchema(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

    public void setFieldTypeName(String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }

    public boolean isPkField() {
        return pkField;
    }

    public void setPkField(boolean pkField) {
        this.pkField = pkField;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getTypeConverterClassName() {
        return typeConverterClassName;
    }

    public void setTypeConverterClassName(String typeConverterClassName) {
        this.typeConverterClassName = typeConverterClassName;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public String getPrefetchGroup() {
        return prefetchGroup;
    }

    public void setPrefetchGroup(String prefetchGroup) {
        this.prefetchGroup = prefetchGroup;
    }

    public int hashCode() {
        return fieldName.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof CmpFieldSchema) {
            CmpFieldSchema cmpSchemaCmp = (CmpFieldSchema) obj;
            return fieldName.equals(cmpSchemaCmp.fieldName);
        }
        return false;
    }

    public String toString() {
        return fieldName;
    }
}