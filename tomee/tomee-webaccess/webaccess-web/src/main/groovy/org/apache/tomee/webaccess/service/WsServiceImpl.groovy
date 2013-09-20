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

import org.apache.openejb.server.ServerService
import org.apache.openejb.server.ServiceManager
import org.apache.openejb.server.SimpleServiceManager
import org.apache.openejb.util.LogCategory
import org.apache.openejb.util.Logger
import org.apache.tomee.webaccess.data.dto.ApplicationDto
import org.apache.tomee.webaccess.data.dto.ServiceDto
import org.apache.tomee.webaccess.data.dto.WsListResultDto

import javax.annotation.security.RolesAllowed
import javax.ejb.Stateless

@Stateless
@RolesAllowed('tomee-admin')
class WsServiceImpl {
    private static def log = Logger.getInstance(LogCategory.OPENEJB_RS, WsServiceImpl)

    private static ApplicationDto getApplicationByName(WsListResultDto queryResult, String name) {
        def dto = queryResult.applications.find {
            it.name = name
        }
        if (!dto) {
            dto = new ApplicationDto(name: name)
            queryResult.applications << dto
        }
        dto
    }

    private static def getValue(def source, String attributeName) {
        try {
            return source[attributeName]
        } catch (ignore) {
            return null
        }
    }

    private static ServerService serverService(String clazz) {
        def sm = ServiceManager.get()
        if (!(sm instanceof SimpleServiceManager)) { // we don't know
            log.warning("the service manager used is not a simple service manager so rest services can't be retrieved")
            return null
        }
        def serverServices = (sm as SimpleServiceManager)?.daemons
        if (!serverServices) {
            log.warning("no service started")
            return null
        }
        serverServices.find { ServerService ss ->
            clazz == ss.class.name
        }
    }

    private static WsListResultDto restWebServices(WsListResultDto queryResult) {
        def ss = serverService("org.apache.openejb.server.cxf.rs.CxfRSService")
        if (!ss) {
            return queryResult
        }
        ss.services?.each { rsService ->
            def app = getValue(rsService, "webapp") as String
            def address = getValue(rsService, "address") as String
            def origin = getValue(rsService, "origin") as String
            getApplicationByName(queryResult, app).services << new ServiceDto(
                    name: origin,
                    address: address + "?wadl"
            )
        }
        queryResult
    }

    private static WsListResultDto soapWebServices(WsListResultDto queryResult) {
        def ss = serverService("org.apache.openejb.server.cxf.CxfService")
        if (!ss) {
            return queryResult
        }
        ss.addressesByApplication?.each { String appName, List addresses ->
            def app = getApplicationByName(queryResult, appName)
            app.services = addresses?.collect { soapService ->
                def wsdl = getValue(soapService, "address") as String
                def port = getValue(soapService, "portName") as String
                def classname = getValue(soapService, "classname") as String
                new ServiceDto(
                        name: classname,
                        address: wsdl + "?wsdl",
                        port: port
                )
            }
        }
        queryResult
    }

    static WsListResultDto list() {
        def queryResult = new WsListResultDto();
        restWebServices(queryResult)
        soapWebServices(queryResult)
        queryResult
    }
}
