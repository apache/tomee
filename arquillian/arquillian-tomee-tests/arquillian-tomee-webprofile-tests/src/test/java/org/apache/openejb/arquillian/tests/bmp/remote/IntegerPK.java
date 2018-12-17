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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * The base class for a primary key for an integer type.
 */
public class IntegerPK extends Number {
    private static final long serialVersionUID = 351946466791640696L;
    private int m_value;

    public IntegerPK(final int value) {
        m_value = value;
    }

    public int getValue() {
        return m_value;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException exc) {
            throw new RuntimeException("Cannot clone", exc);
        }
    }

    public boolean equals(final Object o) {
        if (o == null)
            return false;
        return m_value == ((IntegerPK) o).m_value;
    }

    public int compareTo(final Object o) {
        return (int) (m_value - ((IntegerPK) o).m_value);
    }

    public int hashCode() {
        return m_value;
    }

    public String toString() {
        return String.valueOf(m_value);
    }

    // Number interface
    public byte byteValue() {
        return (byte) m_value;
    }

    public short shortValue() {
        return (short) m_value;
    }

    public int intValue() {
        return m_value;
    }

    public long longValue() {
        return m_value;
    }

    public float floatValue() {
        return m_value;
    }

    public double doubleValue() {
        return m_value;
    }

    /**
     * For ParmType interface
     */
    public void add(final int i, final PreparedStatement stat) throws SQLException {
        stat.setInt(i, m_value);
    }

    public int getType() {
        return Types.INTEGER;
    }
}
