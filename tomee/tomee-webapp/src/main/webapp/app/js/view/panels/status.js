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

    var requirements = ['ApplicationChannel', 'ApplicationTemplates', 'util/Obj', 'util/Sequence', 'util/I18N', 'lib/jquery'];

    define(requirements, function (channel, templates, utils, Sequence, I18N) {
        function newObject() {
            var container = $(templates.getValue('application-tab-status', {}));
            var active = false;
            var locked = true;
            var loaded = false;

            function setLocked(value) {
                locked = value;
                channel.send('ui-actions', 'locked-change', {
                    locked: value,
                    panel: 'status'
                });
            }

            function getPaths() {
                var result = {};
                container.find('.path').each(function (index, el) {
                    var field = $(el);
                    result[field.attr('param-key')] = field.val();
                });
                return result;
            }

            container.find('.buttons').on('click', function () {
                channel.send('ui-actions', 'openejb-installer-clicked', getPaths());
            });

            channel.bind('ui-actions', 'container-resized', function (data) {
                var consoleOutput = container.find('.tomee-status-output');
                var bbar = container.find('.bbar');
                var outputHeight = data.containerHeight - bbar.height() - 3;

                consoleOutput.height(outputHeight);
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

            function showAlert(outMessages, messageType) {
                var messages = utils.getArray(outMessages);
                if (messages.length < 1) {
                    return;
                }
                channel.send('ui-actions', 'show-notification', {
                    message: messages.join('<BR>'),
                    messageType: 'alert-' + messageType
                });
            }

            channel.bind('server-command-callback-success', 'RunInstaller', function (params) {
                showAlert(params.output.infos, 'success');
                showAlert(params.output.warnings, 'warning');
                showAlert(params.output.errors, 'error');
            });

            channel.bind('server-command-callback-success', 'GetStatus', function (data) {
                var output = container.find('.tomee-status-output');
                var buttons = container.find('.buttons');

                var properties = [];

                utils.forEachKey(data.output.mainPaths, function (key, value) {
                    properties.push({
                        key: key,
                        value: value
                    });
                });

                output.empty();
                output.append($(templates.getValue('application-tab-status-lines', {
                    uniqueId: Sequence.next('path'),
                    properties: properties
                })));

                buttons.find('.install').each(function (index, element) {
                    var btn = $(element);
                    if (data.output.isListenerInstalled || data.output.isAgentInstalled) {
                        btn.prop('disabled', true);
                    } else {
                        btn.prop('disabled', false);
                    }
                });

                showAlert([
                    I18N.get('application.status.isAgentInstalled', {
                        message: String(data.output.isAgentInstalled)

                    }),
                    I18N.get('application.status.isListenerInstalled', {
                        message: String(data.output.isListenerInstalled)
                    })
                ], 'success');

                loaded = true;
            });

            return {
                getEl: function () {
                    return container;
                },
                onAppend: function () {
                    active = true;
                    if (!loaded) {
                        channel.send('ui-actions', 'load-status', {});
                    }
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


