/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.openejb.maven.plugin;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * The type BuildTomEEMojo.
 *
 * Create but not run a TomEE.
 */
@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class BuildTomEEMojo extends AbstractTomEEMojo {
    /**
     * The Zip.
     */
    @Deprecated
    @Parameter(property = "tomee-plugin.zip", defaultValue = "true")
    protected boolean zip;

    /**
     * The Attach.
     */
    @Parameter(property = "tomee-plugin.attach", defaultValue = "true")
    protected boolean attach;

    /**
     * The Zip file.
     */
    @Deprecated
    @Parameter(property = "tomee-plugin.zip-file", defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
    protected File zipFile;

    /**
     * The Base.
     */
    @Parameter(property = "tomee-plugin.output-base", defaultValue = "${project.build.directory}/${project.build.finalName}")
    protected File base;

    /**
     * The Project helper.
     */
    @Component
    protected MavenProjectHelper projectHelper;

    /**
     * The Classifier.
     */
    @Parameter(property = "tomee-plugin.classifier")
    protected String classifier = null;

    /**
     * Behaves as TomEE 1 Maven plugin ie zip structure will get bin/ conf/ lib/ ... directly in zip root.
     */
    @Parameter(property = "tomee-plugin.no-root", defaultValue = "false")
    protected boolean skipArchiveRootFolder;

    /**
     * config looks like:
     * &gt;formats&lt;
     *     &gt;zip>${project.build.directory}/${project.build.finalName}.zip&gt;/zip&lt;
     *     &gt;tar.gz /&lt;
     * &gt;/formats&lt;
     *
     * No value means auto format
     */
    @Parameter
    private Map<String, String> formats;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();

        if (formats == null) {
            formats = Collections.emptyMap();
        }

        String prefix = catalinaBase.getParentFile().getAbsolutePath();
        if (!prefix.endsWith(File.separator)) {
            prefix += File.separator;
        }
        if (skipArchiveRootFolder) {
            prefix += catalinaBase.getName() + File.separator;
        }

        if (zip || formats.containsKey("zip")) {
            getLog().info("Zipping Custom TomEE Distribution");

            final String zip = formats.get("zip");
            final File output = zip != null ? new File(zip) : zipFile;
            try (final ZipArchiveOutputStream zos =
                         new ZipArchiveOutputStream(new FileOutputStream(output))) {
                for (final String entry : catalinaBase.list()) {
                    zip(zos, new File(catalinaBase, entry), prefix);
                }
            } catch (final IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            attach("zip", output);
        }
        if (formats != null) {
            formats.remove("zip"); //handled previously for compatibility

            for (final Map.Entry<String, String> format : formats.entrySet()) {
                final String key = format.getKey();
                getLog().info(key + "-ing Custom TomEE Distribution");

                if ("tar.gz".equals(key)) {
                    final String out = format.getValue();
                    final File output = out != null ? new File(out) : new File(base.getParentFile(), base.getName() + "." + key);
                    Files.mkdirs(output.getParentFile());

                    try (final TarArchiveOutputStream tarGz =
                                 new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(output)))) {
                        tarGz.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                        for (final String entry : catalinaBase.list()) {
                            tarGz(tarGz, new File(catalinaBase, entry), prefix);
                        }
                    } catch (final IOException e) {
                        throw new MojoExecutionException(e.getMessage(), e);
                    }

                    attach(key, output);
                } else {
                    throw new MojoExecutionException(key + " format not supported");
                }
            }
        }
    }

    private void attach(final String ext, final File output) {
        if (attach) {
            getLog().info("Attaching Custom TomEE Distribution (" + ext + ")");
            if (classifier != null) {
                projectHelper.attachArtifact(project, ext, classifier, output);
            } else {
                projectHelper.attachArtifact(project, ext, output);
            }
        }
    }

    private void tarGz(final TarArchiveOutputStream tarGz, final File f, final String prefix) throws IOException {
        final String path = f.getPath().replace(prefix, "").replace(File.separator, "/");
        final TarArchiveEntry archiveEntry = new TarArchiveEntry(f, path);
        if (isSh(path)) {
            archiveEntry.setMode(0755);
        }
        tarGz.putArchiveEntry(archiveEntry);
        if (f.isDirectory()) {
            tarGz.closeArchiveEntry();
            final File[] files = f.listFiles();
            if (files != null) {
                for (final File child : files) {
                    tarGz(tarGz, child, prefix);
                }
            }
        } else {
            IO.copy(f, tarGz);
            tarGz.closeArchiveEntry();
        }
    }

    private void zip(final ZipArchiveOutputStream zip, final File f, final String prefix) throws IOException {
        final String path = f.getPath().replace(prefix, "").replace(File.separator, "/");
        final ZipArchiveEntry archiveEntry = new ZipArchiveEntry(f, path);
        if (isSh(path)) {
            archiveEntry.setUnixMode(0755);
        }
        zip.putArchiveEntry(archiveEntry);
        if (f.isDirectory()) {
            zip.closeArchiveEntry();
            final File[] files = f.listFiles();
            if (files != null) {
                for (final File child : files) {
                    zip(zip, child, prefix);
                }
            }
        } else {
            IO.copy(f, zip);
            zip.closeArchiveEntry();
        }
    }

    private boolean isSh(final String path) {
        return path.startsWith(catalinaBase.getName() + "/bin/") && path.endsWith(".sh");
    }

    @Override
    protected void run() {
        // don't start
    }

    @Override
    public String getCmd() {
        return null; // no need
    }

    @Override
    protected boolean getWaitTomEE() {
        return false;
    }
}
