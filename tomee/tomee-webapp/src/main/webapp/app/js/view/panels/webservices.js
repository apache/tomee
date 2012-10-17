/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

TOMEE.ApplicationTabWebservices = function () {
    "use strict";

    var channel = TOMEE.ApplicationChannel,
        container = $(TOMEE.ApplicationTemplates.getValue('application-tab-webservices', {})),
        active = false;

    channel.bind('ui-actions', 'container-resized', function (data) {
        var outputHeight = data.containerHeight - 10;
        container.height(outputHeight);
    });

    channel.bind('ui-actions', 'window-F5-pressed', function () {
        triggerRefresh();
    });

    channel.bind('server-command-callback-success', 'GetWebServices', function (data) {
        var table = $(TOMEE.ApplicationTemplates.getValue('application-tab-webservices-table', {
            webservices:buildTableData(data.output)
        }));

        container.empty();
        container.append(table);
    });

    function buildTableData(data) {
        var rest = TOMEE.utils.getArray(data.rest),
            soap = TOMEE.utils.getArray(data.soap),
            result = [];

        function buildAppData(app, wsType) {
            var services = TOMEE.utils.getArray(app.services);

            TOMEE.utils.forEach(services, function (value) {
                result.push({
                    'wsType':wsType,
                    'app':app.name,
                    'data':value
                });
            });
        }

        TOMEE.utils.forEach(rest, function (app) {
            buildAppData(app, 'rest');
        });

        TOMEE.utils.forEach(soap, function (app) {
            buildAppData(app, 'soap');
        });

        return result;
    }

    function triggerRefresh() {
        channel.send('ui-actions', 'reload-webservices-table', {});
    }

    return {
        getEl:function () {
            return container;
        },
        onAppend:function () {
            active = true;
            triggerRefresh();
        },
        onDetach:function () {
            active = false;
        }
    };
};