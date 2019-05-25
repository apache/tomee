/*
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
package org.apache.tomee.server.composer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class CleanOnExit {

    private final List<File> files = new ArrayList<>();
    private final List<Process> processes = new ArrayList<>();

    public CleanOnExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::clean));
    }

    public File clean(final File file) {
        this.files.add(file);
        return file;
    }

    public Process clean(final Process process) {
        this.processes.add(process);
        return process;
    }

    public void clean() {
        processes.stream().forEach(Process::destroy);
        for (final Process process : processes) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        files.stream().forEach(this::delete);
    }

    private void delete(final File file) {
        try {
            java.nio.file.Files.walkFileTree(file.toPath(), new RecursiveDelete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class RecursiveDelete implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            java.nio.file.Files.deleteIfExists(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            java.nio.file.Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
