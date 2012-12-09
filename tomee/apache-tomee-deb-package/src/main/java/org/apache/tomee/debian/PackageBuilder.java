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

package org.apache.tomee.debian;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

public class PackageBuilder {
    private final String user;
    private final String group;

    private long size = 0;

    public PackageBuilder(String user, String group) {
        this.user = user;
        this.group = group;
    }

    private File uncompress(File gz) throws IOException, CompressorException {
        final File output = new File(gz.getParent(), FilenameUtils.getBaseName(gz.getName()));
        output.delete();

        final InputStream is = new FileInputStream(gz);
        final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("gz", is);
        IOUtils.copy(in, new FileOutputStream(output));
        in.close();

        return output;
    }

    private File compress(File file) throws IOException, CompressorException {
        final File output = new File(file.getParent(), file.getName() + ".gz");
        output.delete();

        final OutputStream out = new FileOutputStream(output);

        final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream("gz", out);
        final FileInputStream is = new FileInputStream(file);
        IOUtils.copy(is, cos);

        is.close();
        cos.close();
        out.close();

        return output;
    }

    private File untar(File tar) throws IOException, ArchiveException {
        final File output = new File(tar.getParent(), FilenameUtils.getBaseName(tar.getName()));
        try {
            FileUtils.deleteDirectory(output);
        } catch (IOException e) {
            throw new PackageException(e);
        }

        final InputStream is = new FileInputStream(tar);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", is);

        for (TarArchiveEntry entry = (TarArchiveEntry) in.getNextEntry(); entry != null; entry = (TarArchiveEntry) in.getNextEntry()) {
            final File newFile = new File(output, entry.getName());

            if (entry.isDirectory()) {
                newFile.mkdirs();
            } else {
                final OutputStream out = new FileOutputStream(newFile);
                this.size = this.size + IOUtils.copy(in, out);
                out.close();
            }
        }
        in.close();

        return output;
    }

    private String getCheckSumLine(String name, File file) throws IOException {
        final FileInputStream fis = new FileInputStream(file);
        final String md5 = DigestUtils.md5Hex(fis);
        fis.close();
        return md5 + " " + name;
    }

