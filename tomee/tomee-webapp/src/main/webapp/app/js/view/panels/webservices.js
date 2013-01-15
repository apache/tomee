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

(function () {
    'use strict';

    var requirements = ['ApplicationChannel', 'ApplicationTemplates', 'util/Obj', 'lib/jquery'];

    define(requirements, function (channel, templates, utils) {
        function newObject() {
            var container = $(templates.getValue('application-tab-webservices', {}));
            var active = false;

            function buildTableData(data) {
                var rest = utils.getArray(data.rest);
                var soap = utils.getArray(data.soap);
                var result = [];

                function buildAppData(app, wsType) {
                    var services = utils.getArray(app.services);

                    utils.forEach(services, function (value) {
                        result.push({
                            'wsType': wsType,
                            'app': app.name,
                            'data': value
                        });
                    });
                }

                utils.forEach(rest, function (app) {
                    buildAppData(app, 'rest');
                });

                utils.forEach(soap, function (app) {
                    buildAppData(app, 'soap');
                });

                return result;
            }

            function triggerRefresh() {
                if (!active) {
                    return;
                }
                channel.send('ui-actions', 'reload-webservices-table', {});
            }

            channel.bind('ui-actions', 'window-F5-pressed', function () {
                triggerRefresh();
            });

            channel.bind('server-command-callback-success', 'GetWebServices', function (data) {
                var table = $(templates.getValue('application-tab-webservices-table', {
                    webservices: buildTableData(data.output)
                }));

                container.find('table').remove();
                container.append(table);
            });

            return {
                getEl: function () {
                    return container;
                },
                onAppend: function () {
                    active = true;
                    triggerRefresh();
                },
                onDetach: function () {
                    active = false;
                }
            };
        }

        return {
            newObject: newObject
        };
    });
}());

