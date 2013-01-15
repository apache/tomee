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
 * This is the communication channel of our application.
 * One can send a message through this object. Other object can register listeners to the messages coming from it.
 *
 * Some other frameworks use the term MessageBus for similar  utilities.
 *
 * This appliation has many instances of this class because it is usefull to have a private channel/messageBus between two components.
 * In this application an example of that is the pagingToolChannel sending message to its parent.
 *
 */

(function () {
    'use strict';

    var requirements = ['util/Obj', 'lib/jquery', 'util/Log'];

    define(requirements, function (obj) {
        var channels = {};

        function createChannel(channelName) {
            var name = channelName;
            var listeners = {};

            /**
             * Bind a listener to a given message
             *
             * @param messageKey this is the messageKey sent by another object
             * @param callback this is your callback function. It contains one
             * parameter with all values sent by the sender object
             */
            function bind(messageKey, callback) {
                var myListeners = listeners[messageKey];

                //avoiding "NullPointerException"
                if (!myListeners) {
                    myListeners = [];
                    listeners[messageKey] = myListeners;
                }

                if (myListeners.indexOf(callback) < 0) {
                    myListeners.push(callback);
                }
            }

            /**
             * Unbind a listener to a given message
             *
             * @param messageKey this is the messageKey sent by another object
             */
            function unbind(messageKey) {
                if (!listeners[messageKey]) {
                    return;
                }
                delete listeners[messageKey];
            }

            function unbindAll() {
                obj.forEachKey(listeners, function (key) {
                    unbind(key);
                });
            }

            /**
             * Send a message
             *
             * @param messageKey your message key
             * @param paramsObj the parameters to the listeners callback methods
             */
            function send(messageKey, paramsObj) {
                var hasListeners = false;
                if (listeners[messageKey] && listeners[messageKey].length > 0) {
                    hasListeners = true;
                }
                console.log('Channel', name, 'key', messageKey, 'Parameters', paramsObj, 'Listeners available',
                    hasListeners);

                if (!hasListeners) {
                    return {
                        consumed: false
                    };
                }

                var myListeners = listeners[messageKey];

                //the safeParamsObj will never be null or undefined
                var safeParamsObj = paramsObj;
                if (!safeParamsObj) {
                    safeParamsObj = {};
                }

                obj.forEach(myListeners, function (callback) {
                    callback(safeParamsObj);
                });

                return {
                    consumed: true
                };
            }

            return {
                bind: bind,
                unbind: unbind,
                unbindAll: unbindAll,
                send: send
            };
        }

        function getChannel(name) {
            if (!channels[name]) {
                channels[name] = createChannel(name);
            }
            return channels[name];
        }

        function unbindAll(name) {
            if (name) {
                console.log('Unbinding all the listeners of "' + name + '"');
                getChannel(name).unbindAll();
            } else {
                console.warn('You are zapping all the channels and listeners!');
                obj.forEachKey(channels, function (key) {
                    getChannel(key).unbindAll();
                });
            }
        }

        return {
            bind: function (name, key, callback) {
                getChannel(name).bind(key, callback);
            },
            unbind: function (name, key) {
                getChannel(name).unbind(key);
            },
            unbindAll: unbindAll,
            send: function (name, key, data) {
                return getChannel(name).send(key, data);
            }
        };
    });
}());