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
package org.apache.tomee.arquillian.remote;

import org.apache.openejb.arquillian.common.Prefixes;
import org.apache.openejb.arquillian.common.TomEEConfiguration;

/**
 * @version $Rev$ $Date$
 */
@Prefixes({"tomee", "tomee.remote"})
public class RemoteTomEEConfiguration extends TomEEConfiguration {

    private String groupId = "org.apache.openejb";
    private String artifactId = "apache-tomee";
    private String version = "1.0.0-beta-3-SNAPSHOT";
    private String classifier = "webprofile";
    private String type = "zip";

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArtifactName() {

        final String format = classifier == null ? "%s:%s:%s:%s" : "%s:%s:%s:%s:%s";

        return String.format(format, getGroupId(), getArtifactId(), getVersion(), getType(), getClassifier());

    }
}
