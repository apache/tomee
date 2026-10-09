/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.admin;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

import org.apache.openejb.client.RequestType;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.Server;
import org.apache.openejb.loader.AdminShutdownSecret;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class AdminDaemon implements ServerService {

    /*
     * Stop requests have to carry a secret right after the request code byte.
     * Unless one is configured, a random one is generated at every start and
     * written to the conf directory, readable by the owner only.
     */
    private static final int MAX_SECRET_LENGTH = 256;
    private static final int SECRET_READ_TIMEOUT = 10000;

    private boolean secretRequired = true;
    private byte[] secret;
    private File secretFile;

    @Override
    public void init(Properties props) throws Exception {
        final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class);
        secretRequired = SystemInstance.get().getOptions().get(AdminShutdownSecret.REQUIRED_PROPERTY, true);
        if (!secretRequired) {
            logger.warning(AdminShutdownSecret.REQUIRED_PROPERTY + "=false: any client able to connect to the admin port can stop this server");
            return;
        }

        final String configured = SystemInstance.get().getOptions().get(AdminShutdownSecret.PROPERTY, (String) null);
        if (configured != null && !configured.trim().isEmpty()) {
            secret = configured.trim().getBytes(StandardCharsets.UTF_8);
            return;
        }

        secret = generateSecret().getBytes(StandardCharsets.UTF_8);
        final File conf = SystemInstance.get().getConf(null);
        if (conf == null || !conf.isDirectory()) {
            logger.info("No conf directory, stop requests on the admin port will be refused");
            return;
        }
        final File file = new File(conf, AdminShutdownSecret.FILE_NAME);
        try {
            writeSecret(file, secret);
            secretFile = file;
        } catch (final IOException e) {
            logger.warning("Unable to write " + file.getAbsolutePath() + ", stop requests on the admin port will be refused", e);
        }
    }

    @Override
    public void service(Socket socket) throws ServiceException, IOException {

        try (InputStream in = socket.getInputStream()) {

            byte requestTypeByte = (byte) in.read();
            try {
                RequestType requestType = RequestType.valueOf(requestTypeByte);

                switch (requestType) {
                    case NOP_REQUEST:
                        return;
                    case STOP_REQUEST_Quit:
                    case STOP_REQUEST_quit:
                    case STOP_REQUEST_Stop:
                    case STOP_REQUEST_stop:
                        if (!isStopAllowed(socket, in)) {
                            Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class)
                                .warning("Refused stop request without a valid secret from " + socket.getInetAddress());
                            break;
                        }
                        Server server = SystemInstance.get().getComponent(Server.class);
                        if (null != server) {
                            server.stop();
                        }
                        break;
                    default:
                        //If this turns up in the logs then it is time to take action
                        Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class).warning("Invalid Server Socket request: " + requestType);
                        break;
                }
            } catch (IllegalArgumentException iae) {
                Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class).warning("Invalid Server Socket request: " + requestTypeByte);
            }

        } catch (Throwable e) {
            Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class).warning("Server Socket request failed", e);
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }
        }
    }

    private boolean isStopAllowed(final Socket socket, final InputStream in) throws IOException {
        if (!secretRequired) {
            return true;
        }
        if (secret == null) {
            return false;
        }
        socket.setSoTimeout(SECRET_READ_TIMEOUT);
        return MessageDigest.isEqual(secret, readSecret(in));
    }

    /**
     * Reads the secret sent after the request code, up to the end of the
     * stream, a NUL or a line break.
     */
    private static byte[] readSecret(final InputStream in) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1 && b != 0 && b != '\n' && b != '\r') {
            if (buffer.size() >= MAX_SECRET_LENGTH) {
                return new byte[0];
            }
            buffer.write(b);
        }
        return buffer.toByteArray();
    }

    private static String generateSecret() {
        final byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        final StringBuilder sb = new StringBuilder(random.length * 2);
        for (final byte b : random) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Writes the secret to a new file only its owner can read: POSIX
     * permissions where supported, else an owner-only ACL (Windows), else
     * best effort relying on the permissions of the conf directory. A
     * partly written file is removed on failure.
     */
    void writeSecret(final File file, final byte[] secret) throws IOException {
        final Path path = file.toPath();
        Files.deleteIfExists(path);
        final boolean posix = supportsPosix(path);
        try {
            if (posix) {
                Files.createFile(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
            } else {
                Files.createFile(path);
            }
        } catch (final FileAlreadyExistsException e) {
            throw new IOException(file.getAbsolutePath() + " was created concurrently", e);
        }
        try {
            if (!posix) {
                restrictToOwner(file);
            }
            Files.write(path, secret);
        } catch (final IOException | RuntimeException e) {
            Files.deleteIfExists(path);
            throw e;
        }
    }

    boolean supportsPosix(final Path path) {
        return path.getFileSystem().supportedFileAttributeViews().contains("posix");
    }

    private static void restrictToOwner(final File file) throws IOException {
        final AclFileAttributeView view = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
        if (view != null) {
            view.setAcl(Collections.singletonList(AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(view.getOwner())
                .setPermissions(AclEntryPermission.values())
                .build()));
            return;
        }
        // no ACL support (FAT, ...): the conf directory permissions apply
        final boolean restricted = file.setReadable(false, false) & file.setReadable(true, true)
            & file.setWritable(false, false) & file.setWritable(true, true);
        if (!restricted) {
            Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class)
                .info("Unable to restrict the permissions of " + file.getAbsolutePath()
                    + ", it is only protected by the permissions of its directory");
        }
    }

    @Override
    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }

    @Override
    public void start() throws ServiceException {
    }

    @Override
    public void stop() throws ServiceException {
        if (secretFile != null) {
            if (!secretFile.delete() && secretFile.exists()) {
                Logger.getInstance(LogCategory.OPENEJB_SERVER, AdminDaemon.class)
                    .warning("Unable to delete " + secretFile.getAbsolutePath());
            }
            secretFile = null;
        }
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public String getIP() {
        return "";
    }

    @Override
    public String getName() {
        return "admin thread";
    }
}
