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

package org.apache.tomee.deb.jira

import groovy.json.JsonSlurper
import groovy.text.GStringTemplateEngine
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

class ChangeLogBuilder {
    static final JIRA_SRV = 'https://issues.apache.org/jira/rest/api/2/search?jql=' +
            'project+in+(${projectName})+AND+' +
            'type+in+(Bug,+Improvement)+AND+' +
            'resolutiondate+is+not+EMPTY+AND+' +
            'fixVersion+is+not+EMPTY+' +
            'ORDER+BY+fixVersion+ASC&' +
            'startAt=${startAt}&maxResults=${maxResults}&' +
            'fields=issuetype,priority,fixVersions,summary,resolutiondate,project'

    static final JIRA_DT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    static final JIRA_RELEASE_DT_FORMAT = new SimpleDateFormat('yyyy-MM-dd')
    static final LOG_RELEASE_DT_FORMAT = new SimpleDateFormat('EEE, d MMM yyyy HH:mm:ss Z')


    Set<VersionIssues> getLogs(String projectName) {
        Map<String, VersionIssues> versionsMap = [:]
        def slupper = new JsonSlurper()
        def total = 1
        def startAt = 0
        def getIssues = {
            def url = new GStringTemplateEngine().createTemplate(JIRA_SRV).make([
                    projectName: projectName,
                    startAt    : startAt,
                    maxResults : 50
            ]).toString()
            def json = slupper.parseText(new URL(url).text)
            total = json.total
            startAt += json.maxResults
            json.issues
        }
        while (total > startAt) {
            getIssues().each { issue ->
                def bean = new IssueLog(
                        key: issue.key,
                        summary: issue.fields.summary,
                        issueType: issue.fields.issuetype.name,
                        priority: issue.fields.priority.name,
                        resolutionDate: JIRA_DT_FORMAT.parse(issue.fields.resolutiondate as String)
                )
                issue.fields.fixVersions.each { fixVersion ->
                    String versionName = fixVersion.name as String
                    if (!versionName.matches('[[0-9]+\\.]+[0-9]') || !fixVersion.releaseDate) {
                        return // not released yet
                    }
                    Date releaseDate = JIRA_RELEASE_DT_FORMAT.parse(fixVersion.releaseDate as String)
                    if (!versionsMap.containsKey(versionName)) {
                        versionsMap.put(versionName, new VersionIssues(
                                project: issue.fields.project.name,
                                version: versionName,
                                releaseDate: releaseDate
                        ))
                    }
                    versionsMap.get(versionName).issues << bean
                }
            }
        }
        new TreeSet<VersionIssues>(versionsMap.values())
    }

    String buildChangelogContent(String projectName) {
        Set<VersionIssues> logs = getLogs(projectName)
        def tplEngine = new GStringTemplateEngine()
        def logTemplate = tplEngine.createTemplate(this.class.getResource('/changelog.template'))
        def logEntryTemplate = tplEngine.createTemplate(this.class.getResource('/changelog_entry.template'))
        def resultWriter = new StringWriter()
        def resultBufWriter = new BufferedWriter(resultWriter)
        logs.each { VersionIssues versionIssues ->
            def versionUrgency = 'low'
            def entryWriter = new StringWriter()
            def entryBufWriter = new BufferedWriter(entryWriter)
            versionIssues.issues.each { IssueLog issueLog ->
                entryBufWriter.writeLine(logEntryTemplate.make([
                        issueTitle: StringUtils.abbreviate(issueLog.summary, 76),
                        issueID   : issueLog.key
                ]).toString())
                switch (issueLog.priority) {
                    case 'Blocker':
                        versionUrgency = 'critical'
                        break
                    case 'Critical':
                        if (versionUrgency != 'critical') {
                            versionUrgency = 'emergency'
                        }
                        break
                    case 'Major':
                        if (versionUrgency != 'critical' && versionUrgency != 'emergency') {
                            versionUrgency = 'high'
                        }
                        break
                    case 'Minor':
                        if (versionUrgency != 'critical' && versionUrgency != 'emergency' && versionUrgency != 'high') {
                            versionUrgency = 'medium'
                        }
                        break
                    default: //Trivial
                        // no-op
                        break
                }
            }
            entryBufWriter.close()
            entryWriter.close()

            resultBufWriter.writeLine(logTemplate.make([
                    projectName: versionIssues.project,
                    version    : versionIssues.version,
                    urgency    : versionUrgency,
                    fixDate    : LOG_RELEASE_DT_FORMAT.format(versionIssues.releaseDate),
                    issues     : entryWriter.toString()
            ]).toString())
        }
        resultBufWriter.close()
        resultWriter.close()
        resultWriter.toString().trim()
    }

}
