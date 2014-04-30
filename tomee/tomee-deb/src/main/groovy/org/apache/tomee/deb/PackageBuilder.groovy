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

import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import groovy.io.FileType
import groovy.text.GStringTemplateEngine
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.archivers.ar.ArArchiveEntry
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import org.apache.maven.project.MavenProject

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.zip.Deflater

class PackageBuilder {

    static final JIRA_SRV = 'https://issues.apache.org/jira'
    static final TOMCAT_EXPLODED_DIR = 'tomcat-exploded'
    static final TOMCAT_PACKAGE_NAME = 'lib-tomcat'

    def ant = new AntBuilder()
    MavenProject project

    private String executionDate = ''

    PackageBuilder(MavenProject myProject) {
        def dft = new SimpleDateFormat('yyyy-MM-dd-HH-mm-ss')
        dft.timeZone = TimeZone.getTimeZone('GMT-0')
        executionDate = dft.format(new Date())
        project = myProject
    }

    def getJiraData = {
        def restClient = null
        try {
            def factory = new AsynchronousJiraRestClientFactory()
            restClient = factory.create(new URI(JIRA_SRV), new AnonymousAuthenticationHandler())
            String version = project.version
            version = version.replaceAll('-SNAPSHOT$', '')
            def query = "project = TOMEE AND issuetype in standardIssueTypes() AND affectedVersion in (${version}) AND status in (Resolved, Closed)"
            return restClient.searchClient.searchJql(query).get(1, TimeUnit.MINUTES)
        } catch (e) {
            e.printStackTrace()
            return null
        }
        finally {
            restClient?.close()
        }
    }.memoize() // execute it just once per instance

    List<String> buildChangelogContent(String classifier) {
        def results = []
        String version = project.version
        version = version.replaceAll('-SNAPSHOT$', '')
        def templateFile = this.class.getResource('/changelog.template')
        getJiraData()?.issues?.each { Issue issue ->
            def urgency
            switch (issue.priority.name) {
                case 'Blocker':
                    urgency = 'critical'
                    break
                case 'Critical':
                    urgency = 'emergency'
                    break
                case 'Major':
                    urgency = 'high'
                    break
                case 'Minor':
                    urgency = 'medium'
                    break
                default: //Trivial
                    urgency = 'low'
                    break
            }
            def maintainer = issue.assignee ?: issue.reporter
            def template = new GStringTemplateEngine().createTemplate(templateFile).make([
                    classifier          : classifier,
                    tomeeVersion        : version,
                    urgency             : urgency,
                    issueTitle          : "[${issue.issueType.name}] ${issue.summary}",
                    issueID             : issue.key,
                    issueMaintainer     : maintainer.name,
                    issueMaintainerEmail: maintainer.emailAddress,
                    issueFixDate        : issue.updateDate.toString('EEE, d MMM yyyy HH:mm:ss Z')
            ])
            results << template.toString()
        }
        results
    }

    private void gz(String file, String gzipFile) {
        new File(file).withInputStream { fis ->
            new File(gzipFile).withOutputStream { fos ->
                def gzParams = new GzipParameters(
                        compressionLevel: Deflater.BEST_COMPRESSION
                )
                def gzo = new GzipCompressorOutputStream(fos, gzParams)
                gzo << fis
                gzo.close()
            }
        }
    }

    void buildChangelog(File docDir, String classifier) {
        def issues = buildChangelogContent(classifier)
        if (!issues) {
            return
        }
        def changelogFile = new File(docDir, 'changelog.Debian')
        changelogFile.withWriter { BufferedWriter writer ->
            issues.each { String issue ->
                writer.writeLine(issue)
            }
        }
        def changelogFileGz = new File(changelogFile.parent, 'changelog.Debian.gz')
        gz(changelogFile.absolutePath, changelogFileGz.absolutePath)
        changelogFile.delete()
    }

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

