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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PerformanceTimer;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.ExcludeIncludeFilter;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;
import org.apache.xbean.finder.filter.IncludeExcludeFilter;
import org.apache.xbean.finder.filter.PatternFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class NewLoaderLogic {
    private static final Logger logger = DeploymentLoader.logger;
    public static final String ADDITIONAL_EXCLUDES = SystemInstance.get().getProperty("openejb.additional.exclude");
    public static final String ADDITIONAL_INCLUDE = SystemInstance.get().getProperty("openejb.additional.include");
    private static final String EXCLUSION_FILE = "exclusions.list";
    private static String[] exclusions = null;

    public static UrlSet filterArchives(final Filter filter, final ClassLoader classLoader, UrlSet urlSet) {

        for (final URL url : urlSet.getUrls()) {
            for (final Archive archive : ClasspathArchive.archives(classLoader, url)) {

                final FilteredArchive filtered = new FilteredArchive(archive, filter);

                if (!filtered.iterator().hasNext()) {
                    urlSet = urlSet.exclude(url);
                }

            }
        }

        return urlSet;
    }

    public static Set<String> callers() {

        final Set<String> callers = new LinkedHashSet<String>();

        final List<StackTraceElement> elements = new ArrayList<StackTraceElement>(Arrays.asList(new Exception().fillInStackTrace().getStackTrace()));

        // Yank out everything until we find a known ENTRY point
        // if we don't find one, so be it, this is only a convenience
        {
            // Entry points are the following:
            final Filter start = Filters.classes("javax.ejb.embeddable.EJBContainer", "javax.naming.InitialContext");

            final Iterator<StackTraceElement> iterator = elements.iterator();
            while (iterator.hasNext()) {
                final StackTraceElement element = iterator.next();
                iterator.remove();

                // If we haven't yet reached an entry point, just keep going
                if (!start.accept(element.getClassName())) continue;

                // We found an entry point.
                // Fast-forward past this class
                while (iterator.hasNext() && element.getClassName().equals(iterator.next().getClassName())) iterator.remove();

                // Ok, we have iterated up to the calling user class, so stop now
                break;
            }
        }


        // Now iterate till we find an END point
        // We don't want any of the classes after that
        {
            final Filter end = Filters.packages(
                    "junit.",
                    "org.junit.",
                    "org.testng.",
                    "org.apache.maven.",
                    "org.eclipse.",
                    "com.intellij."
            );

            // Everything between here and the end is part
            // of the call chain in which we are interested
            for (final StackTraceElement element : elements) {
                if (end.accept(element.getClassName())) break;

                callers.add(element.getClassName());
            }
        }

        // We don't need this anymore
        elements.clear();

        // Finally filter out everything that we definitely don't want
        {
            final Filter unwanted = Filters.packages(
                    "java.",
                    "javax.",
                    "sun.reflect."
            );

            final Iterator<String> classes = callers.iterator();
            while (classes.hasNext()) {
                if (unwanted.accept(classes.next())) classes.remove();
            }
        }


        return callers;
    }

    public static UrlSet applyBuiltinExcludes(final UrlSet urlSet) throws MalformedURLException {
        return applyBuiltinExcludes(urlSet, null);
    }

    public static UrlSet applyBuiltinExcludes(final UrlSet urlSet, final Filter includeFilter) throws MalformedURLException {
        final Filter filter = Filters.prefixes(getExclusions());

        //filter = Filters.optimize(filter, new PatternFilter(".*/openejb-.*"));
        final List<URL> urls = urlSet.getUrls();
        final Iterator<URL> iterator = urls.iterator();
        while (iterator.hasNext()) {
            final URL url = iterator.next();
            final File file = URLs.toFile(url);

            final String name = filter(file).getName();
            if (filter.accept(name) && (includeFilter == null || !includeFilter.accept(name))) {
                iterator.remove();
            }
        }

        return new UrlSet(urls);
    }

    public static void setExclusions(final String[] exclusionArray) {
        exclusions = exclusionArray;
    }

    public static String[] getExclusions() {

        if (exclusions != null) {
            return exclusions;
        }

        FileInputStream fis = null;

        try {
            final File conf = SystemInstance.get().getBase().getDirectory("conf");
            final File exclusionsFile = new File(conf, EXCLUSION_FILE);
            if (exclusionsFile.exists()) {
                fis = new FileInputStream(exclusionsFile);
                exclusions = readInputStreamList(fis);

                logger.info("Loaded classpath exclusions from: " + exclusionsFile.getAbsolutePath());
            }
        } catch (Throwable e) {
            // ignored
        } finally {
            IO.close(fis);
        }

        if (exclusions == null) {

            InputStream is = null;
            try {
                is = NewLoaderLogic.class.getResourceAsStream("/default.exclusions");
                exclusions = readInputStreamList(is);

                logger.debug("Loaded default.exclusions");

            } catch (Throwable e) {
                // ignored
            } finally {
                IO.close(is);
            }
        }

        final List<String> excludes = null != exclusions ? Arrays.asList(exclusions) : new ArrayList<String>();

        if (ADDITIONAL_EXCLUDES != null) {
            for (final String exclude : ADDITIONAL_EXCLUDES.split(",")) {
                excludes.add(exclude.trim());
            }
        }
        if (ADDITIONAL_INCLUDE != null) { // include = not excluded
            for (final String rawInclude : ADDITIONAL_INCLUDE.split(",")) {
                final String include = rawInclude.trim();
                final Iterator<String> excluded = excludes.iterator();
                while (excluded.hasNext()) {
                    if (excluded.next().startsWith(include)) {
                        excluded.remove();
                    }
                }
            }
        }

        return excludes.toArray(new String[excludes.size()]);
    }

    private static String[] readInputStreamList(final InputStream is) {

        final List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        String line;

        try {

            reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                final String value = line.trim();
                if (!value.isEmpty()) {
                    list.add(value);
                }
            }
        } catch (Throwable e) {
            logger.warning("readInputStreamList: Failed to read provided stream");
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }

        return list.toArray(new String[list.size()]);
    }

    private static File filter(File location) {
        final List<String> invalid = new ArrayList<String>();
        invalid.add("classes");
        invalid.add("test-classes");
        invalid.add("target");
        invalid.add("build");
        invalid.add("dist");
        invalid.add("bin");

        while (invalid.contains(location.getName())) {
            location = location.getParentFile();
        }
        return location;
    }


    public static void _loadFromClasspath(final FileUtils base, final List<URL> jarList, final ClassLoader classLoader) {

        final PerformanceTimer timer = new PerformanceTimer();

        timer.event("create filters");
        final Options options = SystemInstance.get().getOptions();
        final String include = "";
        final String exclude = "";
        final PatternFilter classpathInclude = new PatternFilter(options.get(DeploymentFilterable.CLASSPATH_INCLUDE, ".*"));
        final PatternFilter classpathExclude = new PatternFilter(options.get(DeploymentFilterable.CLASSPATH_EXCLUDE, ""));
        final Filter classpathFilter = new ExcludeIncludeFilter(classpathInclude, classpathExclude);


        final PatternFilter packageInclude = new PatternFilter(options.get(DeploymentFilterable.PACKAGE_INCLUDE, ".*"));
        final PatternFilter packageExclude = new PatternFilter(options.get(DeploymentFilterable.PACKAGE_EXCLUDE, ""));

        final IncludeExcludeFilter packageFilter;
        if (classpathInclude.getPattern().pattern().equals(".*") && packageInclude.getPattern().pattern().equals(".*")) {

            timer.event("callers");

            final Set<String> callers = callers();

            timer.event("parse packages");

            callers.size();

            final Set<String> packages = new HashSet<String>();
            for (final String caller : callers) {
                String[] parts = caller.split("\\.");
                if (parts.length > 2) {
                    parts = new String[]{parts[0], parts[1]};
                }
                packages.add(Join.join(".", parts));
            }

            final Filter includes = Filters.packages(packages.toArray(new String[packages.size()]));

            packageFilter = new IncludeExcludeFilter(includes, packageExclude);

        } else {

            packageFilter = new IncludeExcludeFilter(packageInclude, packageExclude);

        }

        timer.event("urlset");

        final Set<RequireDescriptors> requireDescriptors = options.getAll(DeploymentFilterable.CLASSPATH_REQUIRE_DESCRIPTOR, RequireDescriptors.CLIENT);
        final boolean filterDescriptors = options.get(DeploymentFilterable.CLASSPATH_FILTER_DESCRIPTORS, false);
        final boolean filterSystemApps = options.get(DeploymentFilterable.CLASSPATH_FILTER_SYSTEMAPPS, true);

        try {
            UrlSet urlSet = new UrlSet(classLoader);

            timer.event("exclude system urls");
            urlSet = urlSet.exclude(ClassLoader.getSystemClassLoader().getParent());
            urlSet = urlSet.excludeJavaExtDirs();
            urlSet = urlSet.excludeJavaEndorsedDirs();
            urlSet = urlSet.excludeJavaHome();
            urlSet = urlSet.excludePaths(System.getProperty("sun.boot.class.path", ""));
            urlSet = urlSet.exclude(".*/JavaVM.framework/.*");


            timer.event("classpath filter");

            final UrlSet beforeFiltering = urlSet;

            urlSet = urlSet.filter(classpathFilter);


            // If the user filtered out too much, that's a problem
            if (urlSet.size() == 0) {
                final String message = String.format("Classpath Include/Exclude resulted in zero URLs.  There were %s possible URLs before filtering and 0 after: include=\"%s\", exclude=\"%s\"", beforeFiltering.size(), include, exclude);
                logger.error(message);
                logger.info("Eligible Classpath before filtering:");

                for (final URL url : beforeFiltering) {
                    logger.info(String.format("   %s", url.toExternalForm()));
                }
//                throw new IllegalStateException(message);

            }

            // If they are the same size, than nothing was filtered
            // and we know the user did not take action to change the default
            final boolean userSuppliedClasspathFilter = beforeFiltering.size() != urlSet.size();

            if (!userSuppliedClasspathFilter) {

                logger.info("Applying buildin classpath excludes");
                timer.event("buildin excludes");
                urlSet = applyBuiltinExcludes(urlSet);

            }

            DeploymentsResolver.processUrls(urlSet.getUrls(), classLoader, EnumSet.allOf(RequireDescriptors.class), base, jarList);


            timer.event("package filter");

            urlSet = filterArchives(packageFilter, classLoader, urlSet);

            timer.event("process urls");

            // we should exclude system apps before and apply user properties after
//            if (filterSystemApps){
//                urlSet = urlSet.exclude(".*/openejb-[^/]+(.(jar|ear|war)(!/)?|/target/(test-)?classes/?)");
//            }

            final List<URL> urls = urlSet.getUrls();
            final int size = urls.size();
//            if (size == 0) {
//                logger.warning("No classpath URLs matched.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
//                return;
//            } else if (size == 0 && (!filterDescriptors && prefiltered.getUrls().size() == 0)) {
//                return;
//            } else if (size < 20) {
//                logger.debug("Inspecting classpath for applications: " + urls.size() + " urls.");
//            } else {
//                // Has the user allowed some module types to be discoverable via scraping?
//                boolean willScrape = requireDescriptors.size() < RequireDescriptors.values().length;
//
//                if (size < 50 && willScrape) {
//                    logger.info("Inspecting classpath for applications: " + urls.size() + " urls. Consider adjusting your exclude/include.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
//                } else if (willScrape) {
//                    logger.warning("Inspecting classpath for applications: " + urls.size() + " urls.");
//                    logger.warning("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
//                }
//            }

            final long begin = System.currentTimeMillis();
            DeploymentsResolver.processUrls(urls, classLoader, requireDescriptors, base, jarList);
            final long end = System.currentTimeMillis();
            final long time = end - begin;

            timer.stop(System.out);

            final UrlSet unchecked = new UrlSet();
//            if (!filterDescriptors){
//                unchecked = prefiltered.exclude(urlSet);
//                if (filterSystemApps){
//                    unchecked = unchecked.exclude(".*/openejb-[^/]+(.(jar|ear|war)(./)?|/target/classes/?)");
//                }
            DeploymentsResolver.processUrls(unchecked.getUrls(), classLoader, EnumSet.allOf(RequireDescriptors.class), base, jarList);
//            }

            if (logger.isDebugEnabled()) {
                final int urlCount = urlSet.getUrls().size() + unchecked.getUrls().size();
                logger.debug("URLs after filtering: " + urlCount);
                for (final URL url : urlSet.getUrls()) {
                    logger.debug("Annotations path: " + url);
                }
                for (final URL url : unchecked.getUrls()) {
                    logger.debug("Descriptors path: " + url);
                }
            }

            if (urls.size() == 0) return;

            if (time < 1000) {
                logger.debug("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
            } else if (time < 4000 || urls.size() < 3) {
                logger.info("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
            } else if (time < 10000) {
                logger.warning("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
                logger.warning("Consider adjusting your " + DeploymentFilterable.CLASSPATH_EXCLUDE + " and " + DeploymentFilterable.CLASSPATH_INCLUDE + " settings.  Current settings: exclude='" + exclude + "', include='" + include + "'");
            } else {
                logger.fatal("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.  TOO LONG!");
                logger.fatal("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " + DeploymentFilterable.CLASSPATH_EXCLUDE + "='" + exclude + "', " + DeploymentFilterable.CLASSPATH_INCLUDE + "='" + include + "'");
                final List<String> list = new ArrayList<String>();
                for (final URL url : urls) {
                    list.add(url.toExternalForm());
                }
                Collections.sort(list);
                for (final String url : list) {
                    logger.info("Matched: " + url);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            logger.warning("Unable to search classpath for modules: Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
        }

    }
}
