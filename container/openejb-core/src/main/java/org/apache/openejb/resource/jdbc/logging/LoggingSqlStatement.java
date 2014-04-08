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
import java.sql.Statement;

public class LoggingSqlStatement extends AbstractSQLLogger implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SQL, LoggingSqlStatement.class);

    private final Statement delegate;

    public LoggingSqlStatement(final Statement result) {
        delegate = result;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String mtdName = method.getName();
        final boolean execute = mtdName.startsWith("execute") && args != null && args.length > 0;

        final TimeWatcherExecutor.TimerWatcherResult result = TimeWatcherExecutor.execute(method, delegate, args, execute);
        if (execute) {
            LOGGER.info(format((String) args[0], result.getDuration()));
        }

        return result.getResult();
    }
}
