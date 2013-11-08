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
package org.apache.openjpa.slice.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * A virtual connection that contains multiple physical connections.
 * 
 * @author Pinaki Poddar
 * 
 */
public class DistributedConnection implements Connection {

	private final List<Connection> real;
	private final Connection master;

	public DistributedConnection(List<Connection> connections) {
		if (connections == null || connections.isEmpty())
			throw new NullPointerException();
		real = connections;
		master = connections.get(0);
	}
    
	public boolean contains(Connection c) {
		return real.contains(c);
	}

	public void clearWarnings() throws SQLException {
		for (Connection c : real)
			c.clearWarnings();
	}

	public void close() throws SQLException {
		for (Connection c : real)
			c.close();
	}

	public void commit() throws SQLException {
		for (Connection c : real)
			c.commit();
	}

	public Statement createStatement() throws SQLException {
		DistributedStatement ret = new DistributedStatement(this);
		for (Connection c : real) {
			ret.add(c.createStatement());
		}
		return ret;
	}

    public Statement createStatement(int arg0, int arg1) throws SQLException {
		DistributedStatement ret = new DistributedStatement(this);
		for (Connection c : real) {
			ret.add(c.createStatement(arg0, arg1));
		}
		return ret;
	}

	public Statement createStatement(int arg0, int arg1, int arg2)
			throws SQLException {
		DistributedStatement ret = new DistributedStatement(this);
		for (Connection c : real) {
			ret.add(c.createStatement(arg0, arg1, arg2));
		}
		return ret;
	}

	public boolean getAutoCommit() throws SQLException {
		return master.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return master.getCatalog();
	}

	public int getHoldability() throws SQLException {
		return master.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return master.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return master.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return master.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return master.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		boolean ret = true;
		for (Connection c : real) {
			ret &= c.isClosed();
		}
		return ret;
	}

	public boolean isReadOnly() throws SQLException {
		boolean ret = true;
		for (Connection c : real) {
			ret &= c.isReadOnly();
		}
		return ret;
	}

	public String nativeSQL(String arg0) throws SQLException {
		return master.nativeSQL(arg0);
	}

	public CallableStatement prepareCall(String arg0) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public CallableStatement prepareCall(String arg0, int arg1, int arg2)
			throws SQLException {
		throw new UnsupportedOperationException();
	}

	public CallableStatement prepareCall(String arg0, int arg1, int arg2,
			int arg3) throws SQLException {
		throw new UnsupportedOperationException();
	}

    public PreparedStatement prepareStatement(String arg0) throws SQLException {
		// TODO: Big hack
        if (arg0.indexOf("OPENJPA_SEQUENCE_TABLE") != -1) {
			return master.prepareStatement(arg0);
        }
		DistributedPreparedStatement ret = new DistributedPreparedStatement(this);
		for (Connection c : real) {
			ret.add(c.prepareStatement(arg0));
		}
		return ret;
	}

	public PreparedStatement prepareStatement(String arg0, int arg1)
			throws SQLException {
		DistributedPreparedStatement ret = new DistributedPreparedStatement(this);
		for (Connection c : real) {
			ret.add(c.prepareStatement(arg0, arg1));
		}
		return ret;
	}

	public PreparedStatement prepareStatement(String arg0, int[] arg1)
			throws SQLException {
		DistributedPreparedStatement ret = new DistributedPreparedStatement(this);
		for (Connection c : real) {
			ret.add(c.prepareStatement(arg0, arg1));
		}
		return ret;
	}

	public PreparedStatement prepareStatement(String arg0, String[] arg1)
			throws SQLException {
		DistributedPreparedStatement ret = new DistributedPreparedStatement(this);
		for (Connection c : real) {
			ret.add(c.prepareStatement(arg0, arg1));
		}
		return ret;
	}

    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2)
			throws SQLException {
		DistributedPreparedStatement ret = new DistributedPreparedStatement(this);
		for (Connection c : real) {
			ret.add(c.prepareStatement(arg0, arg1, arg2));
		}
		return ret;
	}

    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2,
			int arg3) throws SQLException {
		DistributedPreparedStatement ret = new DistributedPreparedStatement(this);
		for (Connection c : real) {
			ret.add(c.prepareStatement(arg0, arg1, arg2));
		}
		return ret;
	}

	public void releaseSavepoint(Savepoint arg0) throws SQLException {
		for (Connection c : real)
			c.releaseSavepoint(arg0);
	}

	public void rollback() throws SQLException {
		for (Connection c : real)
			c.rollback();
	}

	public void rollback(Savepoint arg0) throws SQLException {
		for (Connection c : real)
			c.rollback(arg0);
	}

	public void setAutoCommit(boolean arg0) throws SQLException {
		for (Connection c : real)
			c.setAutoCommit(arg0);
	}

	public void setCatalog(String arg0) throws SQLException {
		for (Connection c : real)
			c.setCatalog(arg0);
	}

	public void setHoldability(int arg0) throws SQLException {
		for (Connection c : real)
			c.setHoldability(arg0);
	}

	public void setReadOnly(boolean arg0) throws SQLException {
		for (Connection c : real)
			c.setReadOnly(arg0);
	}

	public Savepoint setSavepoint() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public Savepoint setSavepoint(String arg0) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public void setTransactionIsolation(int arg0) throws SQLException {
		for (Connection c : real)
			c.setTransactionIsolation(arg0);
	}

	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		for (Connection c : real)
			c.setTypeMap(arg0);
	}

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClientInfo(String arg0) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(int arg0) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(Properties arg0) throws SQLClientInfoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(String arg0, String arg1)
        throws SQLClientInfoException {
        throw new UnsupportedOperationException();
    }
    
    // Java 7 methods follow
    
    public void abort(Executor executor) throws SQLException {
    	throw new UnsupportedOperationException();
    }
    
    public int getNetworkTimeout() throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException{
    	throw new UnsupportedOperationException();
    }
    
    public String getSchema() throws SQLException {
    	throw new UnsupportedOperationException(); 
    }
    
    public void setSchema(String schema)throws SQLException {
    	throw new UnsupportedOperationException();
    }
}
