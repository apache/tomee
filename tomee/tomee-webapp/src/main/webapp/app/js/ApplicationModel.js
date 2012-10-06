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

/**
 * This object handles all the data manipulation.
 *
 * @param cfg
 */
TOMEE.ApplicationModel = function () {
    "use strict";

    var channel = TOMEE.ApplicationChannel,
        appSocket = null,
        reconnectTask = TOMEE.DelayedTask(),
        reconnectDelay = 1000;

    channel.bind('server-callback', 'socket-connection-message-received', function (data) {
        var bean = $.parseJSON(data);
        TOMEE.ApplicationChannel.send('new-data', bean);
    });

    channel.bind('server-callback', 'socket-connection-closed', function () {
        reconnectTask.delay(connectSocket, reconnectDelay);
    });

    // First connection
    connectSocket();

    function connectSocket() {
        try {
            appSocket = createSocket();
        } catch (e) {
            reconnectTask.delay(connectSocket, reconnectDelay);
        }
    }

    function createSocket() {
        var socket = null,
            host = (function () {
                var suffix = '/tomee/socket';
                var path = window.document.location.host + suffix;
                if (window.location.protocol == 'http:') {
                    return 'ws://' + path;
                }
                return 'wss://' + path;
            })();

        if ('WebSocket' in window) {
            socket = new WebSocket(host);
        } else if ('MozWebSocket' in window) {
            socket = new MozWebSocket(host);
        } else {
            throw 'WebSocket is not supported by this browser.';
        }

        socket.onopen = function () {
            channel.send('server-connection', 'socket-connection-opened', {});
        };

        socket.onclose = function () {
            channel.send('server-connection', 'socket-connection-closed', {});
        };


        socket.onerror = function (message) {
            channel.send('server-connection', 'socket-connection-error', {
                message:message
            });
        };

        socket.onmessage = function (message) {
            var data = JSON.parse(message.data);
            if (data.cmdName) {
                // Commands callback calls
                channel.send('server-callback', data.cmdName, {
                    data:data
                });
            } else {
                channel.send('server-callback', 'socket-message-received', {
                    data:data
                });
            }
        };

        return socket;
    }

    function isSocketReady() {
        return appSocket.readyState === appSocket.OPEN;
    }

    function sendMessage(bean) {
        if (isSocketReady()) {
            var str = JSON.stringify(bean);
            appSocket.send(str);
        } else {
            setTimeout(function () {
                sendMessage(bean);
            }, 1000);
        }
    }

    return {
        sendMessage:sendMessage
    }
};