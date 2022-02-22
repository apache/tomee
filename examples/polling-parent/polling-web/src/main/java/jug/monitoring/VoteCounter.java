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
package jug.monitoring;

import jug.domain.Subject;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@MBean
@ApplicationScoped
@Description("count the number of vote by subject")
public class VoteCounter {

    private final Map<String, Subject> subjects = new ConcurrentHashMap<String, Subject>();

    @ManagedAttribute
    @Description("number of poll created/updated in this instance")
    public int getSubjectNumber() {
        return subjects.size();
    }

    @ManagedOperation
    @Description("current score of the specified poll")
    public String names() {
        return subjects.keySet().toString();
    }

    @ManagedOperation
    @Description("current score of the specified poll")
    public String score(final String name) {
        if (subjects.containsKey(name)) {
            return Integer.toString(subjects.get(name).score());
        }
        return "poll not found";
    }

    public void putSubject(final Subject subject) {
        subjects.put(subject.getName(), subject);
    }
}
