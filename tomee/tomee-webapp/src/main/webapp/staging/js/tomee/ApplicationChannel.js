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
TOMEE.ApplicationChannel = function (cfg) {
    "use strict";

    var listeners = {};

    /**
     * Bind a listener to a given message
     *
     * @param messageKey this is the messageKey sent by another object
     * @param callback this is your callback function. It contains one parameter with all values sent by the sender object
     */
    var bind = function (messageKey, callback) {
        //avoiding "NullPointerException"
        if (!listeners[messageKey]) {
            listeners[messageKey] = [];
        }

        var myListeners = listeners[messageKey];
        if (myListeners.indexOf(callback) < 0) {
            myListeners.push(callback);
        }
    };

    /**
     * Unbind a listener to a given message
     *
     * @param messageKey this is the messageKey sent by another object
     * @param callback the same "function" object you used in the "bind" method
     */
    var unbind = function (messageKey, callback) {
        if (!listeners[messageKey]) {
            return;
        }

        var myListeners = listeners[messageKey];
        var index = myListeners.indexOf(callback);
        if (index >= 0) {
            myListeners.splice(index, 1);
        }
    };

    /**
     * Send a message
     *
     * @param messageKey your message key
     * @param paramsObj the parameters to the listeners callback methods
     */
    var send = function (messageKey, paramsObj) {
        if (!listeners[messageKey]) {
            return;
        }

        var myListeners = listeners[messageKey];

        //the safeParamsObj will never be null or undefined
        var safeParamsObj = paramsObj;
        if (!safeParamsObj) {
            safeParamsObj = {};
        }

        TOMEE.log.info("Message " + messageKey + " * " + myListeners.length);

        for (var i = 0; i < myListeners.length; i++) {
            (function (callback) {
                callback(safeParamsObj);

            })(myListeners[i]);
        }
    };

    return {
        bind: bind,
        unbind: unbind,
        send: send
    };
};