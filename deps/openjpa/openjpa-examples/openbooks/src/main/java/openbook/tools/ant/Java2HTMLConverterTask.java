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
package openbook.tools.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import openbook.tools.converter.Java2HTMLConverter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * An ant task to run {@linkplain Java2HTMLConverter Java2HTML Converter}.
 * 
 * @author Pinaki Poddar
 *
 */
public class Java2HTMLConverterTask extends MatchingTask {
    private String sourcepath        = ".";
    private String destDir           = ".";
    private String extension         = "";
    private String stylesheet        = "java.css";
    private boolean showLineNumber   = true;
    private boolean anchorLineNumber = false;
    private boolean addLineBreak     = true;
    private boolean addExplicitSpace = true;
    private String lineNumberFormat  = "%04d";
    private boolean verbose          = false;
    
    private List<Arg> _args = new ArrayList<Arg>();
    
    public String getSourcepath() {
        return sourcepath;
    }

    public void setSourcepath(String sourcepath) {
        this.sourcepath = sourcepath;
    }
    
    @Override
    public void execute() throws BuildException {
        List<String> files = getFiles();
        List<String> args  = new ArrayList<String>();
        args.add("-sourcepath");       args.add(sourcepath);
        args.add("-d");                args.add(destDir);
        args.add("-extension");        args.add(extension);
        args.add("-verbose");          args.add(""+verbose);
        args.add("-stylesheet");       args.add(stylesheet);
        args.add("-showLineNumber");   args.add(""+showLineNumber);
        args.add("-anchorLineNumber"); args.add(""+anchorLineNumber);
        args.add("-addLineBreak");     args.add(""+addLineBreak);
        args.add("-addExplicitSpace"); args.add(""+addExplicitSpace);
        args.add("-lineNumberFormat"); args.add(lineNumberFormat);
        args.add("-anchorLineNumber"); args.add(""+anchorLineNumber);
        
        for (Arg a : _args) {
            args.add(a.getName());
            args.add(a.getValue());
        }
        for (String file : files) {
            args.add(file.replace(File.separatorChar, '/'));
        }
        try {
            Java2HTMLConverter.main(args.toArray(new String[args.size()]));
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
    
    List<String> getFiles() {
        List<String> list = new ArrayList<String>();
        DirectoryScanner scanner = getDirectoryScanner(new File(getSourcepath()));
        String[] files = scanner.getIncludedFiles();
        for (String file : files) {
            if (file.endsWith(".java")) {
                list.add(file);
            }
        }
        return list;
    }
    
    public Arg createArg() {
        Arg arg = new Arg();
        _args.add(arg);
        return arg;
    }
    

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * @return the stylesheet
     */
    public String getStylesheet() {
        return stylesheet;
    }

    /**
     * @param stylesheet the stylesheet to set
     */
    public void setStylesheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    /**
     * @return the showLineNumber
     */
    public boolean isShowLineNumber() {
        return showLineNumber;
    }

    /**
     * @param showLineNumber the showLineNumber to set
     */
    public void setShowLineNumber(boolean showLineNumber) {
        this.showLineNumber = showLineNumber;
    }

    /**
     * @return the anchorLineNumber
     */
    public boolean isAnchorLineNumber() {
        return anchorLineNumber;
    }

    /**
     * @param anchorLineNumber the anchorLineNumber to set
     */
    public void setAnchorLineNumber(boolean anchorLineNumber) {
        this.anchorLineNumber = anchorLineNumber;
    }

    /**
     * @return the addLineBreak
     */
    public boolean isAddLineBreak() {
        return addLineBreak;
    }

    /**
     * @param addLineBreak the addLineBreak to set
     */
    public void setAddLineBreak(boolean addLineBreak) {
        this.addLineBreak = addLineBreak;
    }

    /**
     * @return the addExplicitSpace
     */
    public boolean isAddExplicitSpace() {
        return addExplicitSpace;
    }

    /**
     * @param addExplicitSpace the addExplicitSpace to set
     */
    public void setAddExplicitSpace(boolean addExplicitSpace) {
        this.addExplicitSpace = addExplicitSpace;
    }

    /**
     * @return the lineNumberFormat
     */
    public String getLineNumberFormat() {
        return lineNumberFormat;
    }

    /**
     * @param lineNumberFormat the lineNumberFormat to set
     */
    public void setLineNumberFormat(String lineNumberFormat) {
        this.lineNumberFormat = lineNumberFormat;
    }
    
    public static class Arg {
        String name;
        String value;
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
