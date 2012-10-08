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
        codeArea = null;

    container.find('.tomee-execute-btn').on('click', function () {
        channel.send('ui-actions', 'execute-script', {
            text:codeArea.getValue()
        });
    });

    container.find('.tomee-execute-clear-btn').on('click', function () {
        var consoleOutput = container.find('.tomee-console-output');
        consoleOutput.empty();
    });

    channel.bind('ui-actions', 'container-resized', function (data) {
        var consoleOutput = container.find('.tomee-console-output'),
            consoleEditor = container.find('.tomee-code'),
            outputHeight = data.containerHeight - data.toolbarHeight - consoleEditor.height();

        consoleOutput.css('padding-top', data.toolbarHeight + 'px');
        consoleOutput.height(outputHeight);
    });

    channel.bind('server-callback', 'RunScript', function (params) {
        var consoleOutput = container.find('.tomee-console-output'),
            newLineData = {
                time: params.data.timeSpent,
                output: params.data.output
            },
            newLine = $(TOMEE.ApplicationTemplates.getValue(
                'application-tab-console-output-line', newLineData));

        consoleOutput.prepend(newLine);
    });

    return {
        getEl:function () {
            return container;
        },
        onAppend:function () {
            if (!codeArea) {
                codeArea = CodeMirror(container.children('.tomee-code').get(0), {
                    lineNumbers:true,
                    value:'// Add your code here.\n'
                });
            }
            codeArea.focus();
        },
        onDetach:function () {
        }
    };
};