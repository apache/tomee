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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder;

import junit.framework.TestCase;

import java.net.URL;
import java.util.List;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class UrlSetTest extends TestCase {
    private UrlSet urlSet;
    private URL[] originalUrls;

    protected void setUp() throws Exception {
        originalUrls = new URL[]{
                new URL("file:/Users/dblevins/work/xbean/trunk/xbean-finder/target/classes/"),
                new URL("file:/Users/dblevins/work/xbean/trunk/xbean-finder/target/test-classes/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/.compatibility/14compatibility.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/charsets.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/classes.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/dt.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/jce.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/jconsole.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/jsse.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/laf.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/ui.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/deploy.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/ext/apple_provider.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/ext/dnsns.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/ext/localedata.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/ext/sunjce_provider.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/ext/sunpkcs11.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/plugin.jar!/"),
                new URL("jar:file:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/lib/sa-jdi.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/CoreAudio.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/MRJToolkit.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/QTJSupport.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/QTJava.zip!/"),
                new URL("jar:file:/System/Library/Java/Extensions/dns_sd.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/j3daudio.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/j3dcore.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/j3dutils.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/jai_codec.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/jai_core.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/mlibwrapper_jai.jar!/"),
                new URL("jar:file:/System/Library/Java/Extensions/vecmath.jar!/"),
                new URL("jar:file:/Users/dblevins/.m2/repository/junit/junit/3.8.1/junit-3.8.1.jar!/"),
        };
        urlSet = new UrlSet(originalUrls);
    }

    public void testAll() throws Exception {

        assertEquals("Urls.size()", 32, urlSet.getUrls().size());

        UrlSet homeSet = urlSet.matching(".*Home.*");
        assertEquals("HomeSet.getUrls().size()", 8, homeSet.getUrls().size());

//        homeSet = urlSet.relative(new File("/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home"));
//        assertEquals("HomeSet.getUrls().size()", 8, homeSet.getUrls().size());

        UrlSet urlSet2 = urlSet.exclude(homeSet);
        assertEquals("Urls.size()", 24, urlSet2.getUrls().size());

        UrlSet xbeanSet = urlSet.matching(".*xbean.*");
        assertEquals("XbeanSet.getUrls().size()", 2, xbeanSet.getUrls().size());

        UrlSet junitSet = urlSet.matching(".*junit.*");
        assertEquals("JunitSet.getUrls().size()", 1, junitSet.getUrls().size());

        UrlSet mergedSet = homeSet.include(xbeanSet);
        assertEquals("MergedSet.getUrls().size()", 10, mergedSet.getUrls().size());

        mergedSet.include(junitSet);
        assertEquals("MergedSet.getUrls().size()", 10, mergedSet.getUrls().size());

        UrlSet mergedSet2 = mergedSet.include(junitSet);
        assertEquals("MergedSet2.getUrls().size()", 11, mergedSet2.getUrls().size());

        UrlSet filteredSet = urlSet.exclude(".*System/Library.*");
        assertEquals("FilteredSet.getUrls().size()", 3, filteredSet.getUrls().size());

        filteredSet.exclude(junitSet);
        assertEquals("FilteredSet.getUrls().size()", 3, filteredSet.getUrls().size());

        UrlSet filteredSet2 = filteredSet.exclude(junitSet);
        assertEquals("FilteredSet2.getUrls().size()", 2, filteredSet2.getUrls().size());
    }
}
