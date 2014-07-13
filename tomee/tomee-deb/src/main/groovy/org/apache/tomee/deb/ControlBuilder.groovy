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
 
package org.apache.tomee.deb

import groovy.text.GStringTemplateEngine
import org.apache.commons.codec.digest.DigestUtils
import org.apache.maven.project.MavenProject

import java.text.SimpleDateFormat

class ControlBuilder {

    private String classifier
    private File workDir
    private File controlDir

    String version
    Double installedSize
    int priority

    def tplEngine = new GStringTemplateEngine()

    private static Date now = new Date()

    ControlBuilder(MavenProject project, String classifier) {
        this.classifier = classifier
        this.workDir = new File(project.properties['distribution.workdir'] as String)

        def dft = new SimpleDateFormat('yyyy-MM-dd-HH-mm-ss')
        dft.timeZone = TimeZone.getTimeZone('GMT-0')
        def executionDate = dft.format(now)
        this.version = project.version
        if (this.version.toLowerCase().endsWith('-snapshot')) {
            this.version += "-${executionDate}"
        }
    }

    private static String getCheckSum(File file) {
        return getCheckSum(new FileInputStream(file))
    }

    private static String getCheckSum(InputStream input) {
        String md5 = DigestUtils.md5Hex(input)
        input.close()
        return md5
    }

    private static String getCheckSumLine(File root, File file) {
        String md5 = getCheckSum(file)
        return "${md5} ${file.absolutePath.substring(root.absolutePath.length() + 1)}"
    }

    ControlBuilder buildControlDir() {
        def exploded = new File(workDir, "exploded-${classifier}")
        controlDir = new File(exploded, 'control')
        controlDir.mkdirs()
        File dataDir = new File(exploded, 'data')
        new File(controlDir, 'md5sums').withWriter { BufferedWriter out ->
            dataDir.eachFileRecurse {
                if (it.directory) {
                    return
                }
                out.writeLine(getCheckSumLine(dataDir, it))
            }
        }
        this.installedSize = dataDir.directorySize() / 1024
        switch (classifier) {
            case 'plus':
                this.priority = 4
                break
            case 'plume':
                this.priority = 3
                break
            default:
                this.priority = 1
        }
        this
    }

    ControlBuilder withWriter(String targetFilePath, def closure) {
        def toFile = new File(controlDir, targetFilePath)
        toFile.parentFile.mkdirs()
        toFile.withWriter closure as Closure
        this
    }

    ControlBuilder withBaseDir(def closure) {
        closure new File(workDir, "exploded-${classifier}")
        this
    }

    ControlBuilder writeTemplate(String toPath, String templatePath, Map<String, String> params) {
        def templateResource = this.class.getResource(templatePath).text
        withWriter(toPath, { BufferedWriter out ->
            def template = tplEngine.createTemplate(templateResource).make(params)
            out.write(template.toString())
        })
        this
    }

    ControlBuilder withMe(def closure) {
        closure(this)
        this
    }
}
