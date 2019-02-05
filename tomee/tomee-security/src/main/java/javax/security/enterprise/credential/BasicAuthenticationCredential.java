/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.security.enterprise.credential;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class BasicAuthenticationCredential extends UsernamePasswordCredential {

    public BasicAuthenticationCredential(String authorizationHeader) {
        super(parseUsername(authorizationHeader), parsePassword(authorizationHeader));
    }

    private static String decodeHeader(String authorizationHeader) {
        final String basicAuthCharset = "US-ASCII";

        if (null == authorizationHeader) {
            throw new NullPointerException("authorization header");
        }

        if (authorizationHeader.isEmpty()) {
            throw new IllegalArgumentException("authorization header is empty");
        }

        final Base64.Decoder decoder = Base64.getMimeDecoder();
        byte[] decodedBytes = decoder.decode(authorizationHeader);
        try {
            return new String(decodedBytes, basicAuthCharset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unknown Charset: " + basicAuthCharset, e);
        }
    }

    private static String parseUsername(String authorizationHeader) {
        String decodedAuthorizationHeader = decodeHeader(authorizationHeader);
        int delimiterIndex = decodedAuthorizationHeader.indexOf(':');
        if (delimiterIndex > -1) {
            return decodedAuthorizationHeader.substring(0, delimiterIndex);
        } else {
            return decodedAuthorizationHeader;
        }
    }

    private static Password parsePassword(String authorizationHeader) {
        String decodedAuthorizationHeader = decodeHeader(authorizationHeader);
        int delimiterIndex = decodedAuthorizationHeader.indexOf(':');
        if (delimiterIndex > -1) {
            return new Password(decodedAuthorizationHeader.substring(delimiterIndex + 1));
        } else {
            return new Password("");
        }
    }
}
