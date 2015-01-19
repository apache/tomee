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

import org.apache.maven.project.MavenProject

class PackageBuilder {

    private MavenProject project
    private CompressTool compressTool
    private File workDir
    private Map<String, PackageInfo> packageInfoMap = [:]

    PackageBuilder(MavenProject myProject) {
        project = myProject
        compressTool = new CompressTool(myProject)
        workDir = new File(myProject.properties['distribution.workdir'] as String)
    }

    private void createLibStructure(List<String> dependencyOrder) {
        Set<String> previousJars = []
        dependencyOrder.each { classifier ->
            def recurse = { DataBuilder builder, String path, File file ->
                if (previousJars.contains(file.name)) {
                    file.delete()
                } else {
                    previousJars << file.name
                    file.renameTo(new File(file.parentFile, "libtomee-${classifier}-${file.name}"))
                }
            }
            def builder = new DataBuilder(project, "libtomee-${classifier}-java", "tomee-${classifier}.zip", compressTool)
                    .buildDataDir()
                    .move('lib', 'usr/share/java')
                    .eachFileRecurse('usr/share/java', recurse)
                    .writeTemplate("usr/share/doc/libtomee-${classifier}-java/copyright", '/data/copyright.template', [:])
                    .move('NOTICE', "usr/share/doc/libtomee-${classifier}-java/NOTICE")
                    .move('RELEASE-NOTES', "usr/share/doc/libtomee-${classifier}-java/RELEASE-NOTES")
                    .buildChangelog()
                    .clean()
            packageInfoMap.put("libtomee-${classifier}-java", new PackageInfo(
                    classifier: "libtomee-${classifier}-java",
                    dataBuilder: builder
            ))
        }
        def writeTpl = { ControlBuilder me, String toPath, String tplPath, String classifier ->
            def params = [
                    classifier  : classifier,
                    tomeeVersion: me.version,
                    inMB        : me.installedSize.longValue().toString()
            ] as Map<String, String>
            me.writeTemplate(toPath, tplPath, params)
        }
        def myDepency = null
        dependencyOrder.each { String classifier ->
            String libClassifier = "libtomee-${classifier}-java"
            def builder = new ControlBuilder(project, "libtomee-${classifier}-java").buildControlDir()
                    .withMe({
                writeTpl(it as ControlBuilder, 'control', '/control/lib_control.template', libClassifier)
            })
            if (myDepency) {
                builder.withBaseDir({ File baseDir ->
                    def control = new File(baseDir, 'control/control')
                    control.append("Depends: libtomee-${myDepency}-java (= ${builder.version})\n")
                })
            }
            myDepency = classifier
            packageInfoMap.get("libtomee-${classifier}-java").controlBuilder = builder
        }
    }

    private Set<String> getLibJarNames(String classifier) {
        def workDir = new File(project.properties['distribution.workdir'] as String)
        def libDir = new File(workDir, "exploded-libtomee-${classifier}-java/data/usr/share/java")
        int startIndex = "libtomee-${classifier}-".size()
        libDir.list().collect { it.substring(startIndex) }
    }

