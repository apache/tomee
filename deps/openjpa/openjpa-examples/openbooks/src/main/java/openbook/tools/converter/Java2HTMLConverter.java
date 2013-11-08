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
package openbook.tools.converter;

import java.io.File;

import openbook.tools.CommandProcessor;
import openbook.tools.util.URIUtils;

public class Java2HTMLConverter extends SourceRenderer {
    
    private final HTMLTokenRenderer _renderer = new HTMLTokenRenderer();
    
      public static void main(String[] args) throws Exception {
          new Java2HTMLConverter().run(args);
      }
      
    @Override
    public void registerOptions(CommandProcessor options) {
        options.register(true, "-stylesheet").setDefault("java.css");
        options.register(true, "-addLineBreak").setDefault("true");
        options.register(true, "-addExplicitSpace").setDefault("true");
        options.register(true, "-anchorLineNumber").setDefault("false");
        options.register(true, "-showLineNumber").setDefault("true");
        options.register(true, "-lineNumberFormat").setDefault("%%0%4d");
    }
    
    @Override
    public TokenRenderer createRenderer(CommandProcessor options, File outFile) {
        File styleFile = null;
        styleFile = new File(getDestinationDirectory(), options.getValue("-stylesheet"));
        if (!styleFile.exists()) {
            throw new IllegalArgumentException("Stylesheet file " + styleFile.getAbsolutePath() +
                    " does not exist. Ensure that the file is available under destination directory" + 
                    getDestinationDirectory().getAbsolutePath());
        }
        _renderer.setStylesheet(getRelativeStylesheet(styleFile, outFile));
        
        _renderer.setAddLineBreak("true".equals(options.getValue("-addLineBreak")));
        _renderer.setAddExplicitSpace("true".equals(options.getValue("-addExplicitSpace")));
        _renderer.setAnchorLineNumber("true".equals(options.getValue("-anchorLineNumber")));
        _renderer.setLineNumberFormat(options.getValue("-lineNumberFormat"));
        _renderer.setShowLineNumber("true".equals(options.getValue("-showLineNumber")));
        return _renderer;
    }
    
    
    String getRelativeStylesheet(File styleFile,  File outFile) {
            String stylesheetPath = styleFile.getAbsolutePath().replace(File.separatorChar, FORWARD_SLASH);
            String outPath = outFile.getAbsolutePath().replace(File.separatorChar, FORWARD_SLASH);
            verbose("stylesheet " + stylesheetPath);
            verbose("output     " + outPath);
            String rstylesheet = URIUtils.getRelativePath(stylesheetPath, outPath, ""+FORWARD_SLASH);
            verbose("stylesheet relative " + rstylesheet);
            return rstylesheet;
    }
}
