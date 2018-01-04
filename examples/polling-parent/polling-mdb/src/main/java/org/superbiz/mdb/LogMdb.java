/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.mdb;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.atomic.AtomicLong;

@MessageDriven(activationConfig = {
        @javax.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @javax.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "LogMDB")
})
public class LogMdb implements MessageListener {
    private static final AtomicLong ID_MANAGER = new AtomicLong(0);
    private long id = ID_MANAGER.incrementAndGet();
    private int usageCount = 0;

    @EJB
    private LogsBean logs;

    public void onMessage(Message message) {
        usageCount++;
        try {
            logs.add("BEAN_" + this.id + " [" + usageCount + "] -> " + message.getStringProperty("txt"));
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}

