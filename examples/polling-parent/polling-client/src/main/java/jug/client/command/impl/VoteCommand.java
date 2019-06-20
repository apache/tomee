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

import jug.client.command.api.Command;
import jug.domain.Value;

@Command(name = "vote", usage = "vote [<subject name>, +1|-1]", description = "vote for a subject")
public class VoteCommand extends QueryAndPostCommand {

    @Override
    protected String getName() {
        return "subject";
    }

    @Override
    protected String getPath() {
        return "api/subject/vote";
    }

    @Override
    protected String prePost(final String post) {
        if ("+1".equals(post) || "like".equals(post)) {
            return Value.I_LIKE.name();
        }
        if ("-1".equals(post)) {
            return Value.I_DONT_LIKE.name();
        }
        throw new IllegalArgumentException("please use +1 or -1 and not '" + post + "'");
    }
}
