/**
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
package org.apache.openejb.arquillian.tests.jms;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

@Dependent
public class DummyManagedBean {
    @Resource
    private Queue queue;

    @Resource
    private Topic topic;

    @Resource
    private ConnectionFactory connectionFactory;

    public String getFoo() {
        if (queue != null) {
            return "queueInjected";
        }
        return "queueNotInjected";
    }

    public String getBoo() {
        if (topic != null) {
            return "topicInjected";
        }
        return "topicNotInjected";
    }

    public String getBaz() {
        if (connectionFactory != null) {
            return "connectionFactoryInjected";
        }
        return "connectionFactoryNotInjected";
    }
}
