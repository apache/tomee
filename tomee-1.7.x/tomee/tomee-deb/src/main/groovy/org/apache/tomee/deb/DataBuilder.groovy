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

import groovy.io.FileType
import groovy.text.GStringTemplateEngine
import org.apache.maven.project.MavenProject
import org.apache.tomee.deb.jira.ChangeLogBuilder

import java.util.jar.JarEntry
import java.util.jar.JarFile

class DataBuilder {

    def ant = new AntBuilder()
    def tplEngine = new GStringTemplateEngine()
    private String classifier
    private File workDir
    private File outputDir
    private File dataDir
    private String zipFile
    Map<String, String> symbolicLinks = [:]
    private CompressTool compressTool
    private File baseDir

    DataBuilder(MavenProject project, String classifier, String zipFile, CompressTool compressTool) {
        this.baseDir = project.basedir
        this.classifier = classifier
        this.workDir = new File(project.properties['distribution.workdir'] as String)
        this.zipFile = zipFile
        this.compressTool = compressTool
    }

    DataBuilder buildDataDir() {
        outputDir = unzipOriginalPackage()
        this
    }

    DataBuilder writeTemplate(String toPath, String templatePath, Map<String, String> params) {
        def templateResource = this.class.getResource(templatePath).text
        def toFile = new File(dataDir, toPath)
        toFile.parentFile.mkdirs()
        toFile.withWriter { BufferedWriter out ->
            def template = tplEngine.createTemplate(templateResource).make(params)
            out.write(template.toString())
        }
        this
    }

    DataBuilder copyContent(String toPath, String sourcePath) {
        def dest = new File(dataDir, toPath)
        dest.parentFile.mkdirs()
        dest.withWriter { BufferedWriter out ->
            def data = this.class.getResource(sourcePath).withInputStream { InputStream dataInput ->
                out << dataInput
            }
        }
        this
    }

    DataBuilder eachFileRecurse(String path, def closure) {
        File root = new File(dataDir, path)
        int pathStartIndex = dataDir.absolutePath.size()
        root.eachFileRecurse {
            closure(this, it.absolutePath.substring(pathStartIndex).replace('\\', '/'), it)
        }
        this
    }

    DataBuilder move(String fromPath, String toPath) {
        File from = new File(outputDir, fromPath)
        File to = new File(dataDir, toPath)
        to.parentFile.mkdirs()
        from.renameTo(to)
        this
    }

    private File unzipOriginalPackage() {
        def exploded = new File(workDir, "exploded-${classifier}")
        def zipFile = new File(workDir, this.zipFile)
        outputDir = new File(exploded, 'output')
        dataDir = new File(exploded, 'data')
        ant.unzip(
                src: zipFile,
                dest: exploded
        )
        exploded.listFiles()[0].renameTo(outputDir)
        outputDir.eachFileRecurse(FileType.FILES) { File file ->
            def fileName = file.name.toLowerCase()
            if (fileName.endsWith('.txt')
                    || fileName.endsWith('.exe')
                    || fileName.endsWith('.bat')
                    || fileName.endsWith('.original')
                    || fileName.endsWith('.tmp')
                    || fileName == 'license') {
                file.delete()
            }
        }
        outputDir
    }

    DataBuilder addSymbolicLink(String path, String reference) {
        symbolicLinks.put(path, reference)
    }

    private boolean hasClasses(File jarFile) {
        def entriesEnumerator = new JarFile(jarFile).entries()
        while (entriesEnumerator.hasMoreElements()) {
            def jarEntry = entriesEnumerator.nextElement() as JarEntry
            if (jarEntry.name.endsWith('.class')) {
                return true
            }
        }
        false
    }

    private void maskCodelessJars() {
        def unjarDir = new File(workDir, 'unjar')
        unjarDir.mkdirs()
        dataDir.eachFileRecurse(FileType.FILES) { File jarFile ->
            if (jarFile.name.endsWith('.jar')) {
                if (!hasClasses(jarFile)) {
                    def explodedJar = new File(unjarDir, "${classifier}/${jarFile.name}")
                    ant.unjar(src: jarFile, dest: explodedJar)
                    jarFile.delete()
                    ant.zip(destfile: jarFile.absolutePath) {
                        fileset(dir: explodedJar.absolutePath) {
                            exclude(name: '**/META-INF/MANIFEST.MF')
                        }
                    }
                }
            }
        }
        unjarDir.deleteDir()
    }

    static def buildChangelogContent = { File baseDir ->
        def preCompiledChangelog = new File(baseDir, 'src/main/resources/precompiled-changelog')
        if(preCompiledChangelog.exists()) {
            return preCompiledChangelog.text
        }
        def builder = new ChangeLogBuilder()
        def content = builder.buildChangelogContent('TOMEE,OPENEJB')
        preCompiledChangelog.withWriter {BufferedWriter out ->
            out.write(content)
        }
        content
    }.memoize() // execute it just once

    DataBuilder buildChangelog() {
        String content = buildChangelogContent(baseDir)
        if (!content) {
            return this
        }
        def docDir = new File(dataDir, "usr/share/doc/${classifier}/")
        docDir.mkdirs()
        def changelogFile = new File(docDir, 'changelog.Debian')
        changelogFile.withWriter { BufferedWriter writer ->
            writer.writeLine(content)
        }
        def changelogFileGz = new File(changelogFile.parent, 'changelog.Debian.gz')
        compressTool.gz(changelogFile.absolutePath, changelogFileGz.absolutePath)
        changelogFile.delete()
        this
    }

    private void deleteEmptyDirs(File rootDir) {
        List<File> deleteMe = []
        rootDir.eachFileRecurse(FileType.DIRECTORIES) { File dir ->
            if (!dir.list()) {
                deleteMe << dir
            }
        }
        deleteMe.each {
            it.delete()
        }
    }

    DataBuilder clean() {
        outputDir.deleteDir()
        deleteEmptyDirs(dataDir)
        maskCodelessJars()
        this
    }
}
