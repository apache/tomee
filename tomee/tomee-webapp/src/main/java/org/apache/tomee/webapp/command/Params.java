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

package org.apache.tomee.webapp.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class Params {
    private final Map<String, String[]> params;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public Params(HttpServletRequest req, HttpServletResponse resp) {
        this.params = req.getParameterMap();
        this.req = req;
        this.resp = resp;
    }

    private String getUnique(String key) {
        final String[] value = params.get(key);
        if (value == null || value.length == 0) {
            return null;
        }
        if (value.length > 1) {
            throw new IllegalArgumentException("the parameter " + key + " is not unique");
        }

        return value[0];
    }

    public String getString(String key) {
        final String value = getUnique(key);
        if (value == null || "".equals(value.trim())) {
            return null;
        }
        return value.trim();
    }

    public Integer getInteger(String key) {
        final String value = getString(key);

        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public Long getLong(String key) {
        final String value = getString(key);

        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean getBoolean(String key) {
        final String value = getString(key);
        return Boolean.valueOf(value);
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }
}
