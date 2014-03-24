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
import org.apache.commons.compress.archivers.ar.ArArchiveEntry
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream

class PackageBuilder {
    def ant = new AntBuilder()
    def properties

    String unzip(String tarPath) {
        def outputDir = new File(new File(tarPath).parent, 'exploded')
        outputDir.mkdirs()
        ant.unzip(
                src: tarPath,
                dest: outputDir.absolutePath
        )
        def files = outputDir.listFiles()
        if (files.size() > 1) {
            return outputDir.absolutePath
        }
        def aux = new File(new File(tarPath).parent, 'aux')
        files[0].renameTo(aux)
        outputDir.delete()
        aux.renameTo(outputDir)
        ant.delete(includeemptydirs: true) {
            fileset(dir: outputDir.absolutePath, includes: '**/*.exe')
            fileset(dir: outputDir.absolutePath, includes: '**/*.bat')
            fileset(dir: outputDir.absolutePath, includes: '**/*.original')
            fileset(dir: outputDir.absolutePath, includes: '**/*.tmp')
            fileset(dir: new File(outputDir, 'webapps').absolutePath) {
                include(name: '**/*')
                exclude(name: '**/tomee/**')
            }
        }
        outputDir.absolutePath
    }

    private String getCheckSum(File file) {
        return getCheckSum(new FileInputStream(file))
    }

    private String getCheckSum(InputStream input) {
        String md5 = DigestUtils.md5Hex(input)
        input.close()
        return md5
    }

    private String getCheckSumLine(File root, File file) {
        String md5 = getCheckSum(file)
        return "${md5} ${file.absolutePath.substring(root.absolutePath.length() + 1)}"
    }

    String createControlDir(String dataDirPath) {
        def dataDir = new File(dataDirPath)
        def controlDir = new File(dataDir.parent, 'control')
        controlDir.mkdirs()
        new File(controlDir, 'md5sums').withWriter { BufferedWriter out ->
            dataDir.eachFileRecurse {
                if (it.directory) {
                    return
                }
                out.writeLine(getCheckSumLine(dataDir, it))
            }
        }
        Double installedSize = dataDir.directorySize() / 1024
        new File(controlDir, 'control').withWriter { BufferedWriter out ->
            def template = new GStringTemplateEngine().createTemplate(
                    this.class.getResource('/control/control.template')
            ).make([
                    tomeeVersion: properties.tomeeVersion,
                    inMB        : installedSize.longValue()
            ])
            out.write(template.toString())
        }
        new File(controlDir, 'postinst').withWriter { BufferedWriter out ->
            out.write(this.class.getResource('/control/postinst.sh').text)
        }
        new File(controlDir, 'prerm').withWriter { BufferedWriter out ->
            out.write(this.class.getResource('/control/prerm.sh').text)
        }
        controlDir.absolutePath
    }

    String createDataDir(String explodedPath) {
        def exploded = new File(explodedPath)
        def outputDir = new File(exploded.parent, 'output')
        def dataDir = new File(outputDir, 'data')
        dataDir.mkdirs()
        def distributionTomeeDir = new File(dataDir, 'opt/tomee')
        ant.move(todir: distributionTomeeDir.absolutePath) {
            fileset(dir: explodedPath) {
                include(name: "**/*")
            }
        }
        ant.move(todir: new File(dataDir, 'etc/tomee').absolutePath) {
            fileset(dir: new File(distributionTomeeDir, 'conf')) {
                include(name: "**/*")
            }
        }
        def initd = new File(dataDir, 'etc/init.d/')
        initd.mkdirs()
        new File(initd, 'tomee').withWriter { BufferedWriter out ->
            out.write(this.class.getResource('/init/tomee.sh').text)
        }
        ant.move(
                todir: new File(dataDir, 'var/lib/tomee').absolutePath,
                file: new File(distributionTomeeDir, 'webapps').absolutePath
        )
        new File(dataDir, 'var/log/tomee').mkdirs()
        new File(dataDir, 'var/tmp/tomee/temp').mkdirs()
        new File(dataDir, 'var/tmp/tomee/work').mkdirs()
        new File(distributionTomeeDir, 'conf').delete() // add link from "/usr/lib/tomee/conf" to "/etc/tomee"
        new File(distributionTomeeDir, 'logs').delete() // add link from "/usr/lib/tomee/logs" to "/var/log/tomee"
        new File(distributionTomeeDir, 'temp').delete() // add link from "/usr/lib/tomee/temp" to "/var/tmp/tomee/temp"
        new File(distributionTomeeDir, 'work').delete() // add link from "/usr/lib/tomee/work" to "/var/tmp/tomee/work"
        new File(distributionTomeeDir, 'webapps').delete() // add link from "/usr/lib/tomee/webapps" to "/var/lib/tomee/webapps"
        exploded.delete()
        dataDir.absolutePath
    }

    private File createTarGz(String path) {
        def dataDir = new File(path)
        def tarFile = new File(dataDir.parent, "${dataDir.name}.tar")
        def gzFile = new File(tarFile.parent, "${tarFile.name}.gz")
        ant.with {
            tar(destfile: tarFile) {
                tarfileset(dir: dataDir.absolutePath, username: 'root', group: 'root', prefix: './') {
                    include(name: "**/*")
                    exclude(name: "**/*.sh")
                    exclude(name: "**/postinst")
                    exclude(name: "**/prerm")
                    exclude(name: "**/init.d/tomee")
                }
                tarfileset(dir: dataDir.absolutePath, username: 'root', group: 'root', filemode: '755', prefix: './') {
                    include(name: "**/*.sh")
                    include(name: "**/postinst")
                    include(name: "**/prerm")
                    include(name: "**/init.d/tomee")
                }
            }
            gzip(src: tarFile, destfile: gzFile)
            delete(file: tarFile)
        }
        gzFile
    }


    private static void arItem(ArArchiveOutputStream output, File file) {
        def entry = new ArArchiveEntry(file.name, file.length())
        output.putArchiveEntry(entry)
        output << new FileInputStream(file)
        output.closeArchiveEntry()
    }

    private static void arDebianBinary(ArArchiveOutputStream output) {
        byte[] text = "2.0\n".getBytes()
        def entry = new ArArchiveEntry('debian-binary', text.length)
        output.putArchiveEntry(entry)
        output.write(text)
        output.closeArchiveEntry()
    }

    File compressFiles(String... paths) {
        def packageName = "apache-tomee-${properties.tomeeVersion}.deb"
        def ar = new File(new File(paths[0]).parent, packageName)
        def output = new ArArchiveOutputStream(new FileOutputStream(ar))
        arDebianBinary(output)
        paths.collect({
            createTarGz(it)
        }).each {
            arItem(output, it)
        }
        output.close()
        ar
    }

    void createPackage() {
        def filePath = new File(properties.workDir as String, 'tomee.zip').absolutePath
        def explodedPath = unzip(filePath)
        def dataDir = createDataDir(explodedPath)
        def controlDir = createControlDir(dataDir)
        def deb = compressFiles(controlDir, dataDir)
        deb.renameTo(new File(properties.buildDir as String, deb.name))
    }
}
