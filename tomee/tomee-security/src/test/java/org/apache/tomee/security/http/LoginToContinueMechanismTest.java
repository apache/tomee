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

import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginToContinueMechanismTest {

    @Test
    public void hasRequest_noSession_returnsFalseWithoutCreatingSession() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        assertFalse(LoginToContinueMechanism.hasRequest(request));

        verify(request).getSession(false);
        verify(request, never()).getSession();
    }

    @Test
    public void getRequest_noSession_returnsNullWithoutCreatingSession() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        assertNull(LoginToContinueMechanism.getRequest(request));

        verify(request).getSession(false);
        verify(request, never()).getSession();
    }

    @Test
    public void hasAuthentication_noSession_returnsFalseWithoutCreatingSession() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        assertFalse(LoginToContinueMechanism.hasAuthentication(request));

        verify(request).getSession(false);
        verify(request, never()).getSession();
    }

    @Test
    public void getAuthentication_noSession_returnsNullWithoutCreatingSession() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        assertNull(LoginToContinueMechanism.getAuthentication(request));

        verify(request).getSession(false);
        verify(request, never()).getSession();
    }

    @Test
    public void clearRequestAndAuthentication_noSession_doesNotCreateSession() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        LoginToContinueMechanism.clearRequestAndAuthentication(request);

        verify(request).getSession(false);
        verify(request, never()).getSession();
    }
}
