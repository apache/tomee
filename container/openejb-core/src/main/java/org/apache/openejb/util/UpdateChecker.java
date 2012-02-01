package org.apache.openejb.util;

import java.net.URL;

public class UpdateChecker implements Runnable {
    private static final String SKIP_CHECK = "openejb.version.check.skip";
    private static final String REPO_URL = System.getProperty("openejb.version.check.repo.url", "http://repo1.maven.org/maven2/org/apache/openejb/");
    private static final String URL = System.getProperty("openejb.version.check.url", REPO_URL + "openejb/maven-metadata.xml");
    private static final String TAG = "latest";
    private static final String UNDEFINED = "undefined";
    private static String LATEST = "undefined";

    @Override
    public void run() {
        if (isSkipped()) {
            return;
        }

        try {
            final URL url = new URL(URL);
            final String metaData = IO.readFileAsString(url.toURI());
            LATEST = extractLatest(metaData);
        } catch (Exception e) {
            // ignored
        }
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

    public static boolean usesLatest() {
        return OpenEjbVersion.get().getVersion().equals(LATEST);
    }

    public static String message() {
        if (isSkipped()) {
            return "version checking is skipped";
        }

        if (UNDEFINED.equals(LATEST)) {
            return "can't determine the latest version";
        }

        final String version = OpenEjbVersion.get().getVersion();
        if (version.equals(LATEST)) {
            return "running on the latest version";
        }
        return new StringBuilder("you are using the version ").append(version)
                .append(", our latest stable version ").append(LATEST)
                .append(" is available on ").append(REPO_URL).toString();
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