    private void tar(File baseDir, PrintWriter checksumWriter, File file, ArchiveOutputStream os, Map<String, Integer> modeMappings) throws IOException {
        if (!baseDir.equals(file)) {
            final int length = baseDir.getAbsolutePath().length();
            final String name = file.getAbsolutePath().substring(length).replaceAll("\\\\", "/");
            final TarArchiveEntry entry = new TarArchiveEntry(file);
            entry.setName(name);

            entry.setUserName(this.user);
            entry.setGroupName(this.group);

            if (modeMappings != null && modeMappings.containsKey(name)) {
                entry.setMode(modeMappings.get(name));
            }

            os.putArchiveEntry(entry);
            if (file.isFile()) {
                if (checksumWriter != null) {
                    checksumWriter.println(getCheckSumLine(name, file));
                }

                final FileInputStream is = new FileInputStream(file);
                IOUtils.copy(is, os);
                is.close();
            }
            os.closeArchiveEntry();
        }

        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (null != children) for (File child : children) {
                tar(baseDir, checksumWriter, child, os, modeMappings);
            }
        }
    }

    private File tar(File src, File checksum, Map<String, Integer> modeMappings) throws IOException, ArchiveException {
        final File output = new File(src.getParent(), src.getName() + ".tar");
        output.delete();

        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("tar", out);

        if (checksum == null) {
            tar(src, null, src, os, modeMappings);

        } else {
            final PrintWriter checksumWriter = new PrintWriter(checksum);
            tar(src, checksumWriter, src, os, modeMappings);
            checksumWriter.close();

        }

        os.close();
        out.flush();
        out.close();

        return output;
    }


    private File ar(File folder, String name) throws IOException, ArchiveException {
        final File output = new File(folder.getParent(), name + ".deb");
        output.delete();

        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.AR, out);

        ar(new File(folder, "debian-binary"), os);
        ar(new File(folder, "control.tar.gz"), os);
        ar(new File(folder, "data.tar.gz"), os);

        os.close();
        out.flush();
        out.close();

        return output;
    }

    private void ar(File file, ArchiveOutputStream os) throws IOException {
        os.putArchiveEntry(new ArArchiveEntry(file.getName(), file.length()));

        final InputStream input = new FileInputStream(file);
        try {
            IOUtils.copy(input, os);
        } finally {
            input.close();
        }

        os.closeArchiveEntry();
    }

    public File expandTarGz(File tarGz) {
        try {
            final File tar = uncompress(tarGz);
            final File result = untar(tar);

            tar.delete();

            return result;
        } catch (Exception e) {
            throw new PackageException(e);
        }
    }

    public File buildDataTarGzFolder(File baseDir, Map<String, String> dirMappings) {
        final File dataTarGzDir = new File(baseDir.getParent(), "data");
        try {
            FileUtils.deleteDirectory(dataTarGzDir);
        } catch (IOException e) {
            throw new PackageException(e);
        }

        final int baseDirLength = baseDir.getAbsolutePath().length();
        final File[] files = baseDir.listFiles();
        if (null != files) for (File entry : files) {
            final String basePath = entry.getAbsolutePath().substring(baseDirLength).replaceAll("\\\\", "/");

            final String dest = dirMappings.get(basePath);
            if (dest == null) {
                throw new PackageException("i don't know where the entry should go. Entry: " + basePath);
            }

            final File destFile = new File(dataTarGzDir, dest);
            try {
                FileUtils.moveToDirectory(entry, destFile, true);
            } catch (IOException e) {
                throw new PackageException(e);
            }
        }

        try {
            FileUtils.deleteDirectory(baseDir);
        } catch (IOException e) {
            throw new PackageException(e);
        }

        return dataTarGzDir;
    }

    public File compressTarGz(File src, File checksum, Map<String, Integer> modeMappings) {
        if (checksum != null) {
            checksum.delete();
        }

        try {
            final File tar = tar(src, checksum, modeMappings);

            if (src.isDirectory()) {
                FileUtils.deleteDirectory(src);
            } else {
                src.delete();
            }

            final File result = compress(tar);
            tar.delete();

            return result;
        } catch (Exception e) {
            throw new PackageException(e);
        }
    }

    public void createDebPackage(String isControl, String isPostinst, String isPrerm, File md5sums, File data, File deb) throws IOException {
        final File debFolder = new File(deb.getParent(), "DEBIAN");
        debFolder.mkdirs();

        FileUtils.cleanDirectory(debFolder);
        FileUtils.moveToDirectory(data, debFolder, true);

        final File control = new File(debFolder, "control");
        control.mkdirs();

        FileUtils.writeStringToFile(new File(control, "control"), isControl);
        FileUtils.writeStringToFile(new File(control, "postinst"), isPostinst);
        FileUtils.writeStringToFile(new File(control, "prerm"), isPrerm);
        FileUtils.moveToDirectory(md5sums, control, true);

        FileUtils.writeStringToFile(new File(debFolder, "debian-binary"), "2.0\n");

        compressTarGz(control, null, null);

        try {
            ar(debFolder, FilenameUtils.getBaseName(deb.getName()));
        } catch (ArchiveException e) {
            throw new PackageException(e);
        }

        FileUtils.deleteDirectory(debFolder);
    }

    public File createDebPackage(String name, String version,
                                 File sourceTarGz, String isControl, String isPostinst, String isPrerm,
                                 Map<String, String> dirMapping,
                                 Map<String, Integer> modeMapping
    ) {
        final File expandedTarGz = expandTarGz(sourceTarGz);

        final File[] files = expandedTarGz.listFiles();
        final File root = (null != files ? files[0] : expandedTarGz);
        final File dataTarGzFolder = buildDataTarGzFolder(files[0], dirMapping);

        final File md5sums = new File(sourceTarGz.getParent(), "md5sums");
        final File data = compressTarGz(
                dataTarGzFolder,
                md5sums,
                modeMapping);

        final File deb = new File(sourceTarGz.getParent(), name + "-" + version + ".deb");
        try {
            createDebPackage(
                    "Installed-Size: " + (this.size / 1024) + "\n" + isControl,
                    isPostinst,
                    isPrerm,
                    md5sums,
                    data,
                    deb
            );
        } catch (IOException e) {
            throw new PackageException(e);
        }

        try {
            FileUtils.deleteDirectory(expandedTarGz);
        } catch (IOException e) {
            throw new PackageException(e);
        }

        return deb;
    }
}
