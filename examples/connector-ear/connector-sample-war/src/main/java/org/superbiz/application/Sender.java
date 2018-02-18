/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.application;

import org.superbiz.connector.api.SampleConnection;
import org.superbiz.connector.api.SampleConnectionFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Singleton
@Lock(LockType.READ)
@Path("")
public class Sender {

    @Resource
    private SampleConnectionFactory cf;

    @EJB
    private MessagesReceived messagesReceived;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void sendMessage(final String message) {
        try {
            final SampleConnection connection = cf.getConnection();
            connection.sendMessage(message);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessages() {

        final StringBuilder sb = new StringBuilder();

        final List<String> messages = this.messagesReceived.getMessagesReceived();
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }

            sb.append(messages.get(i));
        }

        return sb.toString();
    }

}
