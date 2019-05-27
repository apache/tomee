/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.bmp.remote;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A primary key in a Database table with a String representation (varchar).
 */
public class StringPK implements Serializable {
    private static final long serialVersionUID = -6429314795121119701L;
    private String m_value;

    public StringPK(final String s) {
        m_value = s;
    }

    public String getValue() {
        return m_value;
    }

    public boolean equals(final Object o) {
        if (o instanceof StringPK) {
            final StringPK s = (StringPK) o;
            if (m_value == null)
                return s.m_value == null;
            return m_value.equals(s.m_value);
        } else {
            return false;
        }
    }

    public int compareTo(final Object o) {
        return ("" + m_value).compareTo(((StringPK) o).m_value);
    }

    public int hashCode() {
        return ("" + m_value).hashCode();
    }

    public String toString() {
        if (m_value == null)
            return "NULL";
        return '\'' + m_value + '\'';
    }

    public void add(final int i, final PreparedStatement stat) throws SQLException {
        stat.setString(i, m_value);
    }

    public int getType() {
        return Types.VARCHAR;
    }
}
