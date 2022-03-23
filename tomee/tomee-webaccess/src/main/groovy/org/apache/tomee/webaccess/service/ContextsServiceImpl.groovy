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

import org.apache.tomee.webaccess.data.dto.ContextResultDto
import org.apache.tomee.webaccess.data.dto.SessionResultDto

import jakarta.annotation.security.RolesAllowed
import jakarta.ejb.Stateless
import jakarta.ejb.TransactionAttribute
import jakarta.ejb.TransactionAttributeType
import javax.management.InstanceNotFoundException
import javax.management.ObjectName
import java.lang.management.ManagementFactory

@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Stateless(name = 'TomEEWebAccessContextsService')
@RolesAllowed('tomee-admin')
class ContextsServiceImpl {

    void expireSession(String context, String sessionId) {
        def server = ManagementFactory.platformMBeanServer
        def name = "Catalina:type=Manager,context=/$context,host=localhost" as String
        try {
            def contextBean = server.getObjectInstance(new ObjectName(name))
            server.invoke(contextBean.objectName, 'expireSession', [sessionId] as Object[], ['java.lang.String'] as String[])
        } catch (InstanceNotFoundException ignore) {
            // no-op
        }
    }

    List<SessionResultDto> listSessions() {
        def server = ManagementFactory.platformMBeanServer
        def localhostBean
        def prefix = 'Catalina'
        try {
            localhostBean = server.getObjectInstance(new ObjectName("${prefix}:type=Host,host=localhost"))
        } catch (InstanceNotFoundException ignore) {
            prefix = 'Tomcat'
            localhostBean = server.getObjectInstance(new ObjectName("${prefix}:type=Host,host=localhost"))
        }
        def children = server.getAttribute(localhostBean.objectName, 'children')
        def baseNames = children.collect { ObjectName objectName ->
            server.getAttribute(objectName, 'baseName')
        }
        def result = []
        baseNames.each { baseName ->
            def name = "${prefix}:type=Manager,context=/$baseName,host=localhost" as String
            try {
                def contextBean = server.getObjectInstance(new ObjectName(name))
                def maxInactiveInterval = server.getAttribute(contextBean.objectName, 'sessionMaxAliveTime') as Integer
                def getValue = { String operationName, String sessionId ->
                    try {
                        server.invoke(contextBean.objectName, operationName, [sessionId] as Object[], ['java.lang.String'] as String[])
                    } catch (IllegalStateException ignore) {
                        // Session invalidated. Just ignore it.
                    }
                }
                String listSessionIds = server.invoke(contextBean.objectName, 'listSessionIds', null, null)
                listSessionIds.split(' ').each { String sessionId ->
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

    List<ContextResultDto> listContexts() {
        def server = ManagementFactory.platformMBeanServer
        server.queryNames(null, null)*.toString().findAll({
            it.startsWith('Catalina:j2eeType=WebModule') || it.startsWith('Tomcat:j2eeType=WebModule')
        }).findAll({
            def name = new ObjectName(it)
            server.getAttribute(name, 'stateName') != 'STOPPED'
        }).collect({
            def name = new ObjectName(it)
            def docBase = server.getAttribute(name, 'docBase') ?: ''
            def originalDocBase = server.getAttribute(name, 'originalDocBase') ?: ''
            new ContextResultDto(
                    baseName: server.getAttribute(name, 'baseName'),
                    deletable: docBase != '',
                    docBase: docBase == '' ? null : docBase,
                    originalDocBase: originalDocBase == '' ? null : originalDocBase
            )
        })
    }

    void killContext(String baseName) {
        def server = ManagementFactory.platformMBeanServer
        def name = server.queryNames(null, null).find({
            def str = it.toString()
            str.startsWith("Catalina:j2eeType=WebModule,name=//localhost/${baseName}") ||
                    str.startsWith("Tomcat:j2eeType=WebModule,name=//localhost/${baseName}")
        }) as ObjectName
        if (name) {
            def originalDocBase = server.getAttribute(name, 'originalDocBase') ?: ''
            if (originalDocBase) {
                // stop the application first
                server.invoke(name, 'stop', null, null)
                def docBase = new File(originalDocBase as String)
                if (docBase.isDirectory()) {
                    // then remove the directory
                    docBase.deleteDir()
                } else {
                    // just delete the war file and let tomcat do its thing
                    docBase.delete()
                }
            }
        }
    }

}
