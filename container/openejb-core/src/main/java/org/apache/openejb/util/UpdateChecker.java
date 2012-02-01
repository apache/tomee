package org.apache.openejb.util;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateChecker implements Runnable {
    private static final String SKIP_CHECK = "openen.version.check.skip";
    private static final String REPO_URL = System.getProperty("openejb.version.check.repo.url", "http://repo1.maven.org/maven2/org/apache/openejb/");
    private static final String URL = System.getProperty("openejb.version.check.url", REPO_URL + "openejb/maven-metadata.xml");
    private static final String TAG = "latest";
    private static final AtomicBoolean DONE = new AtomicBoolean(false);
    private static final AtomicReference<String> RESULT = new AtomicReference<String>("");
    private static final CountDownLatch LATCH = new CountDownLatch(1);
    private static final String ERROR_MESSAGE = "can't check last version";
    public static final String UNDEFINED = "undefined";

    @Override
    public void run() {
        if (DONE.get() || isSkipped()) {
            return;
        }

        try {
            final URL url = new URL(URL);
            final String metaData = IO.readFileAsString(url);
            final String latest = extractLatest(metaData);
            RESULT.set(message(latest, OpenEjbVersion.get().getVersion()));
        } catch (Exception e) {
            DONE.set(true);
            RESULT.set(ERROR_MESSAGE);
        }
        LATCH.countDown();
    }

    private static String message(final String latest, final String version) {
        if (UNDEFINED.equals(latest)) {
            return "can't determine latest version";
        }
        if (version.equals(latest)) {
            return "running on the latest version";
        }
        return new StringBuilder("current version => ").append(version)
                .append(", latest stable version ").append(latest).append(" is available ")
                .append(" on ").append(REPO_URL).toString();
    }

    private static String extractLatest(final String metaData) {
        if (metaData != null) {
            boolean found = false;
            for (String s : metaData.replace(">", ">\n").split("\n")) {
                if (found) {
                    return trim(s).replace("</" + TAG + ">", "");
                }
                if (!s.isEmpty() && trim(s).endsWith("<" + TAG + ">")) {
                    found = true;
                }
            }
        }
        return UNDEFINED;
    }

    private static String trim(final String s) {
        return s.replace("\t", "").replace(" ", "");
    }

    public static String message() {
        if (isSkipped()) {
            return "version check is skipped";
        }

        try {
            LATCH.await();
        } catch (InterruptedException e) {
            return ERROR_MESSAGE;
        }
        return RESULT.get();
    }

    public static boolean isSkipped() {
        return System.getProperty(SKIP_CHECK) != null;
    }

    public static void main(String[] args) {
        UpdateChecker checker = new UpdateChecker();
        checker.run();
        System.out.println(UpdateChecker.message());
    }
}
