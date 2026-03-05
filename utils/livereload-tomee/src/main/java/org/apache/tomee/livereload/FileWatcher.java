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
package org.apache.tomee.livereload;

import org.apache.johnzon.mapper.Mapper;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWatcher {
    private final Logger logger;
    private final Mapper mapper;
    private final Collection<Session> sessions = new ArrayList<>();

    public FileWatcher(final LogCategory logger, final Mapper mapper) {
        this.logger = Logger.getInstance(logger, FileWatcher.class);
        this.mapper = mapper;
    }

    public synchronized void addSession(final Session session) {
        sessions.add(session);
    }

    public synchronized void removeSession(final Session session) {
        sessions.remove(session);
    }

    public Closeable watch(final String folder) {
        final File file = new File(folder);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(folder + " is not a directory");
        }

        try {
            final AtomicBoolean again = new AtomicBoolean(true);
            final Path path = file.getAbsoluteFile().toPath();
            final WatchService watchService = path.getFileSystem().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            final Thread watcherThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (again.get()) {
                        try {
                            final WatchKey key = watchService.poll(1, TimeUnit.SECONDS); // don't use take to not block forever
                            if (key == null) {
                                continue;
                            }

                            for (final WatchEvent<?> event : key.pollEvents()) {
                                final WatchEvent.Kind<?> kind = event.kind();
                                if (kind != StandardWatchEventKinds.ENTRY_CREATE
                                    && kind != StandardWatchEventKinds.ENTRY_DELETE
                                    && kind != StandardWatchEventKinds.ENTRY_MODIFY) {
                                    continue;
                                }

                                final Path updatedPath = Path.class.cast(event.context());
                                if (kind == StandardWatchEventKinds.ENTRY_DELETE || updatedPath.toFile().isFile()) {
                                    final String path = updatedPath.toString();
                                    if (path.endsWith("___jb_tmp___") || path.endsWith("___jb_old___")) {
                                        continue;
                                    } else if (path.endsWith("~")) {
                                        onChange(path.replace(File.pathSeparatorChar, '/').substring(0, path.length() - 1));
                                    } else {
                                        onChange(path.replace(File.pathSeparatorChar, '/'));
                                    }
                                }
                            }
                            key.reset();
                        } catch (final InterruptedException e) {
                            Thread.interrupted();
                            again.set(false);
                        } catch (final ClosedWatchServiceException cwse) {
                            // ok, we finished there
                        }
                    }
                }
            });
            watcherThread.setName("livereload-tomee-watcher(" + folder + ")");
            watcherThread.start();

            return new Closeable() {
                @Override
                public void close() throws IOException {
                    synchronized (this) {
                        for (final Session s : sessions) {
                            removeSession(s);
                            try {
                                s.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "container shutdowned"));
                            } catch (final Exception e) {
                                // ok: not important there
                            }
                        }
                    }
                    again.compareAndSet(true, false);
                    try {
                        watchService.close();
                    } catch (final IOException ioe) {
                        logger.warning("Error closing the watch service for " + folder + "(" + ioe.getMessage() + ")");
                    }
                    try {
                        watcherThread.join(TimeUnit.MINUTES.toMillis(1));
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            };
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void onChange(final String path) {
        final Command command = new Command();
        command.setCommand("reload");
        command.setPath(path);
        command.setLiveCss(true); // TODO: check path to set it to false if not relevant?

        final String asStr = mapper.writeObjectAsString(command);

        final Collection<Session> copy;
        synchronized (this) {
            copy = new ArrayList<>(this.sessions);
        }

        int failed = 0;
        for (final Session s : copy) {
            try {
                s.getBasicRemote().sendText(asStr);
            } catch (final Exception e) {
                logger.warning("Can't send one livereload update: " + e.getMessage());
                failed++;
            }
        }
        if (failed < copy.size()) {
            logger.info("Updated livereload path: " + path);
        }
    }
}
