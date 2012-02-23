package org.apache.openejb.config;

import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;

import java.io.IOException;
import java.net.URL;

public class WebappAggregatedArchive extends CompositeArchive implements ScanConstants {
    private static final String WEBAPP_GLOBAL_SCAN_LOCATION = "WEB-INF/" + SCAN_XML_NAME;

    public WebappAggregatedArchive(final ClassLoader loader, final Iterable<URL> urls) {
        super(new FilteredArchive(new AggregatedArchive(loader, urls), new ScanFilter(loader)));
    }

    private static class ScanFilter implements Filter {
        private ScanUtil.ScanHandler handler = null;
        private boolean active = false; // faster then using an empty handler

        public ScanFilter(final ClassLoader loader) {
            final URL scanXml = loader.getResource(WEBAPP_GLOBAL_SCAN_LOCATION);
            if (scanXml != null) {
                try {
                    handler = ScanUtil.read(scanXml);
                    active = true;
                } catch (IOException e) {
                    // ignored, will not use filtering with scan.xml
                }
            }
        }

        @Override
        public boolean accept(final String name) {
            if (active) {
                for (String packageName : handler.getPackages()) {
                    if (name.startsWith(packageName)) {
                        return true;
                    }
                }
                for (String className : handler.getClasses()) {
                    if (className.equals(name)) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
    }
}
