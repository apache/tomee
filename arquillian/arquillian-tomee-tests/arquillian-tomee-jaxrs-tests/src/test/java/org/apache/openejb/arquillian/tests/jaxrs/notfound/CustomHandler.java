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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.notfound;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.interceptor.JAXRSOutInterceptor;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import javax.net.ssl.HttpsURLConnection;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class CustomHandler extends AbstractPhaseInterceptor<Message> {
    public CustomHandler() {
        super(Phase.MARSHAL);
        addBefore(JAXRSOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(final Message message) throws Fault {
        if (isResponseAlreadyHandled(message)) {
            return;
        }
        final MessageContentsList objs = MessageContentsList.getContentsList(message);
        if (objs == null || objs.isEmpty()) {
            return;
        }

        final Object responseObj = objs.get(0);
        if (Response.class.isInstance(responseObj)) {
            final Response response = Response.class.cast(responseObj);
            if (is404(message, response)) {
                switchResponse(message);
            }
        } else {
            final Object exchangeStatus = message.getExchange().get(Message.RESPONSE_CODE);
            final int status = exchangeStatus != null ? Integer.class.cast(exchangeStatus) : HttpsURLConnection.HTTP_OK;
            if (status == HttpsURLConnection.HTTP_NOT_FOUND) {
                switchResponse(message);
            }
        }
    }

    private void switchResponse(final Message message) {
        message.setContent(List.class, new MessageContentsList(Response.ok("failed").build()));
    }

    private boolean is404(final Message message, final Response response) {
        return response.getStatus() == HttpsURLConnection.HTTP_NOT_FOUND
                && message.getExchange().get(JAXRSUtils.EXCEPTION_FROM_MAPPER) == null;
    }

    private boolean isResponseAlreadyHandled(final Message m) {
        return Boolean.TRUE.equals(m.getExchange().get(AbstractHTTPDestination.RESPONSE_COMMITED)) ||
                Boolean.TRUE.equals(m.getExchange().get(AbstractHTTPDestination.REQUEST_REDIRECTED));
    }
}
