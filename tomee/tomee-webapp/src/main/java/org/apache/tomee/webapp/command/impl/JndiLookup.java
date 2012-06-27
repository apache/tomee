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

package org.apache.tomee.webapp.command.impl;

import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.Params;
import org.apache.tomee.webapp.listener.UserSessionListener;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JndiLookup implements Command {

    private Context getContext(Context ctx, String path) throws NamingException {
        if (path == null || "".equals(path.trim())) {
            return ctx;
        }

        final Object obj = ctx.lookup(path);

        if (obj instanceof Context) {
            return (Context) obj;

        } else {
            throw new IllegalStateException("obj should be an instance of Context");
        }
    }

    @Override
    public Object execute(Params params) throws Exception {
        final Context initCtx = UserSessionListener.getServiceContext(params.getReq().getSession()).getUserContext();

        final Context ctx = getContext(initCtx, params.getString("path"));
        final String name = params.getString("name");
        final Object obj = ctx.lookup(name);

        UserSessionListener.getServiceContext(params.getReq().getSession()).getSaved().put(params.getString("saveKey"), obj);

        return null;
    }
}
