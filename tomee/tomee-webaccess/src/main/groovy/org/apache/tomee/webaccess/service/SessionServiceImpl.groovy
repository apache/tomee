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

package org.apache.tomee.webaccess.service

import org.apache.tomee.webaccess.data.dto.SessionResultDto

import javax.annotation.security.RolesAllowed
import javax.ejb.Stateless
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.management.InstanceNotFoundException
import javax.management.ObjectName
import javax.ws.rs.PathParam
import java.lang.management.ManagementFactory

@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Stateless
@RolesAllowed('tomee-admin')
class SessionServiceImpl {

    static void expireSession(@PathParam("context") String context, @PathParam("sessionId") String sessionId) {
        def server = ManagementFactory.getPlatformMBeanServer()
        def name = "Catalina:type=Manager,context=/$context,host=localhost" as String
        try {
            def contextBean = server.getObjectInstance(new ObjectName(name))
            server.invoke(contextBean.objectName, 'expireSession', [sessionId] as Object[], [String.class.name] as String[])
        } catch (InstanceNotFoundException ignore) {
            // no-op
        }
    }

    static List<SessionResultDto> listSessions() {
        def server = ManagementFactory.getPlatformMBeanServer()
        def localhostBean = server.getObjectInstance(new ObjectName('Catalina:type=Host,host=localhost'))
        def children = server.getAttribute(localhostBean.objectName, 'children')
        def baseNames = children.collect { ObjectName objectName ->
            server.getAttribute(objectName, 'baseName')
        }
        def result = []
        baseNames.each { baseName ->
            def name = "Catalina:type=Manager,context=/$baseName,host=localhost" as String
            try {
                def contextBean = server.getObjectInstance(new ObjectName(name))
                def maxInactiveInterval = server.getAttribute(contextBean.objectName, 'maxInactiveInterval') as Long

                def getValue = { String operationName, String sessionId ->
                    server.invoke(contextBean.objectName, operationName, [sessionId] as Object[], [String.class.name] as String[])
                }

                server.invoke(contextBean.objectName, 'listSessionIds', null, null).split(' ').each { String sessionId ->
                    if (sessionId != '') {
                        def accessedTs = getValue('getLastAccessedTimestamp', sessionId) as Long
                        def expirationTs = maxInactiveInterval * 1000 + accessedTs
                        def creationTs = getValue('getCreationTimestamp', sessionId) as Long
                        result << new SessionResultDto(
                                context: baseName,
                                sessionId: sessionId,
                                creationTs: creationTs,
                                expirationTs: expirationTs,
                                lastAccessTs: accessedTs
                        )
                    }
                }
            } catch (InstanceNotFoundException ignore) {
                // no-op
            }
        }
        result
    }

}
