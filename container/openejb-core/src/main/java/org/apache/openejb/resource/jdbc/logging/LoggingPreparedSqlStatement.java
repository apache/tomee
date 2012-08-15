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
package org.apache.openejb.resource.jdbc.logging;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingPreparedSqlStatement extends AbstractSQLLogger implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SQL, LoggingPreparedSqlStatement.class);

    private final PreparedStatement delegate;
    private final String sql;
    private final List<Parameter> parameters = new ArrayList<Parameter>();

    public LoggingPreparedSqlStatement(final PreparedStatement result, final String query) {
        delegate = result;
        sql= query;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String mtdName = method.getName();
        final boolean execute = mtdName.startsWith("execute");

        final TimeWatcherExecutor.TimerWatcherResult result = TimeWatcherExecutor.execute(method, delegate, args, execute);

        if (mtdName.startsWith("set") && args.length >= 2 && (args[0].getClass().equals(Integer.TYPE) || args[0].getClass().equals(Integer.class))) {
            parameters.add(new Parameter(mtdName.substring(3), (Integer) args[0], args[1]));
        } else if (execute) {
            String str = sql;
            if (str.contains("?")) {
                Collections.sort(parameters);
                for (Parameter param : parameters) {
                    try {
                        str = str.replaceFirst("\\?", param.value.toString());
                    } catch (Exception e) {
                        str = str.replaceFirst("\\?", param.value.getClass().getName());
                    }
                }
            }
            LOGGER.info(format(str, result.getDuration()));
        }

        return result.getResult();
    }

    protected static class Parameter implements Comparable<Parameter> {
        private String type;
        private int key;
        private Object value;

        public Parameter(String type, int key, Object value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(final Parameter o) {
            return key - o.key;
        }

        @Override
        public String toString() {
            return value + " (" + type + ")";
        }
    }
}
