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

import org.apache.openejb.quartz.JobDetail;
import org.apache.openejb.quartz.ScheduleBuilder;
import org.apache.openejb.quartz.TriggerKey;
import org.apache.openejb.quartz.impl.jdbcjobstore.CronTriggerPersistenceDelegate;
import org.apache.openejb.quartz.impl.jdbcjobstore.Util;
import org.apache.openejb.quartz.spi.MutableTrigger;
import org.apache.openejb.quartz.spi.OperableTrigger;

import jakarta.ejb.ScheduleExpression;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EJBCronTriggerPersistenceDelegate extends CronTriggerPersistenceDelegate {
    @Override
    public String getHandledTriggerTypeDiscriminator() {
        return "EJB_CRON";
    }

    @Override
    public boolean canHandleTriggerType(final OperableTrigger trigger) {
        return trigger instanceof EJBCronTrigger;
    }

    @Override
    public TriggerPropertyBundle loadExtendedTriggerProperties(final Connection conn, final TriggerKey triggerKey) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(Util.rtp(SELECT_CRON_TRIGGER, tablePrefix, schedNameLiteral));
            ps.setString(1, triggerKey.getName());
            ps.setString(2, triggerKey.getGroup());
            rs = ps.executeQuery();

            if (rs.next()) {
                final String cronExpr = rs.getString(COL_CRON_EXPRESSION);
                final String timeZoneId = rs.getString(COL_TIME_ZONE_ID);

                final String[] parts = cronExpr.split(EJBCronTrigger.DELIMITER);
                try {
                    final EJBCronTrigger cb = new EJBCronTrigger(new ScheduleExpression()
                        .year(parts[0])
                        .month(parts[1])
                        .dayOfMonth(parts[2])
                        .dayOfWeek(parts[3])
                        .hour(parts[4])
                        .minute(parts[5])
                        .second(parts[6])
                        .timezone(timeZoneId));
                    return new TriggerPropertyBundle(new EJBCronTriggerSceduleBuilder(cb), null, null);
                } catch (final EJBCronTrigger.ParseException e) {
                    throw new IllegalStateException("Can't build the Trigger with key: '" + triggerKey + "' and statement: " + Util.rtp(SELECT_CRON_TRIGGER, tablePrefix, schedNameLiteral));
                }
            }

            throw new IllegalStateException("No record found for selection of Trigger with key: '" + triggerKey + "' and statement: " + Util.rtp(SELECT_CRON_TRIGGER, tablePrefix, schedNameLiteral));
        } finally {
            Util.closeResultSet(rs);
            Util.closeStatement(ps);
        }
    }

    @Override
    public int insertExtendedTriggerProperties(final Connection conn, final OperableTrigger trigger,
                                               final String state, final JobDetail jobDetail) throws SQLException, IOException {
        final EJBCronTrigger cronTrigger = (EJBCronTrigger) trigger;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(Util.rtp(INSERT_CRON_TRIGGER, tablePrefix, schedNameLiteral));
            ps.setString(1, trigger.getKey().getName());
            ps.setString(2, trigger.getKey().getGroup());
            ps.setString(3, cronTrigger.getRawValue());
            ps.setString(4, cronTrigger.getTimeZone().getID());
            return ps.executeUpdate();
        } finally {
            Util.closeStatement(ps);
        }
    }

    @Override
    public int updateExtendedTriggerProperties(final Connection conn, final OperableTrigger trigger,
                                               final String state, final JobDetail jobDetail) throws SQLException, IOException {
        final EJBCronTrigger cronTrigger = (EJBCronTrigger) trigger;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(Util.rtp(UPDATE_CRON_TRIGGER, tablePrefix, schedNameLiteral));
            ps.setString(1, cronTrigger.getRawValue());
            ps.setString(2, cronTrigger.getTimeZone().getID());
            ps.setString(3, trigger.getKey().getName());
            ps.setString(4, trigger.getKey().getGroup());
            return ps.executeUpdate();
        } finally {
            Util.closeStatement(ps);
        }
    }

    private static class EJBCronTriggerSceduleBuilder extends ScheduleBuilder<EJBCronTrigger> {
        private final EJBCronTrigger trigger;

        public EJBCronTriggerSceduleBuilder(final EJBCronTrigger trig) {
            trigger = trig;
        }

        @Override
        protected MutableTrigger build() {
            return trigger;
        }
    }
}