    private ControlData prepareControlData(String dataDirPath) {
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
        String version = project.version
        if (version.toLowerCase().endsWith('-snapshot')) {
            version += "-${executionDate}"
        }
        new ControlData(
                installedSize: installedSize,
                version: version,
                dataDir: dataDir,
                controlDir: controlDir
        )
    }

    private String createControlDir(String classifier, String dataDirPath) {
        def controlData = prepareControlData(dataDirPath)
        writeTemplate(new File(controlData.controlDir, 'control'), '/control/control.template', [
                classifier  : classifier,
                tomeeVersion: controlData.version,
                inMB        : controlData.installedSize.longValue()
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
        writeTemplate(new File(controlData.controlDir, 'postinst'), '/control/postinst.sh', [
                tomeeVersion: project.version,
                classifier  : classifier,
                priority    : priority
        ])
        writeTemplate(new File(controlData.controlDir, 'prerm'), '/control/prerm.sh', [
                tomeeVersion: project.version, classifier: classifier
        ])
        writeTemplate(new File(controlData.controlDir, 'postrm'), '/control/postrm.sh', [
                tomeeVersion: project.version, classifier: classifier
        ])
        new File(controlData.controlDir, 'conffiles').withWriter { BufferedWriter out ->
            new File(controlData.dataDir, "etc/tomee-${classifier}").eachFile {
                if (it.isFile()) {
                    out.writeLine("/etc/tomee-${classifier}/${it.name}")
                }
            }
            new File(controlData.dataDir, "etc/tomee-${classifier}/conf.d").eachFile {
                out.writeLine("/etc/tomee-${classifier}/conf.d/${it.name}")
            }
            out.writeLine("/etc/init.d/tomee-${classifier}")
        }
        controlData.controlDir.absolutePath
    }

    String createDataDir(String classifier, String explodedPath) {
        def exploded = new File(explodedPath)
        def outputDir = new File(exploded.parent, "output-${classifier}")
        def dataDir = new File(outputDir, 'data')
        dataDir.mkdirs()
        def distributionTomeeDir = new File(dataDir, "usr/share/tomee-${classifier}")
        ant.move(todir: distributionTomeeDir.absolutePath) {
            fileset(dir: explodedPath) {
                include(name: "**/*")
            }
        }
        new File(distributionTomeeDir, 'LICENSE').delete() // Unnecessary (lintian complains about this guy.)
        def homeConf = new File(dataDir, "etc/tomee-${classifier}")
        ant.move(todir: homeConf.absolutePath) {
            fileset(dir: new File(distributionTomeeDir, 'conf')) {
                include(name: "**/*")
            }
        }
        new File(homeConf, 'openejb.conf').withWriter { BufferedWriter out ->
            def data = this.class.getResource('/default.openejb.conf').text
            out.write(data)
        }
        new File(homeConf, 'tomee.xml').withWriter { BufferedWriter out ->
            def data = this.class.getResource('/tomee_xml.template').text
            out.write(data)
        }
        def homeConfD = new File(homeConf, 'conf.d')
        homeConfD.mkdirs()
        // Saving default configuration files
        new File(homeConfD, 'cxf.properties').withWriter { BufferedWriter out ->
            def data = this.class.getResource('/META-INF/org.apache.openejb.server.ServerService/cxf').text
            out.write(data)
        }
        new File(homeConfD, 'cxf-rs.properties').withWriter { BufferedWriter out ->
            def data = this.class.getResource('/META-INF/org.apache.openejb.server.ServerService/cxf-rs').text
            out.write(data)
        }
        new File(homeConfD, 'hsql.properties').withWriter { BufferedWriter out ->
            def data = this.class.getResource('/META-INF/org.apache.openejb.server.ServerService/hsql').text
            out.write(data)
        }
        def initd = new File(dataDir, 'etc/init.d/')
        initd.mkdirs()
        writeTemplate(new File(initd, "tomee-${classifier}"), '/init/tomee.sh', [
                classifier  : classifier,
                tomeeVersion: project.version
        ])
        def docDir = new File(dataDir, "usr/share/doc/tomee-${classifier}/")
        ant.move(todir: docDir.absolutePath) {
            fileset(file: new File(distributionTomeeDir, 'NOTICE').absolutePath)
            fileset(file: new File(distributionTomeeDir, 'RELEASE-NOTES').absolutePath)
            fileset(file: new File(distributionTomeeDir, 'RUNNING.txt').absolutePath)
        }
        buildChangelog(docDir, classifier)
        new File(dataDir, "var/log/tomee-${classifier}").mkdirs()
        new File(dataDir, "var/lib/tomee-${classifier}/conf").mkdirs()
        new File(dataDir, "var/lib/tomee-${classifier}/temp").mkdirs()
        new File(dataDir, "var/lib/tomee-${classifier}/work").mkdirs()
        new File(dataDir, "var/lib/tomee-${classifier}/webapps").mkdirs()
        new File(dataDir, "var/lib/tomee-${classifier}/apps").mkdirs()
        new File(distributionTomeeDir, 'conf').delete() // add link from "/usr/lib/tomee/conf" to "/etc/tomee"
        new File(distributionTomeeDir, 'logs').delete() // add link from "/usr/lib/tomee/logs" to "/var/log/tomee"
        new File(distributionTomeeDir, 'temp').delete() // add link from "/usr/lib/tomee/temp" to "/var/lib/tomee/temp"
        new File(distributionTomeeDir, 'work').delete() // add link from "/usr/lib/tomee/work" to "/var/lib/tomee/work"
        writeTemplate(
                new File(dataDir, "usr/share/doc/tomee-${classifier}/copyright"),
                '/copyright.template',
                [formattedDate: new Date().toString()]
        )
        def baseBinDir = new File(dataDir, "var/lib/tomee-${classifier}/bin")
        baseBinDir.mkdirs()
        writeTemplate(new File(baseBinDir, 'setenv.sh'), '/init/setenv.sh', [
                classifier  : classifier,
                tomeeVersion: project.version
        ])
        writeTemplate(new File(distributionTomeeDir, 'bin/tomee-instance.sh'), '/init/tomee-instance.sh', [
                classifier  : classifier,
                tomeeVersion: project.version
        ])
        exploded.delete()
        // Removing tomcat jars. It will come from an extra tomcat debian package.
        def tomcatLibs = new File(project.properties['distribution.workdir'] as String,
                "$TOMCAT_EXPLODED_DIR/apache-tomcat-${project.properties['tomcat.version']}/lib")
        ant.echo(message: "Removing Tomcat lib files from our TomEE distribution. Tomcat path: ${tomcatLibs.absolutePath}")
        tomcatLibs.eachFile { jar ->
            def existing = new File(dataDir, "usr/share/tomee-${classifier}/lib/${jar.name}")
            if (existing.exists()) {
                existing.delete()
            }
        }
        dataDir.absolutePath
    }

    void maskCodelessJars(String classifier, String dataDirPath) {
        def jars = []
        new File(dataDirPath).eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.jar')) {
                jars << it
            }
        }
        def codelessJars = jars.findAll { jar ->
            def explodedJar = new File(
                    project.properties['distribution.workdir'] as String,
                    "unjar/${classifier}/${jar.name}"
            )
            ant.unjar(src: jar, dest: explodedJar)
            def dotClassFiles = []
            explodedJar.eachFileRecurse(FileType.FILES) {
                if (it.name.endsWith('.class')) {
                    dotClassFiles << it
                }
            }
            explodedJar.listFiles(
                    { dir, file -> file ==~ /.*?\.class/ } as FilenameFilter
            )
            def result = dotClassFiles.size() == 0
            if (!result) {
                ant.delete(dir: explodedJar)
            }
            result
        }
        codelessJars.each { jar ->
            ant.echo(message: "Masking codeless jar: ${jar.absolutePath}")
            def explodedJar = new File(
                    project.properties['distribution.workdir'] as String,
                    "unjar/${classifier}/${jar.name}"
            )
            jar.delete()
            ant.zip(destfile: jar.absolutePath) {
                fileset(dir: explodedJar.absolutePath) {
                    exclude(name: '**/META-INF/MANIFEST.MF')
                }
            }
        }
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
                    if (classifier) {
                        exclude(name: "**/init.d/tomee-${classifier}")
                    }
                }
                tarfileset(dir: dataDir.absolutePath, username: 'root', group: 'root', filemode: '755', prefix: './') {
                    include(name: "**/*.sh")
                    include(name: "**/postinst")
                    include(name: "**/prerm")
                    include(name: "**/postrm")
                    if (classifier) {
                        include(name: "**/init.d/tomee-${classifier}")
                    }
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
        def ar = new File(new File(paths[0]).parent, 'distribution.deb')
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

    private File createPackageUnit(String classifier, String fileName) {
        def filePath = new File(project.properties['distribution.workdir'] as String, fileName).absolutePath
        def explodedPath = unzip(classifier, filePath)
        def dataDir = createDataDir(classifier, explodedPath)
        maskCodelessJars(classifier, dataDir)
        def controlDir = createControlDir(classifier, dataDir)
        def deb = compressFiles(classifier, controlDir, dataDir)
        def debDir = new File(project.build.directory as String, 'deb')
        debDir.mkdirs()
        def result = new File(debDir, "apache-tomee-${classifier}-${project.version}.deb")
        deb.renameTo(result)
        result
    }

    private File createLibPackage(String packageName, File[] jars) {
        def dataDir = new File(project.properties['distribution.workdir'] as String, "libDist/${packageName}/data")
        def libDistDir = new File(dataDir, "usr/share/tomee-${packageName}/lib")
        libDistDir.mkdirs()
        jars.each { jar ->
            ant.copy(file: jar, todir: libDistDir)
        }
        maskCodelessJars(packageName, dataDir.absolutePath)
        def docDir = new File(dataDir, "usr/share/doc/tomee-${packageName}")
        docDir.mkdirs()
        buildChangelog(docDir, packageName)
        writeTemplate(
                new File(docDir, 'copyright'),
                '/copyright.template',
                [formattedDate: new Date().toString()]
        )
        def controlData = prepareControlData(dataDir.absolutePath)
        writeTemplate(new File(controlData.controlDir, 'control'), '/control/lib_control.template', [
                packageName : packageName,
                tomeeVersion: controlData.version,
                inMB        : controlData.installedSize.longValue()
        ])
        def deb = compressFiles(null as String, controlData.controlDir.absolutePath, controlData.dataDir.absolutePath)
        def debDir = new File(project.build.directory as String, 'deb')
        debDir.mkdirs()
        def result = new File(debDir, "apache-tomee-${packageName}-${project.version}.deb")
        deb.renameTo(result)
        result
    }

    private File createTomcatLibPackage() {
        def exploded = new File(project.properties['distribution.workdir'] as String, TOMCAT_EXPLODED_DIR)
        ant.unzip(
                src: new File(project.properties['distribution.workdir'] as String, 'tomcat.zip'),
                dest: exploded
        )
        def tomcatLibs = new File(exploded, "apache-tomcat-${project.properties['tomcat.version']}/lib")
        createLibPackage(TOMCAT_PACKAGE_NAME, tomcatLibs.listFiles())
    }

    void createPackage() {
        createTomcatLibPackage()
        createPackageUnit('jaxrs', 'tomee-jaxrs.zip')
        createPackageUnit('webprofile', 'tomee-webprofile.zip')
        createPackageUnit('plume', 'tomee-plume.zip')
        createPackageUnit('plus', 'tomee-plus.zip')
    }

}
