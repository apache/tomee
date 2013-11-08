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
package org.apache.openjpa.lib.util.svn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SVNUtils {
    static final Pattern fullRevisionPattern = Pattern.compile("(([0-9]*:)?)[0-9]+(([MSms]+)?)");
    static final Pattern revisionPattern = Pattern.compile("[0-9]+");

    /**
     * A public worker method that takes the output from running the svnversion command and parses the trailing integer
     * version out.
     * 
     * For example: 959691:959709M would return 959709
     * 
     * @param svninfo
     * @return The formatted int version, or -1 if svninfo is null or unparsable.
     */
    public static int svnInfoToInteger(String svninfo) {
        if (svninfo == null || fullRevisionPattern.matcher(svninfo).matches() == false) {
            return -1;
        }
        // We only want to look after ":"
        int index = svninfo.indexOf(":");
        if(index != -1){
            svninfo = svninfo.substring(index+1);
        }

        Matcher matcher = revisionPattern.matcher(svninfo);
        if(matcher.find()){
            return Integer.parseInt(matcher.group());
        }

        return -1;
    }
}
