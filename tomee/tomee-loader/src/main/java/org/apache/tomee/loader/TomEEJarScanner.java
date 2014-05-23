/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tomee.loader;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.startup.TldConfig;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@SuppressWarnings("unchecked")
public class TomEEJarScanner extends StandardJarScanner {

    private static final Log log = LogFactory.getLog(StandardJarScanner.class);

    public static final String DEEP_TREE_MATCH = "**";

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP = System.getProperty("path.separator");
    private static final boolean ON_NETWARE = isNetware();
    private static final boolean ON_DOS = isDos();

    protected static final Set<String[]> DEFAULT_JARS_TO_SKIP;

    /**
     * The string resources for this package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private static final Method tldConfigScanStream;
    private static final Field tldConfig;
    private static final Method tldLocationScanStream;
    private static final Field tldLocationCache;

    // tld of the server
    private static final Set<URL> SERVER_URLS;

    // TldConfig of the server (common)
    private static final Set<String> TAG_LIB_URIS;
    private static final ArrayList<String> LISTENERS;

    // TldLocationCache of the server (common)
    private static final Hashtable<String, Object> MAPPINGS;

    private static final Thread SERVER_SCANNING_THREAD;

    static {
        final Set<String> defaultJarsToSkip = new HashSet<String>();
        final String jarList = System.getProperty(Constants.SKIP_JARS_PROPERTY);
        if (jarList != null) {
            final StringTokenizer tokenizer = new StringTokenizer(jarList, ",");
            while (tokenizer.hasMoreElements()) {
                defaultJarsToSkip.add(tokenizer.nextToken());
            }
        }

        final Set<String[]> ignoredJarsTokens = new HashSet<String[]>();
        for (final String pattern : defaultJarsToSkip) {
            ignoredJarsTokens.add(tokenizePathAsArray(pattern));
        }
        DEFAULT_JARS_TO_SKIP = ignoredJarsTokens;

        try {
            final ClassLoader loader = TomEEJarScanner.class.getClassLoader();

            tldConfigScanStream = TldConfig.class.getDeclaredMethod("tldScanStream", InputStream.class);
            tldConfigScanStream.setAccessible(true);
            tldConfig = loader.loadClass("org.apache.catalina.startup.TldConfig$TldJarScannerCallback")
                .getDeclaredFields()[0]; // there is a unique field and this way it is portable
            //.getDeclaredField("this$0");
            tldConfig.setAccessible(true);

            final Class<?> tldLocationsCache = loader.loadClass("org.apache.jasper.compiler.TldLocationsCache");
            tldLocationScanStream = tldLocationsCache.getDeclaredMethod("tldScanStream", String.class, String.class, InputStream.class);
            tldLocationScanStream.setAccessible(true);
            tldLocationCache = loader.loadClass("org.apache.jasper.compiler.TldLocationsCache$TldJarScannerCallback")
                .getDeclaredFields()[0];
            tldLocationCache.setAccessible(true);

            // init server cache
            SERVER_URLS = TldScanner.scan(TomEEJarScanner.class.getClassLoader());

            final Context fakeWebApp = (Context) Proxy.newProxyInstance(loader, new Class<?>[]{Context.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if ("getTldNamespaceAware".equals(method.getName())) {
                            return Globals.STRICT_SERVLET_COMPLIANCE;
                        } else if ("getTldValidation".equals(method.getName())) {
                            return Globals.STRICT_SERVLET_COMPLIANCE;
                        } else if ("getXmlValidation".equals(method.getName())) {
                            return Globals.STRICT_SERVLET_COMPLIANCE;
                        } else if ("getXmlBlockExternal".equals(method.getName())) {
                            return Globals.IS_SECURITY_ENABLED;
                        }
                        return null;
                    }
                }
            );
            final TldConfig config = new TldConfig();
            config.lifecycleEvent(new LifecycleEvent(fakeWebApp, Lifecycle.AFTER_INIT_EVENT, null));

            final Object fakeSc = Proxy.newProxyInstance(loader, new Class<?>[]{ServletContext.class}, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return null;
                }
            });
            final Object locationsCacheInstance = tldLocationsCache.getConstructor(ServletContext.class).newInstance(fakeSc);

            if (!SERVER_URLS.isEmpty()) {
                SERVER_SCANNING_THREAD = new Thread() {
                    @Override
                    public void run() {
                        for (URL current : SERVER_URLS) {
                            tldConfig(config, current);
                            tldLocationCache(locationsCacheInstance, current);
                        }
                    }
                };
                SERVER_SCANNING_THREAD.setName("TomEE-server-tld-reading");
                SERVER_SCANNING_THREAD.setDaemon(true);
                SERVER_SCANNING_THREAD.start();
            } else {
                SERVER_SCANNING_THREAD = null;
            }

            TAG_LIB_URIS = (Set<String>) Reflections.get(config, "taglibUris");
            LISTENERS = (ArrayList<String>) Reflections.get(config, "listeners");
            MAPPINGS = (Hashtable<String, Object>) Reflections.get(locationsCacheInstance, "mappings");
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public void scan(final ServletContext context, final ClassLoader classLoader, final JarScannerCallback callback, final Set<String> jarsToIgnore) {
        if ("FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
            final EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
        } else if ("TldJarScannerCallback".equals(callback.getClass().getSimpleName())) {

            final String cbName = callback.getClass().getName();
            if (cbName.equals(tldConfig.getDeclaringClass().getName())) {
                ensureServerTldsScanned();
                try {
                    final TldConfig config;
                    try {
                        config = (TldConfig) tldConfig.get(callback);
                    } catch (IllegalAccessException e) {
                        throw new OpenEJBException("scan with default algo");
                    }

                    final Set<URL> urls = TldScanner.scan(context.getClassLoader());
                    for (URL url : urls) {
                        if (!SERVER_URLS.contains(url)) {
                            tldConfig(config, url);
                        }
                    }

                    // add already scanned ones
                    for (String uri : TAG_LIB_URIS) {
                        config.addTaglibUri(uri);
                    }
                    for (String listener : LISTENERS) {
                        if (!"org.apache.myfaces.webapp.StartupServletContextListener".equals(listener)) { // done elsewhere
                            config.addApplicationListener(listener);
                        }
                    }

                    return; // done, next code is a fallback if scan() throw an exception
                } catch (OpenEJBException oe) {
                    // no-op
                }
            } else if (cbName.equals(tldLocationCache.getDeclaringClass().getName())) {
                ensureServerTldsScanned();
                try {
                    final Object tldLocationsCache;
                    try {
                        tldLocationsCache = tldLocationCache.get(callback);
                    } catch (IllegalAccessException e) {
                        throw new OpenEJBException("scan with default algo");
                    }

                    final Set<URL> urls = TldScanner.scan(context.getClassLoader());
                    for (URL url : urls) {
                        if (!SERVER_URLS.contains(url)) {
                            tldLocationCache(tldLocationsCache, url);
                        }
                    }

                    // add server ones
                    final Hashtable<String, Object> mappings = (Hashtable<String, Object>) Reflections.get(tldLocationsCache, "mappings");
                    mappings.putAll((Map<String, Object>) MAPPINGS.clone());

                    return;
                } catch (OpenEJBException oe) {
                    // no-op
                }
            } else {
                log.debug("This callback " + callback + " is not known and perf optim will not be available");
            }

            // Scan WEB-INF/lib
            final Set<String> dirList = context.getResourcePaths(Constants.WEB_INF_LIB);
            if (dirList != null) {
                for (final String path : dirList) {
                    if (path.endsWith(Constants.JAR_EXT) &&
                        !matchPath(DEFAULT_JARS_TO_SKIP,
                            path.substring(path.lastIndexOf('/') + 1))) {
                        // Need to scan this JAR
                        URL url = null;
                        try {
                            // File URLs are always faster to work with so use them
                            // if available.
                            final String realPath = context.getRealPath(path);
                            if (realPath == null) {
                                url = context.getResource(path);
                            } else {
                                url = (new File(realPath)).toURI().toURL();
                            }
                            this.process(callback, url);
                        } catch (IOException e) {
                            log.warn(sm.getString("jarScan.webinflibFail", url), e);
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace(sm.getString("jarScan.webinflibJarNoScan", path));
                        }
                    }
                }
            }

            // Scan the classpath
            if (this.isScanClassPath()) {
                if (log.isTraceEnabled()) {
                    log.trace(sm.getString("jarScan.classloaderStart"));
                }


                try {
                    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    final Set<URL> tldFileUrls = TldScanner.scan(loader);

                    final Set<URL> jarUlrs = this.discardFilePaths(tldFileUrls);

                    for (final URL url : jarUlrs) {
                        final String jarName = this.getJarName(url);

                        // Skip JARs known not to be interesting and JARs
                        // in WEB-INF/lib we have already scanned
                        if (jarName != null && !(matchPath(DEFAULT_JARS_TO_SKIP, jarName) || url.toString().contains(Constants.WEB_INF_LIB + jarName))) {

                            if (log.isDebugEnabled()) {
                                log.debug(sm.getString("jarScan.classloaderJarScan", url));
                            }
                            try {
                                this.process(callback, url);
                            } catch (IOException ioe) {
                                log.warn(sm.getString(
                                    "jarScan.classloaderFail", url), ioe);
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace(sm.getString("jarScan.classloaderJarNoScan", url));
                            }
                        }
                    }
                } catch (OpenEJBException e) {
                    log.warn("JarScan.TldScan Failed ", e);
                }
            }

        } else {
            super.scan(context, classLoader, callback, jarsToIgnore);
        }
    }

    private void ensureServerTldsScanned() {
        if (SERVER_SCANNING_THREAD == null) {
            return;
        }

        try {
            SERVER_SCANNING_THREAD.join();
        } catch (InterruptedException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private Set<URL> discardFilePaths(final Set<URL> tldFileUrls) {
        final Set<String> jarPaths = new HashSet<String>();

        for (final URL tldFileUrl : tldFileUrls) {
            jarPaths.add(URLs.toFilePath(tldFileUrl));
        }

        final Set<URL> jars = new HashSet<URL>();
        for (final String jarPath : jarPaths) {
            try {
                final URL url = new File(jarPath).toURI().toURL();
                jars.add(url);
            } catch (MalformedURLException e) {
                log.warn("Skipping JAR file " + jarPath, e);
            }
        }
        return jars;
    }

    private static void tldLocationCache(final Object tldLocationsCache, final URL url) {
        String resource = url.toString();
        String entry = null;

        if (resource.contains("!/")) {
            final String path = url.getPath();
            final int endIndex = path.indexOf("!/");
            resource = path.substring(0, endIndex);
            entry = path.substring(endIndex + 2, path.length());
        }

        InputStream is = null;
        try {
            is = url.openStream();
            tldLocationScanStream.invoke(tldLocationsCache, resource, entry, is);
        } catch (Exception e) {
            log.warn(sm.getString("jarScan.webinflibFail", url.toExternalForm()), e);
        } finally {
            IO.close(is);
        }
    }

    private static void tldConfig(final TldConfig config, final URL current) {
        InputStream is = null;
        try {
            is = current.openStream();
            final XmlErrorHandler handler = (XmlErrorHandler) tldConfigScanStream.invoke(config, is);
            handler.logFindings(log, current.toExternalForm());
        } catch (Exception e) {
            log.warn(sm.getString("jarScan.webinflibFail", current), e);
        } finally {
            IO.close(is);
        }
    }

    /*
    * Scan a URL for JARs with the optional extensions to look at all files
    * and all directories.
    */
    private void process(final JarScannerCallback callback, final URL url) throws IOException {
        final URLConnection conn = url.openConnection();
        if (conn instanceof JarURLConnection) {

            callback.scan((JarURLConnection) conn);

        } else {

            final String urlStr = url.toString();

            if (urlStr.startsWith("file:") || urlStr.startsWith("jndi:")) {

                if (urlStr.endsWith(Constants.JAR_EXT)) {

                    final URL jarURL = new URL("jar:" + urlStr + "!/");
                    callback.scan((JarURLConnection) jarURL.openConnection());

                } else {
                    try {

                        final File f = new File(url.toURI());

                        if (f.isFile() && this.isScanAllFiles()) {

                            // Treat this file as a JAR
                            final URL jarURL = new URL("jar:" + urlStr + "!/");
                            callback.scan((JarURLConnection) jarURL.openConnection());

                        } else if (f.isDirectory() && this.isScanAllDirectories()) {

                            final File metainf = new File(f.getAbsoluteFile() + File.separator + "META-INF");

                            if (metainf.isDirectory()) {
                                callback.scan(f);
                            }
                        }
                    } catch (URISyntaxException e) {
                        // Wrap the exception and re-throw
                        final IOException ioe = new IOException();
                        ioe.initCause(e);
                        throw ioe;
                    }
                }
            }
        }
    }

