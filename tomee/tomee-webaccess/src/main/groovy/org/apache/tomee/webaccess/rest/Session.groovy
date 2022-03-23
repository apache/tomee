
/**
 *
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

package org.apache.tomee.webaccess.rest

import org.apache.tomee.webaccess.data.dto.SessionResultDto
import org.apache.tomee.webaccess.service.ContextsServiceImpl

import jakarta.ejb.EJB
import jakarta.ws.rs.*

@Path('/session')
class Session {

    @EJB
    private ContextsServiceImpl service

    @DELETE
    @Path('/expire/{context}/{sessionId}')
    void expireSession(@PathParam('context') String context, @PathParam('sessionId') String sessionId) {
        service.expireSession(context, sessionId)
    }

    @GET
    @Produces('application/json')
    List<SessionResultDto> list() {
        service.listSessions()
    }
}
