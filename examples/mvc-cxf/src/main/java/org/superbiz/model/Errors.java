/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.mvc.binding.ParamError;

@Named("error")
@RequestScoped
public class Errors {

    private List<ParamError> errors = new ArrayList<>();

    public void setErrors(List<ParamError> messages) {
        this.errors = messages;
    }

    public String getErrors() {
        return errors.stream()
                     .map(ParamError::getMessage)
                     .collect(Collectors.joining("<br>"));
    }

    public String getMessage(String param) {
        return errors.stream()
                     .filter(v -> v.getParamName().equals(param))
                     .map(ParamError::getMessage)
                     .findFirst()
                     .orElse("");
    }
}