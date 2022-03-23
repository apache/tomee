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

package org.apache.openejb.rest;

import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Variant;
import java.util.Date;
import java.util.List;

public class ThreadLocalRequest extends AbstractRestThreadLocalProxy<Request>
    implements Request {

    protected ThreadLocalRequest() {
        super(Request.class);
    }

    public ResponseBuilder evaluatePreconditions(final EntityTag eTag) {
        return get().evaluatePreconditions(eTag);
    }

    public ResponseBuilder evaluatePreconditions(final Date lastModified) {
        return get().evaluatePreconditions(lastModified);
    }

    public ResponseBuilder evaluatePreconditions(final Date lastModified, final EntityTag eTag) {
        return get().evaluatePreconditions(lastModified, eTag);
    }

    public Variant selectVariant(final List<Variant> vars) throws IllegalArgumentException {
        return get().selectVariant(vars);
    }

    public String getMethod() {
        return get().getMethod();
    }

    public ResponseBuilder evaluatePreconditions() {
        return get().evaluatePreconditions();
    }

}
