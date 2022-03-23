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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.resource.jdbc.router.FailOverRouter;

import jakarta.annotation.PostConstruct;

// an all in one RoutedDataSource + FailOverRouter
public class FailoverDataSource extends RoutedDataSource {
    public FailoverDataSource() {
        super(new FailOverRouter());
    }

    private FailOverRouter failoverRouter() {
        return FailOverRouter.class.cast(delegate);
    }

    @PostConstruct
    private void init() {
        failoverRouter().init();
    }

    public void setDatasourceNames(final String datasourceNames) {
        failoverRouter().setDatasourceNames(datasourceNames);
    }

    public void setDelimiter(final String delimiter) {
        failoverRouter().setDelimiter(delimiter);
    }

    public void setStrategy(final String strategy) {
        failoverRouter().setStrategy(strategy);
    }

    public void setStrategyInstance(final FailOverRouter.Strategy strategy) {
        failoverRouter().setStrategyInstance(strategy);
    }

    public void setExceptionSelectorInstance(final FailOverRouter.ExceptionSelector selector) {
        failoverRouter().setExceptionSelectorInstance(selector);
    }

    public void setExceptionSelector(final String selector) {
        failoverRouter().setExceptionSelector(selector);
    }

    public void setErrorHandlerInstance(final FailOverRouter.ErrorHandler errorHandler) {
        failoverRouter().setErrorHandlerInstance(errorHandler);
    }

    public void setErrorHandler(final String errorHandler) {
        failoverRouter().setErrorHandler(errorHandler);
    }
}
