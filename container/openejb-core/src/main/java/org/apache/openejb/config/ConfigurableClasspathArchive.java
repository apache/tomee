package org.apache.openejb.config;

import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.FilterList;
import org.apache.xbean.finder.filter.PackageFilter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ConfigurableClasspathArchive extends CompositeArchive implements ScanConstants {
    public ConfigurableClasspathArchive(final ClassLoader loader, final URL... urls) {
        this(loader, Arrays.asList(urls));
    }

    public ConfigurableClasspathArchive(final ClassLoader loader, final Iterable<URL> urls) {
        this(loader, false, urls);
    }

    public ConfigurableClasspathArchive(final ClassLoader loader, boolean forceDescriptor, final Iterable<URL> urls) {
        super(archive(loader, urls, forceDescriptor));
    }

    public static List<Archive> archive(final ClassLoader loader, final Iterable<URL> urls, boolean forceDescriptor) {
        final List<Archive> archives = new ArrayList<Archive>();
        for (URL location : urls) {
            try {
                archives.add(archive(loader, location, forceDescriptor));
            } catch (Exception e) {
                // ignored
            }
        }
        return archives;
    }

    public static Archive archive(final ClassLoader loader, final URL location, boolean forceDescriptor) {
        final ResourceFinder scanFinder = new ResourceFinder("", location);
        try {
            final URL scanXml = scanFinder.find(SystemInstance.get().getProperty(SCAN_XML_PROPERTY, SCAN_XML));
            final ScanUtil.ScanHandler scan = ScanUtil.read(scanXml);
            final Archive packageArchive = packageArchive(scan.getPackages(), loader, location);
            final Archive classesArchive = classesArchive(scan.getPackages(), scan.getClasses(), loader);

            if (packageArchive != null && classesArchive != null) {
                return new CompositeArchive(classesArchive, packageArchive);
            } else if (packageArchive != null) {
                return  packageArchive;
            }
            return classesArchive;
        } catch (IOException e) {
            if (forceDescriptor) {
                return new ClassesArchive();
            }
            return ClasspathArchive.archive(loader, location);
        }
    }

    public static Archive packageArchive(final Set<String> packageNames, final ClassLoader loader, final URL url) {
        if (!packageNames.isEmpty()) {
            return new FilteredArchive(ClasspathArchive.archive(loader, url), filters(packageNames));
        }
        return null;
    }

    private static Filter filters(final Set<String> packageNames) {
        final List<Filter> filters = new ArrayList<Filter>();
        for (String packageName : packageNames) {
            filters.add(new PackageFilter(packageName));
        }
        return new FilterList(filters);
    }

    public static Archive classesArchive(final Set<String> packages, final Set<String> classnames, final ClassLoader loader) {
        Class<?>[] classes = new Class<?>[classnames.size()];
        int i = 0;
        for (String clazz : classnames) {
            // skip classes managed by package filtering
            if (packages != null && clazzInPackage(packages, clazz)) {
                continue;
            }

            try {
                classes[i++] = loader.loadClass(clazz);
            } catch (ClassNotFoundException e) {
                // ignored
            }
        }

        if (i != classes.length) { // shouldn't occur
            final Class<?>[] updatedClasses = new Class<?>[i];
            System.arraycopy(classes, 0, updatedClasses, 0, i);
            classes = updatedClasses;
        }

        return new ClassesArchive(classes);
    }

    private static boolean clazzInPackage(final Collection<String> packagename, final String clazz) {
        for (String str : packagename) {
            if (clazz.startsWith(str)) {
                return true;
            }
        }
        return false;
    }
}
