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
package org.apache.openejb.resource.activemq.jms2;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.XAJMSContext;
import jakarta.jms.XASession;
import javax.transaction.xa.XAResource;

public class XAJMSContextImpl extends JMSContextImpl implements XAJMSContext {
    public XAJMSContextImpl(final ConnectionFactory factory, final int sessionMode, final String user, final String pwd) {
        super(factory, sessionMode, user, pwd, true);
    }

    @Override
    public JMSContext getContext() {
        return this;
    }

    @Override
    public XAResource getXAResource() {
        return XASession.class.cast(session()).getXAResource();
    }
}
