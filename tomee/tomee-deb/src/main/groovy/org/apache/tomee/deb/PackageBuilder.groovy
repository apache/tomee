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

    String unzip(String classifier, String tarPath) {
        def outputDir = new File(new File(tarPath).parent, "exploded-${classifier}")
        ant.delete(includeemptydirs: true, dir: outputDir.absolutePath) // remove old files
        outputDir.mkdirs()
        ant.unzip(
                src: tarPath,
                dest: outputDir.absolutePath
        )
        def files = outputDir.listFiles()
        if (files.size() == 1) {
            outputDir = files[0]
        }
        ant.delete(includeemptydirs: true) {
            fileset(dir: outputDir.absolutePath, includes: '**/*.txt')
            fileset(dir: outputDir.absolutePath, includes: '**/*.exe')
            fileset(dir: outputDir.absolutePath, includes: '**/*.bat')
            fileset(dir: outputDir.absolutePath, includes: '**/*.original')
            fileset(dir: outputDir.absolutePath, includes: '**/*.tmp')
        }
        ant.delete(includeemptydirs: true, dir: new File(outputDir, 'webapps').absolutePath)
        outputDir.absolutePath
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

    private void writeTemplate(File file, String templatePath, Map variables) {
        try {
            file.withWriter { BufferedWriter out ->
                def template = new GStringTemplateEngine().createTemplate(
                        this.class.getResource(templatePath)
                ).make(variables)
                out.write(template.toString())
            }
        } catch (e) {
            throw new RuntimeException("Error building $templatePath", e)
        }
    }

    String createControlDir(String classifier, String dataDirPath) {
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
        writeTemplate(new File(controlDir, 'control'), '/control/control.template', [
                classifier  : classifier,
                tomeeVersion: properties.tomeeVersion,
                inMB        : installedSize.longValue()
        ])
        def priority
        switch (classifier) {
            case 'plus':
                priority = 4
                break
            case 'plume':
                priority = 3
                break
            case 'jaxrs':
                priority = 2
                break
            default:
                priority = 1
        }
        writeTemplate(new File(controlDir, 'postinst'), '/control/postinst.sh', [
                tomeeVersion: properties.tomeeVersion,
                classifier  : classifier,
                priority    : priority
        ])
        writeTemplate(new File(controlDir, 'prerm'), '/control/prerm.sh', [tomeeVersion: properties.tomeeVersion, classifier: classifier])
        writeTemplate(new File(controlDir, 'postrm'), '/control/postrm.sh', [tomeeVersion: properties.tomeeVersion, classifier: classifier])
        new File(controlDir, 'conffiles').withWriter { BufferedWriter out ->
            new File(dataDir, "etc/tomee/${classifier}/${properties.tomeeVersion}").eachFile {
                out.writeLine("/etc/tomee/${classifier}/${properties.tomeeVersion}/${it.name}")
            }
            out.writeLine("/etc/init.d/tomee-${classifier}")
        }
        controlDir.absolutePath
    }

    String createDataDir(String classifier, String explodedPath) {
        def exploded = new File(explodedPath)
        def outputDir = new File(exploded.parent, "output-${classifier}")
        def dataDir = new File(outputDir, 'data')
        dataDir.mkdirs()
        def distributionTomeeDir = new File(dataDir, "usr/share/tomee/${classifier}/${properties.tomeeVersion}")
        ant.move(todir: distributionTomeeDir.absolutePath) {
            fileset(dir: explodedPath) {
                include(name: "**/*")
            }
        }
        def homeConf = new File(dataDir, "etc/tomee/${classifier}/${properties.tomeeVersion}")
        ant.move(todir: homeConf.absolutePath) {
            fileset(dir: new File(distributionTomeeDir, 'conf')) {
                include(name: "**/*")
            }
        }
        def initd = new File(dataDir, 'etc/init.d/')
        initd.mkdirs()
        writeTemplate(new File(initd, "tomee-${classifier}"), '/init/tomee.sh', [
                classifier  : classifier,
                tomeeVersion: properties.tomeeVersion
        ])
        ant.move(todir: new File(dataDir, "usr/share/doc/tomee/${classifier}/${properties.tomeeVersion}/").absolutePath) {
            fileset(file: new File(distributionTomeeDir, 'LICENSE').absolutePath)
            fileset(file: new File(distributionTomeeDir, 'NOTICE').absolutePath)
            fileset(file: new File(distributionTomeeDir, 'RELEASE-NOTES').absolutePath)
            fileset(file: new File(distributionTomeeDir, 'RUNNING.txt').absolutePath)
        }
        new File(dataDir, "var/log/tomee/${classifier}/${properties.tomeeVersion}").mkdirs()
        def baseConfDir = new File(dataDir, "var/lib/tomee/${classifier}/${properties.tomeeVersion}/conf")
        baseConfDir.mkdirs()
        ant.copy(todir: baseConfDir.absolutePath) {
            fileset(file: new File(homeConf, 'server.xml'))
        }
        def baseBinDir = new File(dataDir, "var/lib/tomee/${classifier}/${properties.tomeeVersion}/bin")
        baseBinDir.mkdirs()
        new File(dataDir, "var/lib/tomee/${classifier}/${properties.tomeeVersion}/temp").mkdirs()
        new File(dataDir, "var/lib/tomee/${classifier}/${properties.tomeeVersion}/work").mkdirs()
        new File(dataDir, "var/lib/tomee/${classifier}/${properties.tomeeVersion}/webapps").mkdirs()
        new File(distributionTomeeDir, 'conf').delete() // add link from "/usr/lib/tomee/conf" to "/etc/tomee"
        new File(distributionTomeeDir, 'logs').delete() // add link from "/usr/lib/tomee/logs" to "/var/log/tomee"
        new File(distributionTomeeDir, 'temp').delete() // add link from "/usr/lib/tomee/temp" to "/var/lib/tomee/temp"
        new File(distributionTomeeDir, 'work').delete() // add link from "/usr/lib/tomee/work" to "/var/lib/tomee/work"
        writeTemplate(
                new File(dataDir, "usr/share/doc/tomee/${classifier}/${properties.tomeeVersion}/copyright"),
                '/copyright.template',
                [formattedDate: new Date().toString()]
        )
        writeTemplate(new File(baseBinDir, 'setenv.sh'), '/init/setenv.sh', [
                classifier  : classifier,
                tomeeVersion: properties.tomeeVersion
        ])
        writeTemplate(new File(distributionTomeeDir, 'bin/tomee-instance.sh'), '/init/tomee-instance.sh', [
                classifier  : classifier,
                tomeeVersion: properties.tomeeVersion
        ])
        exploded.delete()
        dataDir.absolutePath
    }

    private File createTarGz(String classifier, String path) {
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
                    exclude(name: "**/postrm")
                    exclude(name: "**/init.d/tomee-${classifier}")
                }
                tarfileset(dir: dataDir.absolutePath, username: 'root', group: 'root', filemode: '755', prefix: './') {
                    include(name: "**/*.sh")
                    include(name: "**/postinst")
                    include(name: "**/prerm")
                    include(name: "**/postrm")
                    include(name: "**/init.d/tomee-${classifier}")
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

    File compressFiles(String classifier, String... paths) {
        def packageName = "apache-tomee-${classifier}-${properties.tomeeVersion}.deb"
        def ar = new File(new File(paths[0]).parent, packageName)
        def output = new ArArchiveOutputStream(new FileOutputStream(ar))
        arDebianBinary(output)
        paths.collect({
            createTarGz(classifier, it)
        }).each {
            arItem(output, it)
        }
        output.close()
        ar
    }

    void createPackage(String classifier, String fileName) {
        def filePath = new File(properties.workDir as String, fileName).absolutePath
        def explodedPath = unzip(classifier, filePath)
        def dataDir = createDataDir(classifier, explodedPath)
        def controlDir = createControlDir(classifier, dataDir)
        def deb = compressFiles(classifier, controlDir, dataDir)
        deb.renameTo(new File(properties.buildDir as String, deb.name))
    }
}