    /*
     * Extract the JAR name, if present, from a URL
     */
    private String getJarName(final URL url) {

        String name = null;

        final String path = url.getPath();
        final int end = path.indexOf(Constants.JAR_EXT);
        if (end != -1) {
            final int start = path.lastIndexOf('/', end);
            name = path.substring(start + 1, end + 4);
        } else if (this.isScanAllDirectories()) {
            final int start = path.lastIndexOf('/');
            name = path.substring(start + 1);
        }

        return name;
    }

    public static boolean matchPath(Set<String[]> patternSet, String str) {
        for (String[] patternTokens : patternSet) {
            if (matchPath(patternTokens, tokenizePathAsArray(str), true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchPath(String[] tokenizedPattern, String[] strDirs,
                                    boolean isCaseSensitive) {
        int patIdxStart = 0;
        int patIdxEnd = tokenizedPattern.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = tokenizedPattern[patIdxStart];
            if (patDir.equals(DEEP_TREE_MATCH)) {
                break;
            }
            if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    return false;
                }
            }
            return true;
        } else {
            if (patIdxStart > patIdxEnd) {
                // String not exhausted, but pattern is. Failure.
                return false;
            }
        }

        // up to last '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = tokenizedPattern[patIdxEnd];
            if (patDir.equals(DEEP_TREE_MATCH)) {
                break;
            }
            if (!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    return false;
                }
            }
            return true;
        }

        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = tokenizedPattern[patIdxStart + j + 1];
                    String subStr = strDirs[strIdxStart + i + j];
                    if (!match(subPat, subStr, isCaseSensitive)) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (!tokenizedPattern[i].equals(DEEP_TREE_MATCH)) {
                return false;
            }
        }

        return true;
    }

    public static boolean match(String pattern, String str,
                                boolean caseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;
        for (char aPatArr : patArr) {
            if (aPatArr == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (ch != '?') {
                    if (different(caseSensitive, ch, strArr[i])) {
                        return false; // Character mismatch
                    }
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while (true) {
            ch = patArr[patIdxStart];
            if (ch == '*' || strIdxStart > strIdxEnd) {
                break;
            }
            if (ch != '?') {
                if (different(caseSensitive, ch, strArr[strIdxStart])) {
                    return false; // Character mismatch
                }
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            return allStars(patArr, patIdxStart, patIdxEnd);
        }

        // Process characters after last star
        while (true) {
            ch = patArr[patIdxEnd];
            if (ch == '*' || strIdxStart > strIdxEnd) {
                break;
            }
            if (ch != '?') {
                if (different(caseSensitive, ch, strArr[strIdxEnd])) {
                    return false; // Character mismatch
                }
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            return allStars(patArr, patIdxStart, patIdxEnd);
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (ch != '?') {
                        if (different(caseSensitive, ch,
                            strArr[strIdxStart + i + j])) {
                            continue strLoop;
                        }
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        return allStars(patArr, patIdxStart, patIdxEnd);
    }

    private static boolean allStars(char[] chars, int start, int end) {
        for (int i = start; i <= end; ++i) {
            if (chars[i] != '*') {
                return false;
            }
        }
        return true;
    }

    private static boolean different(
        boolean caseSensitive, char ch, char other) {
        return caseSensitive
            ? ch != other
            : Character.toUpperCase(ch) != Character.toUpperCase(other);
    }

    public static String[] tokenizePathAsArray(String path) {
        if (log.isTraceEnabled()) {
            log.trace(sm.getString("matcher.tokenize", path));
        }
        String root = null;
        if (isAbsolutePath(path)) {
            String[] s = dissect(path);
            root = s[0];
            path = s[1];
        }
        char sep = File.separatorChar;
        int start = 0;
        int len = path.length();
        int count = 0;
        for (int pos = 0; pos < len; pos++) {
            if (path.charAt(pos) == sep) {
                if (pos != start) {
                    count++;
                }
                start = pos + 1;
            }
        }
        if (len != start) {
            count++;
        }
        String[] l = new String[count + ((root == null) ? 0 : 1)];

        if (root != null) {
            l[0] = root;
            count = 1;
        } else {
            count = 0;
        }
        start = 0;
        for (int pos = 0; pos < len; pos++) {
            if (path.charAt(pos) == sep) {
                if (pos != start) {
                    String tok = path.substring(start, pos);
                    l[count++] = tok;
                }
                start = pos + 1;
            }
        }
        if (len != start) {
            String tok = path.substring(start);
            l[count/*++*/] = tok;
        }
        return l;
    }

    private static String[] dissect(String path) {
        char sep = File.separatorChar;
        path = path.replace('/', sep).replace('\\', sep);

        String root;
        int colon = path.indexOf(':');
        if (colon > 0 && (ON_DOS || ON_NETWARE)) {

            int next = colon + 1;
            root = path.substring(0, next);
            char[] ca = path.toCharArray();
            root += sep;
            //remove the initial separator; the root has it.
            next = (ca[next] == sep) ? next + 1 : next;

            StringBuilder sbPath = new StringBuilder();
            // Eliminate consecutive slashes after the drive spec:
            for (int i = next; i < ca.length; i++) {
                if (ca[i] != sep || ca[i - 1] != sep) {
                    sbPath.append(ca[i]);
                }
            }
            path = sbPath.toString();
        } else if (path.length() > 1 && path.charAt(1) == sep) {
            // UNC drive
            int nextsep = path.indexOf(sep, 2);
            nextsep = path.indexOf(sep, nextsep + 1);
            root = (nextsep > 2) ? path.substring(0, nextsep + 1) : path;
            path = path.substring(root.length());
        } else {
            root = File.separator;
            path = path.substring(1);
        }
        return new String[]{root, path};
    }

    private static boolean isAbsolutePath(String filename) {
        int len = filename.length();
        if (len == 0) {
            return false;
        }
        char sep = File.separatorChar;
        filename = filename.replace('/', sep).replace('\\', sep);
        char c = filename.charAt(0);
        if (!(ON_DOS || ON_NETWARE)) {
            return (c == sep);
        }
        if (c == sep) {
            // CheckStyle:MagicNumber OFF
            if (!(ON_DOS && len > 4 && filename.charAt(1) == sep)) {
                return false;
            }
            // CheckStyle:MagicNumber ON
            int nextsep = filename.indexOf(sep, 2);
            return nextsep > 2 && nextsep + 1 < len;
        }
        int colon = filename.indexOf(':');
        return (Character.isLetter(c) && colon == 1
            && filename.length() > 2 && filename.charAt(2) == sep)
            || (ON_NETWARE && colon > 0);
    }

    /**
     * Determines if our OS is Netware.
     *
     * @return true if we run on Netware
     */
    private static boolean isNetware() {
        return OS_NAME.contains("netware");
    }

    /**
     * Determines if our OS is DOS.
     *
     * @return true if we run on DOS
     */
    private static boolean isDos() {
        return PATH_SEP.equals(";") && !isNetware();
    }
}
