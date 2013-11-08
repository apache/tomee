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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import openbook.tools.CommandProcessor;
import openbook.tools.parser.JavaLexer;
import openbook.tools.parser.JavaParser;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.debug.DebugEventListener;

/**
 * Renders Java Source Code.
 * 
 * @author Pinaki Poddar
 * 
 */
public abstract class SourceRenderer {
    public final static char FORWARD_SLASH = '/'; 
    public final static String DOT         = "."; 
    public final static String CURRENT_DIRECTORY = "."; 
    
    private File _sourceDir, _destinationDir;
    
    boolean verbose = false;
    /**
     * Renders the given source files. The syntax of the command is
     * <br>
     * <pre>  $ java SourceRenderer [options] files </pre>
     * For example,
     * <pre> $ java SourceRenderer -stylesheet mystyle.css -sourcepath test
     *  -d generated/html acme/foo/SomeClass.java</pre>
     * <p>
     * 
     * Recognized options are<br>
     * <table>
     * <TR><TD><pre>-format</pre></TD> <TD>the format of the converted output. Recognized monikers are 
     * <code>html</code> and <code>text</code>. A fully qualified class name that implements {@link TokenRenderer} is
     * allowed. Default is <code>html</code></TD></tr> 
     * <TR><TD><pre>-sourcepath</pre><TD>the root of the source files. Default is the current directory</tr> 
     * <TR><TD><pre>-d</pre><TD>the root of the generated files. Default is the current directory</tr> 
     * </table>
     * Besides these options, a renderer can accept more options. Any option <code>-someProperty</code>
     * will configure the renderer if a bean-style setter method <code>setSomeProperty(String|boolean|int)</code>
     * is available. See available documentation on the specific {@link HTMLTokenRenderer renderer}.
     * <br>  
     * Stylesheet file must be under destination directory.
     * <pre>-stylesheet</pre> is relative to destination directory.
     * 
     * @param args command-line arguments.
     * 
     * @throws Exception
     */
    
    protected final void run(String[] args) throws Exception {
        CommandProcessor options = new CommandProcessor();
        options.register(true, "-sourcepath").setDefault(CURRENT_DIRECTORY);
        options.register(true, "-d").setDefault(CURRENT_DIRECTORY);
        options.register(true, "-extension").setDefault("");
        options.register(true, "-verbose").setDefault("false");
        registerOptions(options);
        
        String[] inputs = options.setFrom(args); 
        
        _sourceDir  = new File(options.getValue("-sourcepath"));
        _destinationDir = new File(options.getValue("-d"));
        verbose = "true".equalsIgnoreCase(options.getValue("-verbose"));
        for (String path : inputs) {
            path = path.replace(File.separatorChar, FORWARD_SLASH);
            InputStream fin = getInputStream(_sourceDir, path);
            if (fin == null) {
                continue;
            }
            
            File outFile = new File(_destinationDir, suffix(path, options.getValue("-extension")));
            FileOutputStream fout = createOutput(outFile);
            if (fout == null) {
                continue;
            }
            PrintStream out = new PrintStream(fout);

            TokenRenderer renderer = createRenderer(options, outFile);
            if (renderer == null) {
                continue;
            }
            out.print(renderer.getPrologue());
            render(fin, renderer, out);
            out.println(renderer.getEpilogue());
            out.close();
        }
    }
    
    public File getDestinationDirectory() {
        return _destinationDir;
    }
    public File getSourceDirectory() {
        return _sourceDir;
    }
    public abstract void registerOptions(CommandProcessor options);
    public abstract TokenRenderer createRenderer(CommandProcessor options, File outFile);
    
    private void render(InputStream is, TokenRenderer renderer, PrintStream out) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(is);
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DebugEventListener builder = new ParseTokenListener(renderer, out);
        JavaParser parser = new JavaParser(tokens, builder);
        // launch the parser starting at compilation unit
        parser.compilationUnit();
    }
    
    /**
     * Gets the input stream.
     * 
     * @param srcDir the root source directory.
     * @param path the path to input file.
     * @return
     */
    protected InputStream getInputStream(File srcDir, String path) {
        File file = new File(srcDir, path); 
        if (!file.exists()) {
            warn("Input file " + file.getAbsolutePath() + " does not exist");
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (Exception e) {
            warn("Input file " + file.getAbsolutePath() + " failed due to " + e.toString());
        }
        return null;
    }
    
    /**
     * Gets the output stream to write to.
     * @param file
     * @return
     */
    private FileOutputStream createOutput(File file)  {
        try {
            file.getParentFile().mkdirs();
            if (!file.exists())
                file.createNewFile();
            file.setWritable(true);
            FileOutputStream out = new FileOutputStream(file);
            verbose("Output " + file.toURI());
            return out;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    private String suffix(String s, String suffix) {
        if (suffix == null || suffix.isEmpty())
            return s;
        if (suffix.startsWith(".")) 
            return s + suffix;
        return s + "." + suffix;
    }
    
    protected void verbose(String s) {
        if (verbose)
            System.err.println(s);
    }
    
    protected void warn(String s) {
        System.err.println(s);
    }
}
