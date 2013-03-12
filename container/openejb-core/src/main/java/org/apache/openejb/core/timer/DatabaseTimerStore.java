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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.timer;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.util.Base64;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class DatabaseTimerStore implements TimerStore, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

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

    protected DatabaseTimerStore(final String serverUniqueId, final DataSource datasource, final boolean useSequence) throws SQLException {
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

    @Override
    public TimerData getTimer(final String deploymentId, final long timerId) {
        // todo not implemented
        return null;
    }

    @Override
    public Collection<TimerData> getTimers(final String deploymentId) {
        // todo not implemented
        return null;
    }

    @Override
    public TimerData createCalendarTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final ScheduleExpression schedule, final TimerConfig timerConfig)
            throws TimerStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimerData createIntervalTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final Date initialExpiration, final long intervalDuration,
            final TimerConfig timerConfig) throws TimerStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimerData createSingleActionTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final Date expiration, final TimerConfig timerConfig)
            throws TimerStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    public TimerData createTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Object info, final Date expiration, final long intervalDuration) throws TimerStoreException {
        boolean threwException = false;
        final Connection c = getConnection();
        long id;
        try {
            if (useSequence) {
                final PreparedStatement seqStatement = c.prepareStatement(sequenceSQL);
                try {
                    final ResultSet seqRS = seqStatement.executeQuery();
                    try {
                        seqRS.next();
                        id = seqRS.getLong(1);
                    } finally {
                        seqRS.close();
                    }
                } finally {
                    seqStatement.close();
                }
                final PreparedStatement insertStatement = c.prepareStatement(insertSQLWithSequence);
                try {
                    final String serializedPrimaryKey = serializeObject(primaryKey);
                    final String serializedInfo = serializeObject(info);
                    insertStatement.setLong(1, id);
                    insertStatement.setString(2, serverUniqueId);
                    insertStatement.setString(3, deploymentId);
                    insertStatement.setString(4, serializedPrimaryKey);
                    insertStatement.setString(5, serializedInfo);
                    insertStatement.setLong(6, expiration.getTime());
                    insertStatement.setLong(7, intervalDuration);
                    final int result = insertStatement.executeUpdate();
                    if (result != 1) {
                        throw new TimerStoreException("Could not insert!");
                    }
                } finally {
                    insertStatement.close();
                }
            } else {
                final PreparedStatement insertStatement = c.prepareStatement(insertSQLWithIdentity);
                try {
                    final String serializedPrimaryKey = serializeObject(primaryKey);
                    final String serializedInfo = serializeObject(info);
                    insertStatement.setString(1, serverUniqueId);
                    insertStatement.setString(2, deploymentId);
                    insertStatement.setString(3, serializedPrimaryKey);
                    insertStatement.setString(4, serializedInfo);
                    insertStatement.setLong(5, expiration.getTime());
                    insertStatement.setLong(6, intervalDuration);
                    final int result = insertStatement.executeUpdate();
                    if (result != 1) {
                        throw new TimerStoreException("Could not insert!");
                    }
                } finally {
                    insertStatement.close();
                }
                final PreparedStatement identityStatement = c.prepareStatement(identitySQL);
                try {
                    final ResultSet seqRS = identityStatement.executeQuery();
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

        //TimerData timerData = new TimerData(id, timerService, deploymentId, primaryKey, info, expiration, intervalDuration);
        //return timerData;
        return null;
    }


    /**
     * Used to restore a Timer that was cancelled, but the Transaction has been rolled back.
     */
    @Override
    public void addTimerData(final TimerData timerData) throws TimerStoreException {
        // TODO Need to verify how to handle this. Presumably, the Transaction rollback would "restore" this timer. So, no further action would be required.
        // The MemoryTimerStore does require this capability...
    }

    @Override
    public void removeTimer(final long timerId) {
        boolean threwException = false;

        Connection c = null;
        try {
            c = getConnection();
            final PreparedStatement deleteStatement = c.prepareStatement(deleteSQL);
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

    @Override
    public Collection<TimerData> loadTimers(final EjbTimerServiceImpl timerService, final String deploymentId) throws TimerStoreException {
        final Collection<TimerData> timerDatas = new ArrayList<TimerData>();
        boolean threwException = false;
        final Connection c = getConnection();
        try {
            final PreparedStatement selectStatement = c.prepareStatement(selectSQL);
            selectStatement.setString(1, serverUniqueId);
            selectStatement.setString(2, deploymentId);
            try {
                final ResultSet taskRS = selectStatement.executeQuery();
                try {
                    while (taskRS.next()) {
                        final long id = taskRS.getLong(1);
                        final String serizalizedUserId = taskRS.getString(2);
                        final Object userId = deserializeObject(serizalizedUserId);
                        final String serializedUserInfo = taskRS.getString(3);
                        final Object userInfo = deserializeObject(serializedUserInfo);
                        final long timeMillis = taskRS.getLong(4);
                        final Date time = new Date(timeMillis);
                        final long period = taskRS.getLong(5);

                        /*TimerData timerData = new TimerData(id, timerService, deploymentId, userId, userInfo, time, period);
                        timerDatas.add(timerData);*/
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

    @Override
    public void updateIntervalTimer(final TimerData timerData) {
        boolean threwException = false;
        Connection c = null;
        try {
            c = getConnection();
            final PreparedStatement updateStatement = c.prepareStatement(fixedRateUpdateSQL);
            try {
                //updateStatement.setLong(1, timerData.getExpiration().getTime());
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

    private String serializeObject(final Object object) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.close();

            final byte[] encoded = Base64.encodeBase64(baos.toByteArray());
            return new String(encoded);
        } catch (IOException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private Object deserializeObject(final String serializedValue) {
        try {
            final byte[] bytes = Base64.decodeBase64(serializedValue.getBytes());
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            final ObjectInputStream in = new ObjectInputStream(bais);
            return in.readObject();
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private void execSQL(final String sql) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement updateStatement = connection.prepareStatement(sql);
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

    private void close(final Connection connection, final boolean reportSqlException) {
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
