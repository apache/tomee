package org.apache.tomee.deb

import groovy.io.FileType
import org.apache.commons.compress.archivers.ar.ArArchiveEntry
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import org.apache.maven.project.MavenProject

import java.util.zip.Deflater

class CompressTool {
    private def ant = new AntBuilder()
    private File workDir

    CompressTool(MavenProject project) {
        this.workDir = new File(project.properties['distribution.workdir'] as String)
    }

    void gz(String filePath, String gzipFilePath) {
        new File(filePath).withInputStream { fis ->
            new File(gzipFilePath).withOutputStream { fos ->
                def gzParams = new GzipParameters(
                        compressionLevel: Deflater.BEST_COMPRESSION
                )
                def gzo = new GzipCompressorOutputStream(fos, gzParams)
                gzo << fis
                gzo.close()
            }
        }
    }

    private void arDebianBinary(ArArchiveOutputStream output) {
        byte[] text = "2.0\n".getBytes()
        def entry = new ArArchiveEntry('debian-binary', text.length)
        output.putArchiveEntry(entry)
        output.write(text)
        output.closeArchiveEntry()
    }

    private File createTarGz(String classifier, File dataDir) {
        def tarFile = new File(dataDir.parent, "${dataDir.name}.tar")
        def gzFile = new File(dataDir.parent, "${tarFile.name}.gz")
        ant.with {
            tar(destfile: tarFile) {
                tarfileset(dir: dataDir, username: 'root', group: 'root', prefix: './') {
                    include(name: "**/*")
                    exclude(name: "**/*.sh")
                    exclude(name: "**/postinst")
                    exclude(name: "**/prerm")
                    exclude(name: "**/postrm")
                    exclude(name: "**/init.d/${classifier}")
                }
                tarfileset(dir: dataDir, username: 'root', group: 'root', filemode: '755', prefix: './') {
                    include(name: "**/*.sh")
                    include(name: "**/postinst")
                    include(name: "**/prerm")
                    include(name: "**/postrm")
                    include(name: "**/init.d/${classifier}")
                }
            }
            gzip(src: tarFile, destfile: gzFile)
        }
        tarFile.delete()
        gzFile
    }

    private void arItem(ArArchiveOutputStream output, File file) {
        def entry = new ArArchiveEntry(file.name, file.length())
        output.putArchiveEntry(entry)
        output << new FileInputStream(file)
        output.closeArchiveEntry()
    }

    private void compressFiles(String classifier) {
        def exploded = new File(workDir, "exploded-${classifier}")
        def dataDir = new File(exploded, 'data')
        def controlDir = new File(exploded, 'control')
        def ar = new File(exploded, 'distribution.deb')
        def output = new ArArchiveOutputStream(new FileOutputStream(ar))
        arDebianBinary(output)
        arItem(output, createTarGz(classifier, controlDir))
        arItem(output, createTarGz(classifier, dataDir))
        output.close()
        ar.renameTo(new File(workDir, "apache-${classifier}.deb"))
    }

    void createDebs() {
        workDir.eachFile(FileType.DIRECTORIES, { File dir ->
            String classifier = dir.name.substring("exploded-".size())
            compressFiles(classifier)
        })
    }
}
