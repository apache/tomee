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
/**
 * A default implementation of {@linkplain TokenRenderer} that simply prints the token.
 * 
 * @author Pinaki Poddar
 *
 */
public class PlainTokenRenderer implements TokenRenderer {
    private static final String EMPTY = "";
    private boolean showLineNumber = true;
    private String lineNumberFormat  = "%04d";
    
    public String endLine(int line) {
        return EMPTY;
    }

    public String newLine(int line) {
        return showLineNumber ? String.format(lineNumberFormat, line) : EMPTY;
    }

    public String render(int decision, Token token) {
        return token.getText();
    }

    public String getEpilogue() {
        return EMPTY;
    }

    public String getPrologue() {
        return EMPTY;
    }

    public boolean getShowLineNumber() {
        return showLineNumber;
    }

    public void setShowLineNumber(boolean showLineNumber) {
        this.showLineNumber = showLineNumber;
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
}
