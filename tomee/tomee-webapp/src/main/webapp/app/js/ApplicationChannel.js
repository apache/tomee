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
TOMEE.ApplicationChannel = (function () {

    function createChannel(channelName) {
        "use strict";

        var name = channelName,
            listeners = {};

        /**
         * Bind a listener to a given message
         *
         * @param messageKey this is the messageKey sent by another object
         * @param callback this is your callback function. It contains one
         * parameter with all values sent by the sender object
         */
        function bind(messageKey, callback) {
            //avoiding "NullPointerException"
            if (!listeners[messageKey]) {
                listeners[messageKey] = $.Callbacks();
            }

            var myListeners = listeners[messageKey];
            if (!myListeners.has(callback)) {
                // wrap the callback method in order to avoid the standard
                // jquery behaviour for callbacks exceptions
                // (http://bugs.jquery.com/ticket/11193)
                var callbackWrapper = function (paramsObj) {
                    try {
                        return callback(paramsObj);

                    } catch (e) {
                        console.error(
                            'Cannot execute listener callback', e,
                            'Channel', name,
                            'key', messageKey,
                            'Parameters', paramsObj
                        );
                    }
                };

                myListeners.add(callbackWrapper);
            }
        }

        /**
         * Unbind a listener to a given message
         *
         * @param messageKey this is the messageKey sent by another object
         * @param callback the "function" object you used in the "bind" method
         */
        function unbind(messageKey, callback) {
            if (!listeners[messageKey]) {
                return;
            }

            var myListeners = listeners[messageKey];
            myListeners.remove(callback);
        }

        /**
         * Send a message
         *
         * @param messageKey your message key
         * @param paramsObj the parameters to the listeners callback methods
         */
        function send(messageKey, paramsObj) {
            console.log(
                'Channel', name,
                'key', messageKey,
                'Parameters', paramsObj,
                'Listeners available',
                !(!listeners[messageKey])
            );

            if (!listeners[messageKey]) {
                return;
            }

            var myListeners = listeners[messageKey];

            //the safeParamsObj will never be null or undefined
            var safeParamsObj = paramsObj;
            if (!safeParamsObj) {
                safeParamsObj = {};
            }

            myListeners.fire(safeParamsObj);
        }

        return {
            bind:bind,
            unbind:unbind,
            send:send
        };
    }

    ;

    var channels = {};

    function getChannel(name) {
        if (!channels[name]) {
            channels[name] = createChannel(name);
        }
        return channels[name];
    }

    return {
        bind:function (name, key, callback) {
            getChannel(name).bind(key, callback);
        },
        unbind:function (name, key, callback) {
            getChannel(name).unbind(key, callback);
        },
        send:function (name, key, data) {
            getChannel(name).send(key, data);
        }
    };
})();