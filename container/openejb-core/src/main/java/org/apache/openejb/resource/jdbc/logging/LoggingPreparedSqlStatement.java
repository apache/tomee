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

public class LoggingPreparedSqlStatement implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SQL, LoggingPreparedSqlStatement.class);

    private final PreparedStatement delegate;
    private final String sql;
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    private int parameterIndex;

    public LoggingPreparedSqlStatement(final PreparedStatement result, final String query) {
        delegate = result;
        sql = query;
        parameterIndex = 0;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String mtdName = method.getName();
        final boolean execute = mtdName.startsWith("execute");

        final TimeWatcherExecutor.TimerWatcherResult result = TimeWatcherExecutor.execute(method, delegate, args, execute);

        if (mtdName.startsWith("set") && args.length >= 2 && (args[0].getClass().equals(Integer.TYPE) || args[0].getClass().equals(Integer.class))) {
            parameters.add(new Parameter(mtdName.substring(3), parameterIndex, (Integer) args[0], args[1]));
        } else if (execute) {
            String str = sql;
            if (str.contains("?")) {
                Collections.sort(parameters);
                int lastBatch = 0;
                for (int i = 0; i < parameters.size(); i++) {
                    final Parameter param = parameters.get(i);
                    if (str.contains("?")) {
                        try {
                            str = str.replaceFirst("\\?", param.value.toString());
                        } catch (final Exception e) {
                            if (param.value == null) {
                                str = str.replaceFirst("\\?", "null");
                            } else {
                                str = str.replaceFirst("\\?", param.value.getClass().getName());
                            }
                        }
                        lastBatch = param.batchIndex;
                    } else {
                        if (lastBatch != param.batchIndex) {
                            str += ", (";
                            lastBatch = param.batchIndex;
                        }

                        try {
                            str += param.value.toString();
                        } catch (Exception e) {
                            if (param.value == null) {
                                str += "null";
                            } else {
                                str += param.value.getClass().getName();
                            }
                        }

                        if (i == parameters.size() - 1 || parameters.get(i + 1).batchIndex != lastBatch) {
                            str += ")";
                        } else {
                            str += ",";
                        }
                    }
                }
            }
            LOGGER.info(result.format(str));
        } else if ("clearParameters".equals(mtdName)) {
            parameters.clear();
            parameterIndex = 0;
        } else if ("addBatch".equals(mtdName)) {
            parameterIndex++;
        }

        if (result.getThrowable() != null) throw result.getThrowable();
        return result.getResult();
    }

    protected static class Parameter implements Comparable<Parameter> {
        private final String type;
        private final int batchIndex;
        private final int key;
        private final Object value;

        public Parameter(String type, int batchIdx, int key, Object value) {
            this.type = type;
            this.batchIndex = batchIdx;
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(final Parameter o) {
            int comp = batchIndex - o.batchIndex;
            if (comp == 0) {
                return key - o.key;
            }
            return comp;
        }

        @Override
        public String toString() {
            return value + " (" + type + ")";
        }
    }
}
