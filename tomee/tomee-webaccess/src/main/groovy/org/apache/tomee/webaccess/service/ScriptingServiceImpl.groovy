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

import org.apache.tomee.webaccess.data.dto.ScriptingResultDto

import jakarta.annotation.security.RolesAllowed
import jakarta.ejb.Stateless
import jakarta.ejb.TransactionAttribute
import jakarta.ejb.TransactionAttributeType
import javax.naming.Context
import javax.naming.InitialContext
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext

@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Stateless(name = 'TomEEWebAccessScriptingService')
@RolesAllowed('tomee-admin')
class ScriptingServiceImpl {

    @SuppressWarnings('CatchThrowable')
    ScriptingResultDto execute(String engineName, String script, String user, String pass, String realm) {
        def result = new ScriptingResultDto(output: '')
        if (script && '' != script.trim()) {
            def factory = new ScriptEngineManager()
            def sw = new StringWriter()
            def pw = new PrintWriter(sw)
            def engine = factory.getEngineByName(engineName?.trim()?.toLowerCase() ?: 'js')
            def scriptContext = new SimpleScriptContext()
            scriptContext.writer = pw
            scriptContext.errorWriter = pw

            // Creating a local context
            def props = new Properties()
            props[Context.INITIAL_CONTEXT_FACTORY] = 'org.apache.openejb.client.LocalInitialContextFactory'
            if (realm?.trim()) {
                props['openejb.authentication.realmName'] = realm.trim()
            }
            if (user) {
                props[Context.SECURITY_PRINCIPAL] = user
                props[Context.SECURITY_CREDENTIALS] = pass?.trim() ?: ''
            }
            try {
                def ctx = new InitialContext(props)
                try {
                    scriptContext.setAttribute('ctx', ctx, ScriptContext.ENGINE_SCOPE)
                    engine.eval(script, scriptContext)
                } finally {
                    // closing newly created context
                    ctx.close()
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace(pw)
            }
            result.output = sw.toString()
        }
        result
    }

}
