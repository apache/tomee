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

    var requirements = ['ApplicationChannel', 'ApplicationTemplates', 'util/DelayedTask', 'view/ApplicationToolbarView',
        'view/panels/home', 'view/panels/console', 'view/panels/log', 'view/panels/status', 'util/Obj', 'lib/jquery'];

    define(requirements, function (channel, templates, DelayedTask, ApplicationToolbarView, ApplicationTabHome,
                                   ApplicationTabConsole, ApplicationTabLog, ApplicationTabStatus, utils) {
        var panelMap = {
            'home': ApplicationTabHome.newObject(),
            'console': ApplicationTabConsole.newObject(),
            'log': ApplicationTabLog.newObject(),
            'status': ApplicationTabStatus.newObject()
        };
        var selected = null;
        var container = $(templates.getValue('application', {}));
        var toolbar = ApplicationToolbarView.newObject();
        var myWindow = $(window);
        var delayedContainerResize = DelayedTask.newObject();
        var connectionPopupVisible = false;
        var applicationDisabled = $(templates.getValue('application-disabled', {}));
        var connectionPopup = $(templates.getValue('application-disconnected-popup', {}));

        function updateContainerSize() {
            var containerHeight,
                containerWidth,
                toolbarHeight = toolbar.getEl().outerHeight();

            containerHeight = myWindow.outerHeight();
            containerWidth = myWindow.outerWidth();

            container.css('height', containerHeight + 'px');
            container.css('width', containerWidth + 'px');

            channel.send('ui-actions', 'container-resized', {
                containerHeight: containerHeight - toolbarHeight,
                containerWidth: containerWidth - toolbarHeight
            });
        }

        function switchPanel(key) {
            if (panelMap[key].isLocked && panelMap[key].isLocked()) {
                //The panel is locked. The user should login first.
                return false;
            }

            if (selected) {
                selected.getEl().detach();
                selected.onDetach();
            }
            selected = panelMap[key];
            selected.getEl().appendTo(container);
            selected.onAppend();

            updateContainerSize();

            return true;
        }

        function switchPanelAndSendEvent(key) {
            if (switchPanel(key)) {
                channel.send('ui-actions', 'panel-switch', {
                    key: key
                });
            }
        }

        function showConnectionPopup() {
            if (connectionPopupVisible) {
                return;
            }
            connectionPopupVisible = true;
            container.append(applicationDisabled);
            container.append(connectionPopup);
        }

        function hideConnectionPopup() {
            if (!connectionPopupVisible) {
                return;
            }
            connectionPopupVisible = false;
            applicationDisabled.detach();
            connectionPopup.detach();
        }

        channel.bind('server-connection', 'socket-connection-opened', function (data) {
            hideConnectionPopup();
        });

        channel.bind('server-connection', 'socket-connection-closed', function (data) {
            showConnectionPopup();
        });

        channel.bind('server-connection', 'socket-connection-error', function (data) {
            showConnectionPopup();
        });

        channel.bind('ui-actions', 'toolbar-click', function (data) {
            switchPanelAndSendEvent(data.key);
        });

        //disable default contextmenu
        $(window.document).bind("contextmenu", function (e) {
            return false;
        });

        myWindow.on('resize', function () {
            delayedContainerResize.delay(updateContainerSize, 500);
        });

        myWindow.on('keyup', function (ev) {
            var result = {
                consumed: false
            };

            if (ev.keyCode === 18) { //ALT
                result = channel.send('ui-actions', 'window-alt-released', {});
            } else if (ev.keyCode === 17) { //CONTROL
                result = channel.send('ui-actions', 'window-ctrl-released', {});
            } else if (ev.keyCode === 16) { //SHIFT
                result = channel.send('ui-actions', 'window-shift-released', {});
            }

            if (result.consumed) {
                ev.preventDefault();
            }
        });

        myWindow.on('keydown', function (ev) {
            var key = [],
                keyStr = null;

            if (ev.altKey) {
                key.push('alt');
            } else if (ev.ctrlKey) {
                key.push('ctrl');
            } else if (ev.shiftKey) {
                key.push('shift');
            }

            if (key.length === 0 && !((ev.keyCode >= 112 && ev.keyCode <= 123) || ev.keyCode === 27)) { // F1...F12 or esc
                return; //nothing to do
            }

            keyStr = utils.keyCodeToString(ev.keyCode);
            if (!keyStr) {
                keyStr = ev.keyCode;
            }
            key.push(keyStr);

            var result = channel.send('ui-actions', 'window-' + key.join('-') + '-pressed', {});
            if (result.consumed) {
                ev.preventDefault();
            }
        });

        channel.bind('ui-actions', 'window-alt-1-pressed', function () {
            switchPanelAndSendEvent('home');
        });
        channel.bind('ui-actions', 'window-alt-2-pressed', function () {
            switchPanelAndSendEvent('console');
        });
        channel.bind('ui-actions', 'window-alt-3-pressed', function () {
            switchPanelAndSendEvent('log');
        });

        return {
            render: function () {
                var myBody = $('body');
                container.append(toolbar.getEl());
                myBody.append(container);
                switchPanel('home');
                showConnectionPopup();
                delayedContainerResize.delay(updateContainerSize, 500);
            }
        };
    });
}());
