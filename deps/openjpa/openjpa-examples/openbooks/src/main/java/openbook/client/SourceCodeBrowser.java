/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package openbook.client;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import jpa.tools.swing.SourceCodeViewer;

/**
 * Browses source code.
 * The source code URI is resolved with respect to a root URI.
 * The source code is expected to be in HTML format.
 * The actual rendering of the source code can be either through an 
 * {@linkplain SourceCodeViewer internal} or an external browser. 
 * 
 * @author Pinaki Poddar
 *
 */
public class SourceCodeBrowser {
    private boolean _useExternal;
    private SourceCodeViewer _internal = new SourceCodeViewer();
    private URI _rootURI;
    private File _rootDir;
    
    /**
     * Construct a browser.
     * 
     * @param root a path to be resolved as an URI to root of source tree.
     * @param useDesktop flags to use external or internal browser.
     */
    public SourceCodeBrowser(String root, boolean useDesktop) {
        _rootURI = convertToURI(root);
        _useExternal = useDesktop;
    }
    
    public URI getRootURI() {
        return _rootURI;
    }
    
    /**
     * Gets the root source directory if the sources are being serverd from a local
     * file system directory.
     */
    public File getRootDirectory() {
        return _rootDir;
    }
    /**
     * Shows the given page.
     * @param key key a user visible moniker for the page.
     * @param page the path of the page content w.r.t the root URI of this browser.
     */
    public void showPage(String key, String page) {
        showPage(key, URI.create(_rootURI.toString() + page));
    }
    
    public void addPage(String key, String path) {
        _internal.addPage(key, URI.create(_rootURI.toString() + path));
    }
    
    public SourceCodeViewer getViewer() {
        return _internal;
    }
    /**
     * Shows the given page.
     * 
     * @param key a user visible moniker for the page.
     * @param uri the URI of the page content.
     */
    public void showPage(String key, URI uri) {
        System.err.println("Going to show [" + uri + "] for anchor " + key);
        try {
            if (_useExternal) {
                Desktop.getDesktop().browse(uri);
            } else {
                _internal.showPage(key, uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private URI convertToURI(String srcPath) {
        try {
            URI uri = URI.create(srcPath);
            String scheme = uri.getScheme();
            if (scheme != null && !"file".equals(scheme)) {
                return uri;
            }
        } catch (IllegalArgumentException e) {
            // we have a relative path. Resolve it against current directory
        }
        File srcDir = new File(new File("."), srcPath);
        if (!srcDir.exists()) {
            throw new RuntimeException(srcDir.getAbsolutePath() + " does not exist." +
                    "The source root must be relative to current dir");
        }
        if (!srcDir.isDirectory()) {
            throw new RuntimeException(srcDir.getAbsolutePath() + " is not a directory");
        }
        _rootDir = srcDir;
        return convertForWindows(_rootDir.toURI());
    }
    
    URI convertForWindows(URI uri) {
        String os = System.getProperty("os.name");
        boolean windows = os.toLowerCase().indexOf("windows") != -1;
        return URI.create(uri.getScheme() + (windows ? "://" : "") + uri.getRawSchemeSpecificPart());
    }
}
