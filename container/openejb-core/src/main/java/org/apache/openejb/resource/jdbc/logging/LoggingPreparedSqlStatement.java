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

import org.apache.openejb.core.ObjectInputStreamFiltered;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingPreparedSqlStatement implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SQL, LoggingPreparedSqlStatement.class);

    private final PreparedStatement delegate;
    private final String sql;
    private final List<Parameter> parameters = new ArrayList<>();
    private final String[] packages;
    private int parameterIndex;

    public LoggingPreparedSqlStatement(final PreparedStatement result, final String query, final String[] debugPackages) {
        delegate = result;
        sql = query;
        parameterIndex = 0;
        packages = debugPackages;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String mtdName = method.getName();
        final boolean execute = mtdName.startsWith("execute");

        final boolean debug = false;
        if (debug) {
            LOGGER.info(String.format("PreparedStatement.%s(%s)", method.getName(),
                (args == null) ? "" :
                    Join.join(", ", args)
            ));
            if (execute) {
                logDebug();
            }
        }

        final TimeWatcherExecutor.TimerWatcherResult result = TimeWatcherExecutor.execute(method, delegate, args, execute);

        if (mtdName.startsWith("set") && args.length >= 2 && (args[0].getClass().equals(Integer.TYPE) || args[0].getClass().equals(Integer.class))) {
            final Parameter param = new Parameter(mtdName.substring(3), parameterIndex, (Integer) args[0], args[1]);

            if (debug) {
                logParam(param);
            }

            parameters.add(param);
        } else if (execute) {
            StringBuilder str = new StringBuilder(sql);
            if (str.toString().contains("?")) {
                Collections.sort(parameters);
                int lastBatch = 0;
                for (int i = 0; i < parameters.size(); i++) {
                    final Parameter param = parameters.get(i);
                    if (str.toString().contains("?")) {
                        try {
                            String val;
                            if (ByteArrayInputStream.class.isInstance(param.value)) {
                                final ByteArrayInputStream bais = ByteArrayInputStream.class.cast(param.value);
                                try {
                                    bais.reset(); // already read when arriving here - mainly openjpa case
                                    val = new ObjectInputStreamFiltered(bais).readObject().toString();
                                } catch (final Exception e) {
                                    val = param.value.toString();
                                }
                            } else {
                                val = param.value.toString();
                            }
                            str = new StringBuilder(str.toString().replaceFirst("\\?", val));
                        } catch (final Exception e) {
                            if (param.value == null) {
                                str = new StringBuilder(str.toString().replaceFirst("\\?", "null"));
                            } else {
                                str = new StringBuilder(str.toString().replaceFirst("\\?", param.value.getClass().getName()));
                            }
                        }
                        lastBatch = param.batchIndex;
                    } else {
                        if (lastBatch != param.batchIndex) {
                            str.append(", (");
                            lastBatch = param.batchIndex;
                        }

                        try {
                            str.append(param.value.toString());
                        } catch (final Exception e) {
                            if (param.value == null) {
                                str.append("null");
                            } else {
                                str.append(param.value.getClass().getName());
                            }
                        }

                        if (i == parameters.size() - 1 || parameters.get(i + 1).batchIndex != lastBatch) {
                            str.append(")");
                        } else {
                            str.append(",");
                        }
                    }
                }
            }
            LOGGER.info(result.format(str.toString()) + (packages != null ? " - stack:" + TimeWatcherExecutor.inlineStack(packages) : ""));
        } else if ("clearParameters".equals(mtdName)) {
            parameters.clear();
            parameterIndex = 0;
        } else if ("addBatch".equals(mtdName)) {
            parameterIndex++;
        }

        if (result.getThrowable() != null) {
            throw result.getThrowable();
        }
        return result.getResult();
    }

    private void logDebug() {
        try {
            LOGGER.info("SQL " + sql);
            for (final Parameter parameter : parameters) {
                logParam(parameter);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    private void logParam(final Parameter parameter) throws SQLException {
        logParam(this.delegate.getParameterMetaData(), parameter);
    }

    private void logParam(final ParameterMetaData md, final Parameter parameter) throws SQLException {
        final int i = parameter.key;
        final String format = String.format(" - PARAM  index=%s, type%s, typeName=%s, className=%s, nullable=%s, mode=%s, precision=%s, value=%s",
            i,
            md.getParameterType(i),
            md.getParameterTypeName(i),
            md.getParameterClassName(i),
            md.isNullable(i),
            md.getParameterMode(i),
            md.getPrecision(i),
            parameter.value
        );

        LOGGER.info(format);
    }

    protected static class Parameter implements Comparable<Parameter> {
        private final String type;
        private final int batchIndex;
        private final int key;
        private final Object value;

        public Parameter(final String type, final int batchIdx, final int key, final Object value) {
            this.type = type;
            this.batchIndex = batchIdx;
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(final Parameter o) {
            final int comp = batchIndex - o.batchIndex;
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
