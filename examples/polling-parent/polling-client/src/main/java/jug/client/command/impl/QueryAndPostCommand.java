/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import jakarta.ws.rs.core.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class QueryAndPostCommand extends AbstractCommand {

    private static final Pattern PATTERN = Pattern.compile(" \\[(.*),(.*)\\]");

    @Override
    protected Response invoke(final String cmd) {
        final Matcher matcher = PATTERN.matcher(cmd.substring(getClass().getAnnotation(Command.class).name().length()));
        if (!matcher.matches() || matcher.groupCount() != 2) {
            System.err.println("'" + cmd + "' doesn't match command usage");
            return null;
        }

        return client.path(getPath()).query(getName(), matcher.group(1).trim()).post(prePost(matcher.group(2).trim()));
    }

    protected String prePost(final String post) {
        return post;
    }

    protected abstract String getName();

    protected abstract String getPath();
}
