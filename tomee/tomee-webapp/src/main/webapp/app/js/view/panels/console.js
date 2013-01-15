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

    var requirements = ['ApplicationChannel', 'ApplicationTemplates', 'view/panels/webservices', 'view/panels/jndi',
        'util/DelayedTask', 'util/I18N', 'lib/jquery', 'lib/codeMirror'];

    define(requirements, function (channel, templates, ApplicationTabWebservices, ApplicationTabJndi, DelayedTask, I18N) {
        var codeMirror = CodeMirror; //making lint happy

        function newObject() {
            var container = $(templates.getValue('application-tab-console', {}));
            var innerPanels = {
                webservices: ApplicationTabWebservices.newObject(),
                jndi: ApplicationTabJndi.newObject()
            };

            var codeArea = null;
            var active = false;
            var locked = true;
            var delayedContainerResize = DelayedTask.newObject();
            var userName = null;

            channel.bind('server-command-callback-success', 'Login', function (params) {
                userName = params.params.user;
            });
            channel.bind('server-command-callback-success', 'session', function (params) {
                userName = params.data.userName;
            });

            function setLocked(value) {
                locked = value;
                channel.send('ui-actions', 'locked-change', {
                    locked: value,
                    panel: 'console'
                });
            }

            function clearConsole() {
                if (!active) {
                    return;
                }

                var consoleOutput = container.find('.tomee-console-output');
                consoleOutput.empty();
            }

            function triggerScriptExecution() {
                if (!active) {
                    return;
                }

                var btn = container.find('.tomee-execute-btn');
                btn.prop('disabled', true);

                channel.send('ui-actions', 'execute-script', {
                    text: codeArea.getValue()
                });
            }

            container.find('.webservices-div').append(innerPanels.webservices.getEl());
            container.find('.jndi-div').append(innerPanels.jndi.getEl());

            container.find('.dropdown-toggle').on('click', function (ev) {
                var button = $(ev.currentTarget);
                var customAttr = 'tab-name';
                var group = button.parent('.btn-group');
                var tab = group.attr(customAttr);

                container.find('.btn-group').each(function (index, grpHtmlEl) {
                    var grpEl = $(grpHtmlEl);
                    var grpTabName = grpEl.attr(customAttr);
                    if (tab === grpTabName) {
                        return;
                    }
                    innerPanels[grpTabName].onDetach();
                    grpEl.removeClass('open');
                });


                if (group.hasClass('open')) {
                    group.removeClass('open');
                    innerPanels[tab].onDetach();
                } else {
                    group.addClass('open');
                    innerPanels[tab].onAppend();
                }
            });

            container.find('.tomee-execute-btn').on('click', function () {
                triggerScriptExecution();
            });

            channel.bind('ui-actions', 'window-ctrl-X-pressed', function () {
                triggerScriptExecution();
            });

            channel.bind('ui-actions', 'window-esc-pressed', function () {
                clearConsole();
            });

            container.find('.tomee-execute-clear-btn').on('click', function () {
                clearConsole();
            });

            channel.bind('ui-actions', 'container-resized', function (data) {
                var consoleOutput = container.find('.tomee-console-output'),
                    consoleEditor = container.find('.tomee-code'),
                    consoleBBar = container.find('.bbar'),
                    splitter = container.find('.splitter'),
                    availableSpace = data.containerHeight - consoleBBar.outerHeight() - splitter.outerHeight() - 10;

                consoleOutput.height(availableSpace / 2);
                consoleEditor.height(availableSpace / 2);

                // This guy likes special treatment
                delayedContainerResize.delay(function () {
                    if (codeArea) {
                        codeArea.setSize(null, availableSpace / 2);
                    }
                }, 1000);
            });

            channel.bind('server-command-callback', 'RunScript', function (data) {
                var btn = container.find('.tomee-execute-btn'),
                    consoleOutput = container.find('.tomee-console-output'),
                    newLineData = {
                        time: data.timeSpent,
                        output: data.output
                    },
                    newLine = $(templates.getValue('application-tab-console-output-line', newLineData));

                btn.prop('disabled', false);
                consoleOutput.prepend(newLine);
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
                    if (!codeArea) {
                        codeArea = codeMirror(container.children('.tomee-code').get(0), {
                            lineNumbers: true,
                            value: templates.getValue('application-tab-console-sample', {
                                port: window.location.port,
                                protocol: (function () {
                                    var protocol = window.location.protocol;
                                    protocol = protocol.replace(':', '');
                                    return protocol;
                                }()),
                                name: userName,
                                password: I18N.get('application.console.password')
                            })
                        });
                    }
                    codeArea.focus();
                    active = true;
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


