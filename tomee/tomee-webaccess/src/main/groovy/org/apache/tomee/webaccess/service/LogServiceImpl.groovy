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

import org.apache.tomee.webaccess.data.dto.ListFilesResultDto
import org.apache.tomee.webaccess.data.dto.LogFileResultDto

import jakarta.annotation.security.RolesAllowed
import jakarta.ejb.Stateless
import jakarta.ejb.TransactionAttribute
import jakarta.ejb.TransactionAttributeType

@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Stateless(name = 'TomEEWebAccessLogService')
@RolesAllowed('tomee-admin')
class LogServiceImpl {
    private static final LINE_LIMIT = 2000

    ListFilesResultDto listFiles() {
        def logFolder = new File(System.getProperty('catalina.base'), 'logs')
        def files = logFolder.listFiles()
        def names = new TreeSet<String>()
        names.addAll(files.findAll({ it.length() > 0 })*.name)
        new ListFilesResultDto(
                files: names
        )
    }

    LogFileResultDto load(String fileName) {
        def logFolder = new File(System.getProperty('catalina.base'), 'logs')
        def file = new File(logFolder, fileName)
        def text
        try {
            def lines = new LinkedList()
            file.eachLine {
                if (lines.size() > LINE_LIMIT) {
                    lines.removeFirst()
                }
                lines << it
            }
            text = lines.join(System.getProperty('line.separator'))
        } catch (FileNotFoundException ignore) {
            text = "'$fileName' not found."
        }
        new LogFileResultDto(
                content: text
        )
    }

}
