/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package commands

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.io.FileUtils
import org.apache.tomee.debian.MapBuilder
import org.apache.tomee.debian.PackageBuilder

class BuildDeb {
    def project
    def log

    def BuildDeb(def source) {
        this.project = source.project
        this.log = source.log
    }

    String get(name) {
        assert name != null

        def value = project.properties.getProperty(name)

        log.debug("Get property: $name=$value")

        return value
    }

    String require(name) {
        assert name != null

        log.debug("Require property: $name")

        if (!project.properties.containsKey(name) && !System.properties.containsKey(name)) {
            throw new Exception("Missing required property: $name")
        }

        def value = get(name)

        if (value == 'null') {
            throw new Exception("Missing required property: $name (resolved to null)")
        }

        return value
    }

    String getResource(String name) {
        def result = this.getClass().getClassLoader().getResourceAsStream(name)
        return result.getText()
    }

	def execute() {
		String file = require('file')
        String name = require('name')
        String version = require('version')

        final PackageBuilder builder = new PackageBuilder("tomee", "tomee")

        Map<String, String> dirMapping = new MapBuilder<String, String>(new HashMap<String, String>())
                .add("/bin", "/usr/tomee")
                .add("/conf", "/etc/tomee")
                .add("/endorsed", "/usr/tomee")
                .add("/lib", "/usr/tomee")
                .add("/LICENSE", "/usr/share/doc/tomee")
                .add("/NOTICE", "/usr/share/doc/tomee")
                .add("/RELEASE-NOTES", "/usr/share/doc/tomee")
                .add("/RUNNING.txt", "/usr/share/doc/tomee")
                .add("/work", "/var/tomee")
                .add("/logs", "/var/tomee")
                .add("/webapps", "/var/tomee")
                .add("/temp", "/tmp/tomee")
                .getMap()

        Map<String, Integer> modeMapping = new MapBuilder<String, Integer>(new HashMap<String, Integer>())
                .add("/usr/tomee/bin/startup.sh", 700)
                .getMap()

        final File original = new File(file)
        final File gz = new File(new File(require('project.folder')), 'build/' + original.getName())
        FileUtils.copyFile(original, gz)

        String control = StringUtils.replace(getResource("control"), "{0}", version)
        control = StringUtils.replace(control, "{1}", name)

        builder.createDebPackage(name, version, gz,
                control,
                getResource("postinst"),
                getResource("prerm"),
                dirMapping,
                modeMapping
        )
        gz.delete()
    }
}

