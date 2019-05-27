/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jul.handler.rotating;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * NOTE: for simplicity the prefix `org.apache.tomee.jul.handler.rotating.LocalFileHandler.` has been removed of name columns.
 * <p>
 * |===
 * | Name                      | Default Value                                     | Description
 * | filenamePattern           | ${catalina.base}/logs/logs.%s.%03d.log            | where log files are created, it uses String.format() and gives you the date and file number - in this order.
 * | limit                     | 10 Megabytes                                      | limit size indicating the file should be rotated
 * | dateCheckInterval         | 5 seconds                                         | how often the date should be computed to rotate the file (don't do it each time for performances reason, means you can get few records of next day in a file name with current day)
 * | bufferSize                | -1 bytes                                          | if positive the in memory buffer used to store data before flushing them to the disk
 * | encoding                  | -                                                 | file encoding
 * | level                     | ALL                                               | level this handler accepts
 * | filter                    | -                                                 | filter used to check if the message should be logged
 * | formatter                 | java.util.logging.SimpleFormatter                 | formatter used to format messages
 * | archiveDirectory          | ${catalina.base}/logs/archives/                   | where compressed logs are put.
 * | archiveFormat             | gzip                                              | zip or gzip.
 * | archiveOlderThan          | -1 days                                           | how many days files are kept before being compressed
 * | purgeOlderThan            | -1 days                                           | how many days files are kept before being deleted, note: it applies on archives and not log files so 2 days of archiving and 3 days of purge makes it deleted after 5 days.
 * | compressionLevel          | -1                                                | In case of zip archiving the zip compression level (-1 for off or 0-9).
 * | formatterPattern          | -                                                 | SimpleFormatter pattern (ignored if formatter is provided).
 * | formatterLocale           | -                                                 | Locale to use.
 * |===
 * </p>
 * <p>
 * NOTE: archiving and purging are done only when a file is rotated, it means it can be ignored during days if there is no logging activity.
 * </p>
 * <p>
 * NOTE: archiving and purging is done in a background thread pool, you can configure the number of threads in thanks to
 * `org.apache.tomee.jul.handler.rotating.BackgroundTaskRunner.threads` property in `conf/logging.properties`.
 * Default is 2 which should be fine for most applications.
 * </p>
 */
/*
 Open point/enhancements:
  - date pattern/filename pattern instead of hardcoded String.format?
  - write another async version? ensure it flushed well, use disruptor? -> bench seems to show it is useless
 */
public class LocalFileHandler extends Handler {
    private static final int BUFFER_SIZE = 8102;

    private long limit = 0;
    private int bufferSize = -1;
    private Pattern filenameRegex;
    private Pattern archiveFilenameRegex;
    private String filenamePattern = "${catalina.base}/logs/logs.%s.%03d.log";
    private String archiveFormat = "gzip";
    private long dateCheckInterval = TimeUnit.SECONDS.toMillis(5);
    private long archiveExpiryDuration;
    private int compressionLevel;
    private long purgeExpiryDuration;
    private File archiveDir;

    private volatile int currentIndex;
    private volatile long lastTimestamp;
    private volatile String date;
    private volatile PrintWriter writer;
    private volatile int written;
    private final ReadWriteLock writerLock = new ReentrantReadWriteLock();
    private final Lock backgroundTaskLock = new ReentrantLock();
    private volatile boolean closed;

    public LocalFileHandler() {
        configure();
    }

    private void configure() {
        date = currentDate();

        final String className = getClass().getName(); //allow classes to override

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        dateCheckInterval = new Duration(getProperty(className + ".dateCheckInterval", String.valueOf(dateCheckInterval))).asMillis();
        filenamePattern = replace(getProperty(className + ".filenamePattern", filenamePattern));
        limit = new Size(getProperty(className + ".limit", String.valueOf("10 Mega"))).asBytes();

        final int lastSep = Math.max(filenamePattern.lastIndexOf('/'), filenamePattern.lastIndexOf('\\'));
        String fileNameReg = lastSep >= 0 ? filenamePattern.substring(lastSep + 1) : filenamePattern;
        fileNameReg = fileNameReg.replace("%s", "\\d{4}\\-\\d{2}\\-\\d{2}"); // date.
        {   // file rotation index
            final int indexIdxStart = fileNameReg.indexOf('%');
            if (indexIdxStart >= 0) {
                final int indexIdxEnd = fileNameReg.indexOf('d', indexIdxStart);
                if (indexIdxEnd >= 0) {
                    fileNameReg = fileNameReg.substring(0, indexIdxStart) + "\\d*" + fileNameReg.substring(indexIdxEnd + 1, fileNameReg.length());
                }
            }
        }
        filenameRegex = Pattern.compile(fileNameReg);

        compressionLevel = Integer.parseInt(getProperty(className + ".compressionLevel", String.valueOf(Deflater.DEFAULT_COMPRESSION)));
        archiveExpiryDuration = new Duration(getProperty(className + ".archiveOlderThan", String.valueOf("-1 days"))).asMillis();
        archiveDir = new File(replace(getProperty(className + ".archiveDirectory", "${catalina.base}/logs/archives/")));
        archiveFormat = replace(getProperty(className + ".archiveFormat", archiveFormat));
        archiveFilenameRegex = Pattern.compile(fileNameReg + "\\." + archiveFormat);

        purgeExpiryDuration = new Duration(getProperty(className + ".purgeOlderThan", String.valueOf("-1 days"))).asMillis();

        try {
            bufferSize = (int) new Size(getProperty(className + ".bufferSize", "-1 b")).asBytes();
        } catch (final NumberFormatException ignore) {
            // no-op
        }

        final String encoding = getProperty(className + ".encoding", null);
        if (encoding != null && encoding.length() > 0) {
            try {
                setEncoding(encoding);
            } catch (final UnsupportedEncodingException ex) {
                // no-op
            }
        }

        setLevel(Level.parse(getProperty(className + ".level", "" + Level.ALL)));

        final String filterName = getProperty(className + ".filter", null);
        if (filterName != null) {
            try {
                setFilter(Filter.class.cast(cl.loadClass(filterName).newInstance()));
            } catch (final Exception e) {
                // Ignore
            }
        }

        final String formatterName = getProperty(className + ".formatter", null);
        if (formatterName != null) {
            try {
                setFormatter(Formatter.class.cast(cl.loadClass(formatterName).newInstance()));
            } catch (final Exception e) {
                setFormatter(newSimpleFormatter(className));
            }
        } else {
            setFormatter(newSimpleFormatter(className));
        }

        setErrorManager(new ErrorManager());

        lastTimestamp = System.currentTimeMillis();
    }

    private Formatter newSimpleFormatter(final String className) {
        final String defaultFormat = System.getProperty("java.util.logging.SimpleFormatter.format", "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n");
        final String format = getProperty(className + ".formatterPattern", defaultFormat);
        final String locale = getProperty(className + ".formatterLocale", null);
        return new PatternFormatter(format, locale == null ? Locale.getDefault() : newLocale(locale));
    }

    private Locale newLocale(final String str) { // LocaleUtils [lang3]
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return new Locale("", "");
        }
        if (str.contains("#")) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final char ch0 = str.charAt(0);
        if (ch0 == '_') {
            if (len < 3) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 3) {
                return new Locale("", str.substring(1, 3));
            }
            if (len < 5) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (str.charAt(3) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale("", str.substring(1, 3), str.substring(4));
        }

        final String[] split = str.split("_", -1);
        final int occurrences = split.length -1;
        switch (occurrences) {
            case 0:
                return new Locale(str.toUpperCase(Locale.ENGLISH));
            case 1:
                return new Locale(split[0], split[1]);
            case 2:
                return new Locale(split[0], split[1], split[2]);
            default:
                throw new IllegalArgumentException("Invalid locale format: " + str);
        }
    }

    protected String currentDate() {
        return new Timestamp(System.currentTimeMillis()).toString().substring(0, 10);
    }

    @Override
    public void publish(final LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        final long now = System.currentTimeMillis();
        final String tsDate;
        // just do it once / sec if we have a lot of log, can make some log appearing in the wrong file but better than doing it each time
        if (now - lastTimestamp > dateCheckInterval) { // using as much as possible volatile to avoid to lock too much
            lastTimestamp = now;
            tsDate = currentDate();
        } else {
            tsDate = null;
        }

        try {
            writerLock.readLock().lock();
            rotateIfNeeded(tsDate);

            final String result;
            try {
                result = getFormatter().format(record);
            } catch (final Exception e) {
                reportError(null, e, ErrorManager.FORMAT_FAILURE);
                return;
            }

            try {
                if (writer != null) {
                    writer.write(result);
                    if (bufferSize < 0) {
                        writer.flush();
                    }
                } else {
                    reportError(getClass().getSimpleName() + " is closed or not yet initialized, unable to log [" + result + "]", null, ErrorManager.WRITE_FAILURE);
                }
            } catch (final Exception e) {
                reportError(null, e, ErrorManager.WRITE_FAILURE);
            }
        } finally {
            writerLock.readLock().unlock();
        }
    }

    private void rotateIfNeeded(final String currentDate) {
        if (!closed && writer == null) {
            try {
                writerLock.readLock().unlock();
                writerLock.writeLock().lock();

                if (!closed && writer == null) {
                    openWriter();
                }
            } finally {
                writerLock.writeLock().unlock();
                writerLock.readLock().lock();
            }
        } else if (shouldRotate(currentDate)) {
            try {
                writerLock.readLock().unlock();
                writerLock.writeLock().lock();

                if (shouldRotate(currentDate)) {
                    close();
                    if (currentDate != null && !date.equals(currentDate)) {
                        currentIndex = 0;
                        date = currentDate;
                    }
                    openWriter();
                }
            } finally {
                writerLock.writeLock().unlock();
                writerLock.readLock().lock();
            }
        }
    }

    private boolean shouldRotate(final String currentDate) { // new day, new file or limit exceeded
        return (currentDate != null && !date.equals(currentDate)) || (limit > 0 && written >= limit);
    }

    @Override
    public void close() {
        closed = true;

        writerLock.writeLock().lock();
        try {
            if (writer == null) {
                return;
            }
            writer.write(getFormatter().getTail(this));
            writer.flush();
            writer.close();
            writer = null;
        } catch (final Exception e) {
            reportError(null, e, ErrorManager.CLOSE_FAILURE);
        } finally {
            writerLock.writeLock().unlock();
        }

        // wait for bg tasks if running
        backgroundTaskLock.lock();
        backgroundTaskLock.unlock();
    }

    @Override
    public void flush() {
        writerLock.readLock().lock();
        try {
            writer.flush();
        } catch (final Exception e) {
            reportError(null, e, ErrorManager.FLUSH_FAILURE);
        } finally {
            writerLock.readLock().unlock();
        }
    }

    protected void openWriter() {
        final long beforeRotation = System.currentTimeMillis();

        writerLock.writeLock().lock();
        FileOutputStream fos = null;
        OutputStream os = null;
        try {
            File pathname;
            do {
                pathname = new File(formatFilename(filenamePattern, date, currentIndex));
                final File parent = pathname.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    reportError("Unable to create [" + parent + "]", null, ErrorManager.OPEN_FAILURE);
                    writer = null;
                    return;
                }
                currentIndex++;
            } while (pathname.isFile()); // loop to ensure we don't overwrite existing files

            final String encoding = getEncoding();
            fos = new FileOutputStream(pathname, true);
            os = new CountingStream(bufferSize > 0 ? new BufferedOutputStream(fos, bufferSize) : fos);
            writer = new PrintWriter((encoding != null) ? new OutputStreamWriter(os, encoding) : new OutputStreamWriter(os), false);
            writer.write(getFormatter().getHead(this));
        } catch (final Exception e) {
            reportError(null, e, ErrorManager.OPEN_FAILURE);
            writer = null;
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e1) {
                    // no-op
                }
            }
        } finally {
            writerLock.writeLock().unlock();
        }

        BackgroundTaskRunner.push(new Runnable() {
            @Override
            public void run() {
                backgroundTaskLock.lock();
                try {
                    evict(beforeRotation);
                } catch (final Exception e) {
                    reportError("Can't do the log eviction", e, ErrorManager.GENERIC_FAILURE);
                } finally {
                    backgroundTaskLock.unlock();
                }
            }
        });
    }

    private void evict(final long now) {
        if (purgeExpiryDuration > 0) { // purging archives
            final File[] archives = archiveDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return archiveFilenameRegex.matcher(name).matches();
                }
            });

            if (archives != null) {
                for (final File archive : archives) {
                    try {
                        final BasicFileAttributes attr = Files.readAttributes(archive.toPath(), BasicFileAttributes.class);
                        if (now - attr.creationTime().toMillis() > purgeExpiryDuration) {
                            if (!Files.deleteIfExists(archive.toPath())) {
                                // dont try to delete on exit cause we will find it again
                                reportError("Can't delete " + archive.getAbsolutePath() + ".", null, ErrorManager.GENERIC_FAILURE);
                            }
                        }
                    } catch (final IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        if (archiveExpiryDuration > 0) { // archiving log files
            final File[] logs = new File(formatFilename(filenamePattern, "0000-00-00", 0)).getParentFile()
                    .listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return filenameRegex.matcher(name).matches();
                        }
                    });

            if (logs != null) {
                for (final File file : logs) {
                    try {
                        final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        if (attr.creationTime().toMillis() < now && now - attr.lastModifiedTime().toMillis() > archiveExpiryDuration) {
                            createArchive(file);
                        }
                    } catch (final IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }

    private String formatFilename(final String pattern, final String date, final int index) {
        return String.format(pattern, date, index);
    }

    private void createArchive(final File source) {
        final File target = new File(archiveDir, source.getName() + "." + archiveFormat);
        if (target.isFile()) {
            return;
        }

        final File parentFile = target.getParentFile();
        if (!parentFile.isDirectory() && !parentFile.mkdirs()) {
            throw new IllegalStateException("Can't create " + parentFile.getAbsolutePath());
        }

        if (archiveFormat.equalsIgnoreCase("gzip")) {
            try (final OutputStream outputStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(target)))) {
                final byte[] buffer = new byte[BUFFER_SIZE];
                try (final FileInputStream inputStream = new FileInputStream(source)) {
                    copyStream(inputStream, outputStream, buffer);
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        } else { // consider file defines a zip whatever extension it is
            try (final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(target))) {
                outputStream.setLevel(compressionLevel);

                final byte[] buffer = new byte[BUFFER_SIZE];
                try (final FileInputStream inputStream = new FileInputStream(source)) {
                    final ZipEntry zipEntry = new ZipEntry(source.getName());
                    outputStream.putNextEntry(zipEntry);
                    copyStream(inputStream, outputStream, buffer);
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        try {
            if (!Files.deleteIfExists(source.toPath())) {
                reportError("Can't delete " + source.getAbsolutePath() + ".", null, ErrorManager.GENERIC_FAILURE);
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void copyStream(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer) throws IOException {
        int n;
        while ((n = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, n);
        }
    }

    protected String getProperty(final String name, final String defaultValue) {
        String value = LogManager.getLogManager().getProperty(name);
        if (value == null) {
            value = defaultValue;
        } else {
            value = value.trim();
        }
        return value;
    }

    protected static String replace(final String str) { // [lang3] would be good but no dep for these classes is better
        String result = str;
        int start = str.indexOf("${");
        if (start >= 0) {
            final StringBuilder builder = new StringBuilder();
            int end = -1;
            while (start >= 0) {
                builder.append(str, end + 1, start);
                end = str.indexOf('}', start + 2);
                if (end < 0) {
                    end = start - 1;
                    break;
                }

                final String propName = str.substring(start + 2, end);
                String replacement = !propName.isEmpty() ? System.getProperty(propName) : null;
                if (replacement == null) {
                    replacement = System.getenv(propName);
                }
                if (replacement != null) {
                    builder.append(replacement);
                } else {
                    builder.append(str, start, end + 1);
                }
                start = str.indexOf("${", end + 1);
            }
            builder.append(str, end + 1, str.length());
            result = builder.toString();
        }
        return result;
    }

    private final class CountingStream extends OutputStream {
        private final OutputStream out;

        private CountingStream(final OutputStream out) {
            this.out = out;
            written = 0;
        }

        @Override
        public void write(final int b) throws IOException {
            out.write(b);
            written++;
        }

        @Override
        public void write(final byte[] buff) throws IOException {
            out.write(buff);
            written += buff.length;
        }

        @Override
        public void write(final byte[] buff, final int off, final int len) throws IOException {
            out.write(buff, off, len);
            written += len;
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    public static class PatternFormatter extends Formatter {
        private final ThreadLocal<Date> date = new ThreadLocal<Date>() {
            @Override
            protected Date initialValue() {
                return new Date();
            }
        };

        private final String format;
        private final Locale locale;

        public PatternFormatter(final String format, final Locale locale) {
            this.format = format;
            this.locale = locale;
        }

        @Override
        public String format(final LogRecord record) {
            final Date date = this.date.get();
            date.setTime(record.getMillis());

            String source;
            final String sourceClassName = record.getSourceClassName();
            final String sourceMethodName = record.getSourceMethodName();
            if (sourceClassName != null) {
                source = sourceClassName;
                if (sourceMethodName != null) {
                    source += " " + sourceMethodName;
                }
            } else {
                source = record.getLoggerName();
            }

            final String message = formatMessage(record);

            String throwable = "";
            final Throwable thrown = record.getThrown();
            if (thrown != null) {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                pw.println();
                thrown.printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }

            return String.format(
                    locale, format,
                    date, source,
                    record.getLoggerName(),
                    Locale.ENGLISH == locale ? record.getLevel().getName() : record.getLevel().getLocalizedName(),
                    message, throwable,
                    sourceClassName == null ? source : sourceClassName,
                    sourceMethodName == null ? source : sourceMethodName);
        }
    }
}
