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
            var container = $(templates.getValue('application-tab-log', {}));
            var selectedFile = null;
            var active = false;
            var locked = true;

            function setLocked(value) {
                locked = value;
                channel.send('ui-actions', 'locked-change', {
                    locked: value,
                    panel: 'log'
                });
            }

            function triggerFileSelected() {
                if (!active || !selectedFile) {
                    return;
                }
                channel.send('ui-actions', 'log-file-selected', {
                    file: selectedFile
                });
            }

            function setFileName(name) {
                var el = container.find('.log-file-name');
                el.html(name);
                selectedFile = name;
            }

            channel.bind('ui-actions', 'container-resized', function (data) {
                var consoleOutput = container.find('.tomee-log-output'),
                    bbar = container.find('.bbar'),
                    outputHeight = data.containerHeight - bbar.height() + 2;

                consoleOutput.height(outputHeight);
            });

            channel.bind('server-command-callback-success', 'GetLogFiles', function (data) {
                var logFiles = container.find('.tomee-log-files');
                logFiles.empty();

                utils.forEach(data.output.files, function (value) {
                    var file = $(templates.getValue('application-tab-log-file', {
                        file: value
                    }));
                    file.on('click', function () {
                        channel.send('ui-actions', 'log-file-selected', {
                            file: value
                        });
                    });
                    logFiles.append(file);
                });
            });

            channel.bind('server-command-callback-success', 'GetLog', function (data) {
                setFileName(data.output.log.name);
                var lines = container.find('.tomee-log-output');
                lines.empty();
                lines.append($(templates.getValue('application-tab-log-lines', {
                    lines: data.output.log.lines
                })));

                lines.animate({
                    scrollTop: lines.prop("scrollHeight") - lines.height()
                }, 500);
            });

            container.find('.log-file-name').on('click', function () {
                triggerFileSelected();
            });

            channel.bind('ui-actions', 'log-file-selected', function (param) {
                setFileName(param.file);
            });

            channel.bind('ui-actions', 'window-F5-pressed', function () {
                triggerFileSelected();
            });

            channel.bind('server-command-callback-success', 'Login', function (params) {
                if (params.output.loginSuccess) {
                    setLocked(false);
                } else {
                    setLocked(true);
                }
            });

            channel.bind('server-command-callback-success', 'session', function (params) {
                if (params.data.userName) {
                    setLocked(false);
                } else {
                    setLocked(true);
                }
            });

            return {
                getEl: function () {
                    return container;
                },
                onAppend: function () {
                    active = true;
                    channel.send('ui-actions', 'load-file-names', {});
                },
                onDetach: function () {
                    active = false;
                },
                isLocked: function () {
                    return locked;
                }
            };
        }

        return {
            newObject: newObject
        };
    });
}());


