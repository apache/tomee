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
package org.apache.openjpa.kernel.jpql;

import java.util.TreeSet;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.util.UserException;

/**
 * Signifies that a lexical error occurred when parsing the JPQL statement.
 *
 * @author Marc Prud'hommeaux
 */
public class ParseException
    extends UserException {

    private static final Localizer _loc =
        Localizer.forPackage(ParseException.class);

    /**
     * This constructor is used by the method "generateParseException"
     * in the generated parser. Calling this constructor generates
     * a new object of this type with the fields "currentToken",
     * "expectedTokenSequences", and "tokenImage" set.
     * This constructor calls its super class with the empty string
     * to force the "toString" method of parent class "Throwable" to
     * print the error message in the form:
     * ParseException: <result of getMessage>
     */
    public ParseException(Token currentTokenVal,
        int[][]expectedTokenSequencesVal, String[]tokenImageVal) {
        super(initMessage(currentTokenVal,
            expectedTokenSequencesVal, tokenImageVal));
    }

    /**
     * The following constructors are for use by you for whatever
     * purpose you can think of. Constructing the exception in this
     * manner makes the exception behave in the normal way - i.e., as
     * documented in the class "Throwable". The fields "errorToken",
     * "expectedTokenSequences", and "tokenImage" do not contain
     * relevant information. The JavaCC generated code does not use
     * these constructors.
     */
    public ParseException() {
        super();
    }

    /**
     * String constructor. Constructing the exception in this
     * manner makes the exception behave in the normal way - i.e., as
     * documented in the class "Throwable". The fields "errorToken",
     * "expectedTokenSequences", and "tokenImage" do not contain
     * relevant information. The JavaCC generated code does not use
     * these constructors.
     */
    public ParseException(String message) {
        super(message);
    }
    
    public ParseException(String message, Throwable t) {
    	super(message, t);
    }

    /**
     * This method has the standard behavior when this object has been
     * created using the standard constructors. Otherwise, it uses
     * "currentToken" and "expectedTokenSequences" to generate a parse
     * error message and returns it. If this object has been created
     * due to a parse error, and you do not catch it (it gets thrown
     * from the parser), then this method is called during the printing
     * of the final stack trace, and hence the correct error message
     * gets displayed.
     */
    private static Message initMessage(Token currentToken,
        int[][]expectedTokenSequences, String[]tokenImage) {
        TreeSet expected = new TreeSet();

        int maxSize = 0;

        for (int i = 0; i < expectedTokenSequences.length; i++) {
            if (maxSize < expectedTokenSequences[i].length)
                maxSize = expectedTokenSequences[i].length;

            for (int j = 0; j < expectedTokenSequences[i].length; j++)
                expected.add(tokenImage[expectedTokenSequences[i][j]]);
        }

        Token tok = currentToken.next;

        StringBuffer curtokBuf = new StringBuffer();
        for (int i = 0; i < maxSize; i++) {
            if (i != 0)
                curtokBuf.append(" ");
            if (tok.kind == 0) {
                curtokBuf.append(tokenImage[0]);
                break;
            }

            curtokBuf.append(escape(tok.image));
            tok = tok.next;
        }
        String curtok = curtokBuf.toString();

        return _loc.get("bad-parse", new Object[]{ curtok,
            Integer.valueOf(currentToken.next.beginColumn), expected });
    }

    /**
     * Used to convert raw characters to their escaped version
     * when these raw version cannot be used as part of an ASCII string literal.
     */
    private static String escape(String str) {
        StringBuffer retval = new StringBuffer();
        char ch;

        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case 0:
                    continue;
                case '\b':
                    retval.append("\\b");
                    continue;
                case '\t':
                    retval.append("\\t");
                    continue;
                case '\n':
                    retval.append("\\n");
                    continue;
                case '\f':
                    retval.append("\\f");
                    continue;
                case '\r':
                    retval.append("\\r");
                    continue;
                case '\"':
                    retval.append("\\\"");
                    continue;
                case '\'':
                    retval.append("\\\'");
                    continue;
                case '\\':
                    retval.append("\\\\");
                    continue;
                default:
                    if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = "0000" + Integer.toString(ch, 16);

                        retval.append("\\u" + s.substring(s.length() - 4,
                            s.length()));
                    } else {
                        retval.append(ch);
                    }
                    continue;
            }
        }
        return retval.toString();
	}

}
