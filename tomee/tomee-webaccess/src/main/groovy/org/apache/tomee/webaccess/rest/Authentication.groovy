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

import org.apache.tomee.webaccess.data.dto.AuthenticationResultDto

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.ws.rs.FormParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context

@Path('/authentication')
class Authentication {

    @POST
    @Produces('application/json')
    AuthenticationResultDto postUser(
            @FormParam('user') String user,
            @FormParam('password') String password, @Context HttpServletRequest request) {
        def dto = new AuthenticationResultDto(
                sessionId: request.session.id,
                success: true
        )
        try {
            request.login(user, password)
            request.session.setAttribute('authenticated', Boolean.TRUE)
        } catch (ServletException ignore) {
            dto.success = false
            dto.info = 'bad.username.or.password'
            request.session.setAttribute('authenticated', Boolean.FALSE)
        }
        dto
    }

}
