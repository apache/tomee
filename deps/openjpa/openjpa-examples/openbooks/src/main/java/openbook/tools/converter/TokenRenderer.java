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

import org.antlr.runtime.Token;
import org.antlr.runtime.debug.DebugEventListener;

/**
 * Renders a runtime ANTLR token.
 * {@linkplain ParseTokenListener} controls a renderer and streams the rendered Strings 
 * to appropriate output. 
 * <br>
 * A renderer can define additional bean-style setter methods. They will be set before
 * renderer is used.
 * <br>
 * A concrete renderer should have a no argument constructor.  
 * <br>
 * {@linkplain ParseTokenListener} or the <em>framework</em> calls a renderer in following sequence:
 * <pre>
 *    bean-style setter methods as per the user configured 
 *    getPrologue()
 *    for each token {
 *       if (line is changing) {
 *          endLine()
 *          newLine()
 *       }
 *       render()
 *    }
 *    getEpilogue();
 * </pre>

 * @author Pinaki Poddar
 *
 */
public interface TokenRenderer {
    /**
     * Gets a string to be added before token processing begins.
     * <br>
     * For example, a HTML renderer may return the opening HTML and BODY tags.
     * A HTML render using a Cascaded Style Sheet may additionally specify
     * the <HEAD> tag to include the style sheet.
     */
    public String getPrologue();
    
    /**
     * Gets a string to be added after token processing ends.
     * For example, a HTML renderer may return the closing HTML and BODY tags.
     * 
     */
    public String getEpilogue();
    
    /**
     * Produce a string representation of the given token.
     * 
     * @param decision the index of the decision (or the context) in which
     * the current token is being processed. The index refers to ANTLR
     * {@link DebugEventListener#enterDecision(int)}.
     * 
     * @param token the token to be rendered. Can be a hidden token as well.
     * 
     * @return a string representation of the given token.
     */
    public String render(int decision, Token token);
    
    /**
     * Produce a string to signal beginning of a line.
     * <br>
     * For example, a renderer printing line numbers can produce a String with the given line number.
     * 
     * @param line the current line number 
     * 
     * @return a String can be a blank
     */
    public String newLine(int line);
    
    
    /**
     * Produce a string to signal end of a line.
     * <br>
     * For example, a renderer can produce a newline.
     * 
     * @param line the line being ended 
     * 
     * @return a String can be a blank
     */
    public String endLine(int line); 
}
