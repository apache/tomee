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
 */

(function () {
    'use strict';

    var requirements = ['ApplicationChannel', 'util/DelayedTask', 'lib/jquery'];

    define(requirements, function (channel, DelayedTask) {
        var appSocket = null;
        var reconnectTask = DelayedTask.newObject();
        var reconnectDelay = 5000;
        var urlBase = window.document.URL;
        var sessionReady = false;

        urlBase = urlBase.replace(new RegExp('^' + window.location.protocol + '//'), '');
        urlBase = urlBase.replace(new RegExp('^' + window.document.location.host), '');
        urlBase = urlBase.replace('#', '');

        function createSocket() {
            var isFake = false;
            var socket = null;
            var host = (function () {
                var suffix = urlBase + 'socket';
                var path = window.document.location.host + suffix;
                if (window.location.protocol === 'http:') {
                    return 'ws://' + path;
                }
                return 'wss://' + path;
            }());

            if (window.WebSocket !== undefined) {
                socket = new window.WebSocket(host);
            } else if (window.MozWebSocket !== undefined) {
                socket = new window.MozWebSocket(host);
            } else {
                socket = (function () {
                    // We need to simulate the socket object.
                    // This browser does not have it.
                    isFake = true;

                    var readyStateON = true;

                    function send(str) {
                        $.ajax({
                            url: urlBase + 'command',
                            type: 'POST',
                            dataType: 'text',
                            data: {
                                strParam: str
                            },
                            error: function (data) {
                                socket.onerror(data);
                            },
                            success: function (data) {
                                socket.onmessage({
                                    data: data
                                });
                            }
                        });
                    }

                    return {
                        readyState: readyStateON,
                        OPEN: readyStateON,
                        send: send
                    };
                }());
            }

            socket.onopen = function () {
                channel.send('server-connection', 'socket-connection-opened', {});
            };

            socket.onclose = function () {
                channel.send('server-connection', 'socket-connection-closed', {});
            };

            socket.onerror = function (message) {
                channel.send('server-connection', 'socket-connection-error', {
                    message: message
                });
            };

            socket.onmessage = function (message) {
                var data = JSON.parse(message.data);
                if (data.cmdName) {
                    // Commands callback calls
                    channel.send('server-command-callback', data.cmdName, data);

                    if (data.success) {
                        channel.send('server-command-callback-success', data.cmdName, data);
                    } else {
                        channel.send('server-command-callback-error', data.cmdName, data);
                    }

                } else {
                    channel.send('server-callback', 'socket-message-received', {
                        data: data
                    });
                }
            };

            if (isFake) {
                socket.onopen();
            }

            return socket;
        }

        function connectSocket() {
            try {
                appSocket = createSocket();
            } catch (e) {
                reconnectTask.delay(connectSocket, reconnectDelay);
            }
        }

        function isSocketReady() {
            return appSocket.readyState === appSocket.OPEN;
        }

        function sendMessage(bean) {
            if (isSocketReady()) {
                bean.sessionId = window.sessionStorage.sessionId;
                var str = JSON.stringify(bean);
                appSocket.send(str);
            } else {
                window.setTimeout(function () {
                    sendMessage(bean);
                }, 1000);
            }
        }

        channel.bind('server-callback', 'socket-connection-message-received', function (data) {
            var bean = $.parseJSON(data);
            channel.send('new-data', bean);
        });

        channel.bind('server-connection', 'socket-connection-closed', function () {
            reconnectTask.delay(connectSocket, reconnectDelay);
        });

        channel.bind('server-command-callback', 'Logout', function (data) {
            delete window.sessionStorage.sessionId;
        });

        function sendRequest(bean) {
            $.ajax({
                url: urlBase + bean.servlet,
                method: 'POST',
                dataType: 'json',
                data: bean.params,
                success: function (data) {
                    if (bean.callback) {
                        bean.callback(data);
                    }
                    channel.send('server-command-callback-success', bean.servlet, {
                        data: data
                    });
                }
            });
        }

        (function () {
            var params = {};
            if (window.sessionStorage.sessionId) {
                params.sessionId = window.sessionStorage.sessionId;
            }
            sendRequest({
                servlet: 'session',
                params: params,
                callback: function (data) {
                    window.sessionStorage.sessionId = data.sessionId;
                    sessionReady = true;
                    channel.send('server-connection', 'session-ready', {});
                }
            });
        }());

        return {
            connectSocket: connectSocket,
            sendMessage: sendMessage,
            sendRequest: sendRequest,
            isSessionReady: function () {
                return sessionReady;
            }
        };
    });
}());


