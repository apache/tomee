/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.core.timer;

import com.thoughtworks.xstream.XStream;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.openejb.util.Logger;

public class DatabaseTimerStore implements TimerStore {
    private static final Logger log = Logger.getInstance("Timer", "org.apache.openejb.util.resources");

    private static final String createSequenceSQL = "create sequence timertasks_seq";
    private static final String createTableSQLWithSequence = "create table timertasks (id long primary key, serverid varchar(256) not null, timerkey varchar(256) not null, userid varchar(4096), userinfo varchar(4096), firsttime long not null, period long)";
    private static final String createTableSQLWithIdentity = "create table timertasks (id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), serverid varchar(256) not null, timerkey varchar(256) not null, userid varchar(4096), userinfo varchar(4096), firsttime NUMERIC(18,0) not null, period NUMERIC(18, 0))";
    private static final String sequenceSQL = "select timertasks_seq.nextval";
    private static final String identitySQL = "values IDENTITY_VAL_LOCAL()";
    private static final String insertSQLWithSequence = "insert into timertasks (id, serverid, timerkey, userid, userinfo, firsttime, period) values (?, ?, ?, ?, ?, ?, ?)";
    private static final String insertSQLWithIdentity = "insert into timertasks (serverid, timerkey, userid, userinfo, firsttime, period) values (?, ?, ?, ?, ?, ?)";
    private static final String deleteSQL = "delete from timertasks where id=?";
    private static final String selectSQL = "select id, userid, userinfo, firsttime, period from timertasks where serverid = ? and timerkey=?";
    private static final String fixedRateUpdateSQL = "update timertasks set firsttime = ? where id = ?";

    private final String serverUniqueId;
    private final DataSource dataSource;
    private boolean useSequence = false;

    protected DatabaseTimerStore(String serverUniqueId, DataSource datasource, boolean useSequence) throws SQLException {
        this.serverUniqueId = serverUniqueId;
        this.dataSource = datasource;
        this.useSequence = useSequence;
        if (this.useSequence) {
            execSQL(createSequenceSQL);
            execSQL(createTableSQLWithSequence);
        } else {
            execSQL(createTableSQLWithIdentity);
        }
    }

    public TimerData getTimer(String deploymentId, long timerId) {
        // todo not implemented
        return null;
    }

    public Collection<TimerData> getTimers(String deploymentId) {
        // todo not implemented
        return null;
    }

