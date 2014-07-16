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

class VersionIssues implements Comparable<VersionIssues> {
    String project
    String version
    Date releaseDate
    List<IssueLog> issues = []

    private String normalizeVersion(String version) {
        String versionNumber = (version =~ /[[0-9]+\.]+[0-9]/)[0]
        def lowerCase = version.toLowerCase().substring(versionNumber.length())
        if (lowerCase.contains('alpha')) {
            return "${versionNumber}-0-${lowerCase}"
        }
        if (lowerCase.contains('beta')) {
            return "${versionNumber}-1-${lowerCase}"
        }
        return "${versionNumber}-2-${lowerCase}"
    }

    @Override
    int compareTo(VersionIssues other) {
        return -1 * normalizeVersion(version).compareTo(normalizeVersion(other.version))
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (version != (o as VersionIssues).version) return false
        return true
    }

    int hashCode() {
        return version.hashCode()
    }
}
