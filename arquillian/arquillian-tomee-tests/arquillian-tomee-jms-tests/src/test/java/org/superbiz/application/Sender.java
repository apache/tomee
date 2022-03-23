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

import org.apache.openejb.loader.IO;
import org.superbiz.connector.api.SampleConnection;
import org.superbiz.connector.api.SampleConnectionFactory;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "Sender", urlPatterns = { "/sender" })
public class Sender extends HttpServlet{

    @Resource
    private SampleConnectionFactory cf;

    @EJB
    private MessagesReceived messagesReceived;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String payload = IO.slurp(req.getInputStream());
            final SampleConnection connection = cf.getConnection();
            connection.sendMessage(payload);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final StringBuilder sb = new StringBuilder();

        final List<String> messages = this.messagesReceived.getMessagesReceived();
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }

            sb.append(messages.get(i));
        }

        resp.getWriter().print(sb.toString());
    }
}