    private void createDataStructure(String classifier) {
        def jarsWebprofile = getLibJarNames('webprofile')
        def jarsPlus = getLibJarNames('plus')
        def jarsPlume = getLibJarNames('plume')
        def recurse = { DataBuilder builder, String path, File file ->
            def addSymLink = { jarClassifier ->
                builder.addSymbolicLink(path, "/usr/share/java/libtomee-${jarClassifier}-${file.name}")
                file.delete()
            }
            if (jarsWebprofile.contains(file.name)) {
                addSymLink('webprofile')
            } else if (jarsPlus.contains(file.name)) {
                addSymLink('plus')
            } else if (jarsPlume.contains(file.name)) {
                addSymLink('plume')
            }
        }
        def tplParams = [classifier: classifier, tomeeVersion: project.version]
        def builder = new DataBuilder(project, "tomee-${classifier}", "tomee-${classifier}.zip", compressTool)
                .buildDataDir()
                .move('lib', "usr/share/tomee-${classifier}/lib")
                .eachFileRecurse("usr/share/tomee-${classifier}/lib", recurse)
                .move('endorsed', "usr/share/tomee-${classifier}/endorsed")
                .move('bin', "usr/share/tomee-${classifier}/bin")
                .move('conf', "etc/tomee-${classifier}")
                .writeTemplate("usr/share/doc/tomee-${classifier}/copyright", '/data/copyright.template', [:])
                .move('NOTICE', "usr/share/doc/tomee-${classifier}/NOTICE")
                .move('RELEASE-NOTES', "usr/share/doc/tomee-${classifier}/RELEASE-NOTES")
                .copyContent("etc/tomee-${classifier}/conf.d/cxf.properties", '/META-INF/org.apache.openejb.server.ServerService/cxf')
                .copyContent("etc/tomee-${classifier}/conf.d/cxf-rs.properties", '/META-INF/org.apache.openejb.server.ServerService/cxf-rs')
                .copyContent("etc/tomee-${classifier}/conf.d/hsql.properties", '/META-INF/org.apache.openejb.server.ServerService/hsql')
                .copyContent("var/lib/tomee-${classifier}/bin/setenv.sh", '/init/setenv.sh')
                .copyContent("etc/tomee-${classifier}/openejb.conf", '/default.openejb.conf')
                .copyContent("etc/tomee-${classifier}/tomee.xml", '/tomee_xml.template')
                .writeTemplate("usr/share/tomee-${classifier}/bin/tomee-instance.sh", '/init/tomee-instance.sh', tplParams)
                .writeTemplate("etc/init.d/tomee-${classifier}", '/init/tomee.sh', tplParams)
                .buildChangelog()
                .clean()
        packageInfoMap.put(classifier, new PackageInfo(
                classifier: classifier,
                dataBuilder: builder
        ))
    }

    private String getLibLinks(String classifier) {
        def sw = new StringWriter()
        def bw = new BufferedWriter(sw)
        packageInfoMap.get(classifier).dataBuilder.symbolicLinks.each { String path, String reference ->
            bw.writeLine("ln -sf ${reference} ${path}")
        }
        bw.close()
        sw.close()
        sw.toString().trim()
    }

    private createControlStructure(String classifier) {
        def writeTpl = { ControlBuilder me, String toPath, String tplPath ->
            def params = [
                    classifier  : classifier,
                    tomeeVersion: me.version,
                    inMB        : me.installedSize.longValue().toString(),
                    priority    : me.priority,
                    libLinks    : getLibLinks(classifier)
            ] as Map<String, String>
            me.writeTemplate(toPath, tplPath, params)
        }
        def writeConffiles = { ControlBuilder me ->
            me.withWriter('conffiles', { BufferedWriter out ->
                me.withBaseDir { File baseDir ->
                    def dataDir = new File(baseDir, 'data')
                    new File(dataDir, "etc/tomee-${classifier}").eachFile {
                        if (it.isFile()) {
                            out.writeLine("/etc/tomee-${classifier}/${it.name}")
                        }
                    }
                    new File(dataDir, "etc/tomee-${classifier}/conf.d").eachFile {
                        out.writeLine("/etc/tomee-${classifier}/conf.d/${it.name}")
                    }
                    out.writeLine("/etc/init.d/tomee-${classifier}")
                }
            })
        }
        new ControlBuilder(project, "tomee-${classifier}").buildControlDir()
                .withMe({ writeTpl(it as ControlBuilder, 'control', '/control/control.template') })
                .withMe({ writeTpl(it as ControlBuilder, 'postinst', '/control/postinst.sh') })
                .withMe({ writeTpl(it as ControlBuilder, 'prerm', '/control/prerm.sh') })
                .withMe({ writeTpl(it as ControlBuilder, 'postrm', '/control/postrm.sh') })
                .withMe(writeConffiles)
    }

    void createPackage() {
        def classifiers = ['webprofile', 'plus', 'plume']
        createLibStructure(classifiers)
        classifiers.each { createDataStructure(it) }
        classifiers.each { createControlStructure(it) }
        compressTool.createDebs()
    }

}
