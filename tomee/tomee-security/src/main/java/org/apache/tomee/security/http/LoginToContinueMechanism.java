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
package org.apache.tomee.security.http;

import org.apache.tomcat.util.buf.ByteChunk;

import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

public interface LoginToContinueMechanism {
    int MAX_SAVE_POST_SIZE = 4 * 1024;

    String ORIGINAL_REQUEST = "org.apache.tomee.security.request.original";
    String AUTHENTICATION = "org.apache.tomee.security.request.authentication";
    String CALLER_AUTHENICATION = "org.apache.tomee.security.request.caller.authentication";

    LoginToContinue getLoginToContinue();

    static void saveRequest(final HttpServletRequest request) throws IOException {
        SavedRequest saved = new SavedRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                saved.addCookie(cookies[i]);
            }
        }
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                saved.addHeader(name, value);
            }
        }
        Enumeration<Locale> locales = request.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = locales.nextElement();
            saved.addLocale(locale);
        }

        int maxSavePostSize = MAX_SAVE_POST_SIZE;
        if (maxSavePostSize != 0) {
            ByteChunk body = new ByteChunk();
            body.setLimit(maxSavePostSize);

            byte[] buffer = new byte[4096];
            int bytesRead;
            InputStream is = request.getInputStream();

            while ( (bytesRead = is.read(buffer) ) >= 0) {
                body.append(buffer, 0, bytesRead);
            }

            // Only save the request body if there is something to save
            if (body.getLength() > 0) {
                saved.setContentType(request.getContentType());
                saved.setBody(body);
            }
        }

        saved.setMethod(request.getMethod());
        saved.setQueryString(request.getQueryString());
        saved.setRequestURI(request.getRequestURI());
        saved.setRequestURL(request.getRequestURL().toString());

        // Stash the SavedRequest in our session for later use
        request.getSession().setAttribute(ORIGINAL_REQUEST, saved);
    }

    static boolean matchRequest(final HttpServletRequest request) {
        // Has a session been created?
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        // Is there a saved request?
        SavedRequest originalRequest = (SavedRequest) request.getSession().getAttribute(ORIGINAL_REQUEST);
        if (originalRequest == null) {
            return false;
        }

        // Is there a saved principal?
        /*
        if (session.getNote(Constants.FORM_PRINCIPAL_NOTE) == null) {
            return false;
        }
        */

        // Does the request URI match?
        String requestURI = request.getRequestURI();
        return requestURI != null && requestURI.equals(originalRequest.getRequestURI());
    }

    static boolean hasRequest(final HttpServletRequest request) {
        return request.getSession().getAttribute(ORIGINAL_REQUEST) != null;
    }

    static SavedRequest getRequest(final HttpServletRequest request) {
        return (SavedRequest) request.getSession().getAttribute(ORIGINAL_REQUEST);
    }

    static void saveAuthentication(final HttpServletRequest request,
                                   final Principal principal,
                                   final Set<String> groups) {
        SavedAuthentication savedAuthentication = new SavedAuthentication(principal, groups);
        request.getSession().setAttribute(AUTHENTICATION, savedAuthentication);
    }

    static boolean hasAuthentication(final HttpServletRequest request) {
        return request.getSession().getAttribute(AUTHENTICATION) != null;
    }

    static SavedAuthentication getAuthentication(final HttpServletRequest request) {
        return (SavedAuthentication) request.getSession().getAttribute(AUTHENTICATION);
    }

    static void clearRequestAndAuthentication(final HttpServletRequest request) {
        request.getSession().removeAttribute(ORIGINAL_REQUEST);
        request.getSession().removeAttribute(AUTHENTICATION);
    }
}
