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
package openbook.tools.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;
import java.util.regex.Pattern;

public class URIUtils {
    public static URI resolve(final URI baseURI, final String reference) {
        return URIUtils.resolve(baseURI, URI.create(reference));
    }

    /**
     * Resolves a URI reference against a base URI. 
     * 
     * @param baseURI the base URI
     * @param reference the URI reference
     * @return the resulting URI
     */
    public static URI resolve(final URI baseURI, URI reference) {
        if (baseURI == null) {
            throw new IllegalArgumentException("Base URI may nor be null");
        }
        if (reference == null) {
            throw new IllegalArgumentException("Reference URI may nor be null");
        }
        String s = reference.toString();
        boolean emptyReference = s.length() == 0;
        if (emptyReference) {
            reference = URI.create("#");
        }
        URI resolved = baseURI.resolve(reference);
        if (emptyReference) {
            String resolvedString = resolved.toString();
            resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf('#')));
        }
        return removeDotSegments(resolved);
    }

    /**
     * Removes dot segments according to RFC 3986, section 5.2.4
     * 
     * @param uri
     *            the original URI
     * @return the URI without dot segments
     */
    private static URI removeDotSegments(URI uri) {
        String path = uri.getPath();
        if ((path == null) || (path.indexOf("/.") == -1)) {
            // No dot segments to remove
            return uri;
        }
        String[] inputSegments = path.split("/");
        Stack<String> outputSegments = new Stack<String>();
        for (int i = 0; i < inputSegments.length; i++) {
            if ((inputSegments[i].length() == 0) || (".".equals(inputSegments[i]))) {
                // Do nothing
            } else if ("..".equals(inputSegments[i])) {
                if (!outputSegments.isEmpty()) {
                    outputSegments.pop();
                }
            } else {
                outputSegments.push(inputSegments[i]);
            }
        }
        StringBuilder outputBuffer = new StringBuilder();
        for (String outputSegment : outputSegments) {
            outputBuffer.append('/').append(outputSegment);
        }
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), outputBuffer.toString(), uri.getQuery(), uri
                    .getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {

        File f = new File(targetPath);
        boolean isDir = f.isDirectory();
        //  We need the -1 argument to split to make sure we get a trailing 
        //  "" token if the base ends in the path separator and is therefore
        //  a directory. We require directory paths to end in the path
        //  separator -- otherwise they are indistinguishable from files.
        String[] base = basePath.split(Pattern.quote(pathSeparator), -1);
        String[] target = targetPath.split(Pattern.quote(pathSeparator), 0);

        //  First get all the common elements. Store them as a string,
        //  and also count how many of them there are. 
        String common = "";
        int commonIndex = 0;
        for (int i = 0; i < target.length && i < base.length; i++) {
            if (target[i].equals(base[i])) {
                common += target[i] + pathSeparator;
                commonIndex++;
            }
            else break;
        }

        if (commonIndex == 0) {
            //  not even a single common path element. This most
            //  likely indicates differing drive letters, like C: and D:. 
            //  These paths cannot be relativized. Return the target path.
            return targetPath;
        }

        String relative = "";
        if (base.length == commonIndex) {
            //  Comment this out if you prefer that a relative path not start with ./
            relative = "." + pathSeparator;
        }  else {
            int numDirsUp = base.length - commonIndex - (isDir ? 0 : 1); /* only subtract 1 if it  is a file. */
            //  The number of directories we have to backtrack is the length of 
            //  the base path MINUS the number of common path elements, minus
            //  one because the last element in the path isn't a directory.
            for (int i = 1; i <= (numDirsUp); i++) {
                relative += ".." + pathSeparator;
            }
        }
        //if we are comparing directories 
        if (targetPath.length() > common.length()) {
           //it's OK, it isn't a directory
           relative += targetPath.substring(common.length());
        }

        return relative;
    }

}
