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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.openjpa.lib.jdbc.DecoratingDataSource;

/**
 * A virtual datasource that contains many physical datasources.
 * 
 * @author Pinaki Poddar 
 *
 */
public class DistributedDataSource extends DecoratingDataSource implements
		Iterable<DataSource> {
	private List<DataSource> real = new ArrayList<DataSource>();
	private DataSource master;
	
	public DistributedDataSource(List<DataSource> dataSources) {
		super(dataSources.get(0));
		real = dataSources;
		master = dataSources.get(0);
	}
	
	public void addDataSource(DataSource ds) {
	    real.add(ds);
	}
	
	Connection getConnection(DataSource ds) throws SQLException {
		if (ds instanceof DecoratingDataSource)
			return getConnection(((DecoratingDataSource)ds)
			    .getInnermostDelegate());
		if (ds instanceof XADataSource)
            return ((XADataSource)ds).getXAConnection().getConnection();
		return ds.getConnection();
	}
	
	Connection getConnection(DataSource ds, String user, String pwd) 
	    throws SQLException {
		if (ds instanceof DecoratingDataSource)
			return getConnection(((DecoratingDataSource)ds)
			    .getInnermostDelegate(), user, pwd);
		if (ds instanceof XADataSource)
			return ((XADataSource)ds).getXAConnection(user, pwd)
			.getConnection();
		return ds.getConnection(user, pwd);
	}

	public Iterator<DataSource> iterator() {
		return real.iterator();
	}

	public Connection getConnection() throws SQLException {
		List<Connection> c = new ArrayList<Connection>();
		for (DataSource ds : real)
			c.add(ds.getConnection());
		return new DistributedConnection(c);
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		List<Connection> c = new ArrayList<Connection>();
		for (DataSource ds : real)
			c.add(ds.getConnection(username, password));
		return new DistributedConnection(c);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return master.getLogWriter();
	}

	public int getLoginTimeout() throws SQLException {
		return master.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		for (DataSource ds:real)
			ds.setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		for (DataSource ds:real)
			ds.setLoginTimeout(seconds);
	}
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
