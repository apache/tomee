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

import java.io.PrintStream;
import java.util.StringTokenizer;

import openbook.tools.parser.JavaParser;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.BlankDebugEventListener;

/**
 * A token processor to render the ANTLR tokens.
 * 
 * This token processor is notified as ANTLR parses every token.
 * This processor controls a {@linkplain TokenRenderer renderer} 
 * that renders the token.
 *  
 * @author Pinaki Poddar
 *
 */
public class ParseTokenListener extends BlankDebugEventListener {
    private int currentLine  = 0;
    private int backtracking = 0;
    private int decision;
    private TokenRenderer _renderer;
    private PrintStream _stream;
    
    /**
     * By default, uses a {@linkplain PlainTokenRenderer}.
     */
    ParseTokenListener() {
        this(new PlainTokenRenderer());
    }
    
    /**
     * Uses the given renderer and outputs to System output.
     * 
     * @param renderer a renderer to render the tokens.
     */
    ParseTokenListener(TokenRenderer renderer) {
        this(renderer, System.out);
    }
    
    /**
     * Uses the given renderer and given outputs stream.
     * 
     * @param renderer a renderer to render the tokens.
     * @param stream a output stream where the rendered strings are streamed.
     */
    ParseTokenListener(TokenRenderer renderer, PrintStream stream) {
        _renderer = renderer;
        _stream = stream;
    }
    
    @Override
    public void enterDecision(int d) {
        backtracking += 1; 
        decision = d;
    } 
    
    @Override
    public void exitDecision(int i) { 
         backtracking -= 1; 
    }
    
    /**
     * A regular token is delegated to the renderer for a string representation
     * and the resultant string is sent to the output stream.
     */
    @Override
    public void consumeToken(Token token) {
        if (backtracking > 0) return;
        changeLine(token.getLine());
        _stream.print(_renderer.render(decision, token));
    }
    
    /**
     * Hidden tokens are tokens that are not processed at lexical processing
     * stage. The  most important hidden token for rendering are the tokens
     * that represent multi-line or single line comments. The multi-line
     * comments must be broken into individual lines for line numbering to
     * remain consistent.
     *  
     */
    @Override
    public void consumeHiddenToken(Token token) {
        if (this.backtracking > 0 && currentLine != 0) return;
        int type = token.getType();
        if (type == JavaParser.COMMENT || type == JavaParser.LINE_COMMENT) {
            StringTokenizer linebreaker = new StringTokenizer(token.getText(), "\r\n", false);
            int i = 0;
            while (linebreaker.hasMoreTokens()) {
                Token dummy = new CommonToken(JavaParser.COMMENT, linebreaker.nextToken());
                changeLine(token.getLine() + i);
                _stream.print(_renderer.render(decision, dummy));
                i++;
            }
        } else {
            changeLine(token.getLine()); 
            _stream.print(_renderer.render(decision, token));
        }
    }
    
    /**
     * If the given line is different than the current line, then asks the
     * renderer to end the current line and start a new line. Otherwise, does nothing.
     * 
     * @param newline
     */
    void changeLine(int newline) {
        if (newline == currentLine)
            return;
        _stream.print(_renderer.endLine(currentLine));
        _stream.print(_renderer.newLine(newline));
        currentLine = newline;
    }
}
