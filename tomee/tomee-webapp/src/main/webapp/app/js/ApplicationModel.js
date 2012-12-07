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

    var channel = TOMEE.ApplicationChannel;
    var appSocket = null;
    var reconnectTask = TOMEE.DelayedTask();
    var reconnectDelay = 5000;
    var urlBase = window.document.URL;

    urlBase = urlBase.replace(new RegExp('^' + window.location.protocol + '//'), '');
    urlBase = urlBase.replace(new RegExp('^' + window.document.location.host), '');
    urlBase = urlBase.replace('#', '');

    channel.bind('server-callback', 'socket-connection-message-received', function (data) {
        var bean = $.parseJSON(data);
        TOMEE.ApplicationChannel.send('new-data', bean);
    });

    channel.bind('server-connection', 'socket-connection-closed', function () {
        reconnectTask.delay(connectSocket, reconnectDelay);
    });

    channel.bind('server-command-callback', 'Logout', function (data) {
        delete sessionStorage.sessionId;
    });

    (function () {
        var params = {};
        if (sessionStorage.sessionId) {
            params.sessionId = sessionStorage.sessionId;
        }
        sendRequest({
            servlet:'session',
            params:params,
            callback:function (data) {
                sessionStorage.sessionId = data.sessionId;
                channel.send('server-connection', 'session-ready', {});
            }
        });
    })();

    function connectSocket() {
        try {
            appSocket = createSocket();
        } catch (e) {
            reconnectTask.delay(connectSocket, reconnectDelay);
        }
    }

    function createSocket() {
        var isFake = false;
        var socket = null;
        var host = (function () {
            var suffix = urlBase + 'socket';
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
            socket = (function () {
                // We need to simulate the socket object.
                // This browser does not have it.
                isFake = true;

                var readyStateON = true;

                function send(str) {
                    $.ajax({
                            url:urlBase + 'command',
                            type:'POST',
                            dataType:'text',
                            data:{
                                strParam:str
                            },
                            error:function (data) {
                                socket.onerror(data);
                            },
                            success:function (data) {
                                socket.onmessage({
                                    data:data
                                });
                            }
                        }
                    );
                }

                return {
                    readyState:readyStateON,
                    OPEN:readyStateON,
                    send:send
                }
            })();
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
                channel.send('server-command-callback', data.cmdName, data);

                if (data.success) {
                    channel.send('server-command-callback-success', data.cmdName, data);
                } else {
                    channel.send('server-command-callback-error', data.cmdName, data);
                }

            } else {
                channel.send('server-callback', 'socket-message-received', {
                    data:data
                });
            }
        };

        if (isFake) {
            socket.onopen();
        }

        return socket;
    }

    function isSocketReady() {
        return appSocket.readyState === appSocket.OPEN;
    }

    function sendMessage(bean) {
        if (isSocketReady()) {
            bean.sessionId = sessionStorage.sessionId;
            var str = JSON.stringify(bean);
            appSocket.send(str);
        } else {
            setTimeout(function () {
                sendMessage(bean);
            }, 1000);
        }
    }

    function sendRequest(bean) {
        $.ajax({
                url:urlBase + bean.servlet,
                method:'POST',
                dataType:'json',
                data:bean.params,
                success:function (data) {
                    if (bean.callback) {
                        bean.callback(data);
                    }
                    channel.send('server-command-callback-success', bean.servlet, {
                        data:data
                    });
                }
            }
        );
    }

    return {
        connectSocket:connectSocket,
        sendMessage:sendMessage,
        sendRequest:sendRequest
    }
};