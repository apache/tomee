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

TOMEE.ApplicationTabConsole = function () {
    "use strict";

    var channel = TOMEE.ApplicationChannel,
        container = $(TOMEE.ApplicationTemplates.getValue('application-tab-console', {})),
        webservices = TOMEE.ApplicationTabWebservices(),
        jndi = TOMEE.ApplicationTabJndi(),
        codeArea = null,
        active = false,
        delayedContainerResize = TOMEE.DelayedTask();

    container.find('.webservices-div').append(webservices.getEl());
    container.find('.jndi-div').append(jndi.getEl());

    container.find('.dropdown-toggle').on('click', function (ev) {
        var button = $(ev.currentTarget),
            group = button.parent('.btn-group'),
            isOpened = group.hasClass('open');

        container.find('.btn-group').removeClass('open');

        if (!isOpened) {
            group.addClass('open');
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
                time:data.timeSpent,
                output:data.output
            },
            newLine = $(TOMEE.ApplicationTemplates.getValue(
                'application-tab-console-output-line', newLineData));

        btn.prop('disabled', false);
        consoleOutput.prepend(newLine);
    });

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
            text:codeArea.getValue()
        });
    }


    return {
        getEl:function () {
            return container;
        },
        onAppend:function () {
            if (!codeArea) {
                codeArea = CodeMirror(container.children('.tomee-code').get(0), {
                    lineNumbers:true,
                    value:TOMEE.ApplicationTemplates.getValue('application-tab-console-sample', {})
                });
            }
            codeArea.focus();
            active = true;
        },
        onDetach:function () {
            active = false;
        }
    };
};