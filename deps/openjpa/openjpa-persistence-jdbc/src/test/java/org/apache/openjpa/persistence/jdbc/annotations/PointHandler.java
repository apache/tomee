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
package org.apache.openjpa.persistence.jdbc.annotations;

import java.awt.*;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.strats.AbstractValueHandler;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Handler for {@link Point}. This is a sample of how to create custom
 * handlers, and is in this package for unit testing purposes.
 */
public class PointHandler extends AbstractValueHandler {

    /**
     * Create columns with default names and java type values.  Kodo will
     * fill in the rest of the information and merge in any information given
     * in the user's mapping data.  If the user does not give column names,
     * Kodo will alter your default names as necessary to avoid conflicts and
     * meet the database's name limitations.
     *
     * @param    name    default base name for columns
     */
    public Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt) {
        Column xcol = new Column();
        xcol.setName("X" + name);
        xcol.setJavaType(JavaTypes.INT);
        Column ycol = new Column();
        ycol.setName("Y" + name);
        ycol.setJavaType(JavaTypes.INT);
        return new Column[]{ xcol, ycol };
    }

    /**
     * Return whether the column value is an exact value that can be used
     * in state-comparison versioning.
     */
    public boolean isVersionable() {
        return true;
    }

    /**
     * Convert the object value to its datastore equivalent.
     */
    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        if (val == null)
            return null;

        Point p = (Point) val;
        return new Object[]{ p.x, p.y };
    }

    /**
     *  Convert the datastore value to its object equivalent.
     */
    public Object toObjectValue(ValueMapping vm, Object val) {
        Object[] vals = (Object[]) val;
        if (vals[0] == null || vals[1] == null)
            return null;

        int x = ((Number) vals[0]).intValue();
        int y = ((Number) vals[1]).intValue();
        return new Point(x, y);
    }
}
