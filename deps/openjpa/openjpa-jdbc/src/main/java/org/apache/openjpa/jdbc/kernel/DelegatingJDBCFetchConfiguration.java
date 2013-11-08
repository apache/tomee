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
package org.apache.openjpa.jdbc.kernel;

import java.util.Collection;
import java.util.Set;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.kernel.DelegatingFetchConfiguration;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

///////////////////////////////////////////////////////////////
// NOTE: when adding a public API method, be sure to add it to 
// JDO and JPA facades!
///////////////////////////////////////////////////////////////

/**
 * Delegating fetch configuration that can also perform exception
 * transation for use in facades.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class DelegatingJDBCFetchConfiguration
    extends DelegatingFetchConfiguration
    implements JDBCFetchConfiguration {

    /**
     * Constructor; supply delegate.
     */
    public DelegatingJDBCFetchConfiguration(JDBCFetchConfiguration delegate) {
        super(delegate);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingJDBCFetchConfiguration(JDBCFetchConfiguration delegate,
        RuntimeExceptionTranslator trans) {
        super(delegate, trans);
    }

    /**
     * Return the JDBC delegate.
     */
    public JDBCFetchConfiguration getJDBCDelegate() {
        return (JDBCFetchConfiguration) getDelegate();
    }

    public int getEagerFetchMode() {
        try {
            return getJDBCDelegate().getEagerFetchMode();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setEagerFetchMode(int mode) {
        try {
            getJDBCDelegate().setEagerFetchMode(mode);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getSubclassFetchMode() {
        try {
            return getJDBCDelegate().getSubclassFetchMode();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getSubclassFetchMode(ClassMapping cls) {
        try {
            return getJDBCDelegate().getSubclassFetchMode(cls);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setSubclassFetchMode(int mode) {
        try {
            getJDBCDelegate().setSubclassFetchMode(mode);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getResultSetType() {
        try {
            return getJDBCDelegate().getResultSetType();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setResultSetType(int type) {
        try {
            getJDBCDelegate().setResultSetType(type);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getFetchDirection() {
        try {
            return getJDBCDelegate().getFetchDirection();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setFetchDirection(int direction) {
        try {
            getJDBCDelegate().setFetchDirection(direction);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getLRSSize() {
        try {
            return getJDBCDelegate().getLRSSize();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setLRSSize(int lrsSize) {
        try {
            getJDBCDelegate().setLRSSize(lrsSize);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getJoinSyntax() {
        try {
            return getJDBCDelegate().getJoinSyntax();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setJoinSyntax(int syntax) {
        try {
            getJDBCDelegate().setJoinSyntax(syntax);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Set getJoins() {
        try {
            return getJDBCDelegate().getJoins();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasJoin(String field) {
        try {
            return getJDBCDelegate().hasJoin(field);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration addJoin(String field) {
        try {
            getJDBCDelegate().addJoin(field);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration addJoins(Collection fields) {
        try {
            getJDBCDelegate().addJoins(fields);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration removeJoin(String field) {
        try {
            getJDBCDelegate().removeJoin(field);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration removeJoins(Collection fields) {
        try {
            getJDBCDelegate().removeJoins(fields);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration clearJoins() {
        try {
            getJDBCDelegate().clearJoins();
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int getIsolation() {
        try {
            return getJDBCDelegate().getIsolation();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration setIsolation(int level) {
        try {
            getJDBCDelegate().setIsolation(level);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration traverseJDBC(FieldMetaData fm) {
        try {
            return getJDBCDelegate().traverseJDBC(fm);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Set getFetchInnerJoins() {
        try {
            return getJDBCDelegate().getFetchInnerJoins();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasFetchInnerJoin(String field) {
        try {
            return getJDBCDelegate().hasFetchInnerJoin(field);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration addFetchInnerJoin(String field) {
        try {
            getJDBCDelegate().addFetchInnerJoin(field);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public JDBCFetchConfiguration addFetchInnerJoins(Collection fields) {
        try {
            getJDBCDelegate().addFetchInnerJoins(fields);
            return this;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    @Override
    public void setIgnoreDfgForFkSelect(boolean b) {
        try {
            getJDBCDelegate().setIgnoreDfgForFkSelect(b);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    @Override
    public boolean getIgnoreDfgForFkSelect() {
        try {
            return getJDBCDelegate().getIgnoreDfgForFkSelect();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
}