    public TimerData createTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Object info, Date expiration, long intervalDuration) throws TimerStoreException {
        boolean threwException = false;
        Connection c = getConnection();
        long id;
        try {
            if (useSequence) {
                PreparedStatement seqStatement = c.prepareStatement(sequenceSQL);
                try {
                    ResultSet seqRS = seqStatement.executeQuery();
                    try {
                        seqRS.next();
                        id = seqRS.getLong(1);
                    } finally {
                        seqRS.close();
                    }
                } finally {
                    seqStatement.close();
                }
                PreparedStatement insertStatement = c.prepareStatement(insertSQLWithSequence);
                try {
                    String serializedPrimaryKey = serializeObject(primaryKey);
                    String serializedInfo = serializeObject(info);
                    insertStatement.setLong(1, id);
                    insertStatement.setString(2, serverUniqueId);
                    insertStatement.setString(3, deploymentId);
                    insertStatement.setString(4, serializedPrimaryKey);
                    insertStatement.setString(5, serializedInfo);
                    insertStatement.setLong(6, expiration.getTime());
                    insertStatement.setLong(7, intervalDuration);
                    int result = insertStatement.executeUpdate();
                    if (result != 1) {
                        throw new TimerStoreException("Could not insert!");
                    }
                } finally {
                    insertStatement.close();
                }
            } else {
                PreparedStatement insertStatement = c.prepareStatement(insertSQLWithIdentity);
                try {
                    String serializedPrimaryKey = serializeObject(primaryKey);
                    String serializedInfo = serializeObject(info);
                    insertStatement.setString(1, serverUniqueId);
                    insertStatement.setString(2, deploymentId);
                    insertStatement.setString(3, serializedPrimaryKey);
                    insertStatement.setString(4, serializedInfo);
                    insertStatement.setLong(5, expiration.getTime());
                    insertStatement.setLong(6, intervalDuration);
                    int result = insertStatement.executeUpdate();
                    if (result != 1) {
                        throw new TimerStoreException("Could not insert!");
                    }
                } finally {
                    insertStatement.close();
                }
                PreparedStatement identityStatement = c.prepareStatement(identitySQL);
                try {
                    ResultSet seqRS = identityStatement.executeQuery();
                    try {
                        seqRS.next();
                        id = seqRS.getLong(1);
                    } finally {
                        seqRS.close();
                    }
                } finally {
                    identityStatement.close();
                }
            }
        } catch (SQLException e) {
            threwException = true;
            throw new TimerStoreException(e);
        } finally {
            close(c, !threwException);
        }

        TimerData timerData = new TimerData(id, timerService, deploymentId, primaryKey, info, expiration, intervalDuration);
        return timerData;
    }

    public void removeTimer(long timerId) {
        boolean threwException = false;

        Connection c = null;
        try {
            c = getConnection();
            PreparedStatement deleteStatement = c.prepareStatement(deleteSQL);
            try {
                deleteStatement.setLong(1, timerId);
                deleteStatement.execute();
            } finally {
                deleteStatement.close();
            }
        } catch (TimerStoreException e) {
            log.warning("Unable to remove get a database connection", e);
        } catch (SQLException e) {
            threwException = true;
            log.warning("Unable to remove timer data from database", e);
        } finally {
            close(c, !threwException);
        }
    }

    public Collection<TimerData> loadTimers(EjbTimerServiceImpl timerService, String deploymentId) throws TimerStoreException {
        Collection<TimerData> timerDatas = new ArrayList<TimerData>();
        boolean threwException = false;
        Connection c = getConnection();
        try {
            PreparedStatement selectStatement = c.prepareStatement(selectSQL);
            selectStatement.setString(1, serverUniqueId);
            selectStatement.setString(2, deploymentId);
            try {
                ResultSet taskRS = selectStatement.executeQuery();
                try {
                    while (taskRS.next()) {
                        long id = taskRS.getLong(1);
                        String serizalizedUserId = taskRS.getString(2);
                        Object userId = deserializeObject(serizalizedUserId);
                        String serializedUserInfo = taskRS.getString(3);
                        Object userInfo = deserializeObject(serializedUserInfo);
                        long timeMillis = taskRS.getLong(4);
                        Date time = new Date(timeMillis);
                        long period = taskRS.getLong(5);

                        TimerData timerData = new TimerData(id, timerService, deploymentId, userId, userInfo, time, period);
                        timerDatas.add(timerData);
                    }
                } finally {
                    taskRS.close();
                }
            } finally {
                selectStatement.close();
            }
        } catch (SQLException e) {
            threwException = true;
            throw new TimerStoreException(e);
        } finally {
            close(c, !threwException);
        }
        return timerDatas;
    }

    public void updateIntervalTimer(TimerData timerData) {
        boolean threwException = false;
        Connection c = null;
        try {
            c = getConnection();
            PreparedStatement updateStatement = c.prepareStatement(fixedRateUpdateSQL);
            try {
                updateStatement.setLong(1, timerData.getExpiration().getTime());
                updateStatement.setLong(2, timerData.getId());
                updateStatement.execute();
            } finally {
                updateStatement.close();
            }
        } catch (TimerStoreException e) {
            log.warning("Unable to remove get a database connection", e);
        } catch (SQLException e) {
            threwException = true;
            log.warning("Unable to remove timer data from database", e);
        } finally {
            close(c, !threwException);
        }
    }

    private String serializeObject(Object object) {
        XStream xstream = new XStream();
        String serializaedValue = xstream.toXML(object);
        return serializaedValue;
    }

    private Object deserializeObject(String serializedValue) {
        XStream xstream = new XStream();
        Object object = xstream.fromXML(serializedValue);
        return object;
    }

    private void execSQL(String sql) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            PreparedStatement updateStatement = connection.prepareStatement(sql);
            try {
                updateStatement.execute();
            } catch (SQLException e) {
                //ignore... table already exists.
            } finally {
                updateStatement.close();
            }
        } finally {
            connection.close();
        }
    }

    private Connection getConnection() throws TimerStoreException {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            throw new TimerStoreException(e);
        }
    }

    private void close(Connection connection, boolean reportSqlException) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                if (!reportSqlException) {
                    log.warning("Unable to close database connection", e) ;
                }
            }
        }
    }
}
