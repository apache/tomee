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

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/log")
public class ApiLog {

    @Resource
    private ConnectionFactory connectionFactory;

    @Resource(name = "LoggingBean")
    private Queue vector;

    @EJB
    private CounterBean logs;

    @GET
    @Path("/{txt}")
    public Response get(@PathParam("txt") String txt) throws JMSException {
        try (final Connection connection = connectionFactory.createConnection()) {
            connection.start();
            try (final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                try (final MessageProducer producer = session.createProducer(vector)) {
                    final Message msg = session.createMessage();
                    msg.setStringProperty("txt", txt);
                    producer.send(msg);
                }
            }
        }
        return Response.ok().build();
    }

}
