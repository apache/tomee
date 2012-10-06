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
    "use strict";
 */

(function() {
    var winConsole = window.console,

        // These are the available methods.
        // Add more to this list if necessary.
        consoleEmpty = {
            error: function() {},
            log: function() {}
        },

        consoleProxy = (function() {
            // This object wraps the "window.console"
            var consoleWrapper = {};

            function buildMethodProxy(key) {
                if (winConsole[key] && typeof winConsole[key] === 'function') {
                    consoleWrapper[key] = function() {
                        var cFunc = winConsole[key];
                        cFunc.call(winConsole, arguments);
                    };
                } else {
                    consoleWrapper[key] = function() {
                        consoleEmpty[key]();
                    };
                }
            }

            // Checking if the browser has the "console" object
            if (winConsole) {
                // Only the methods defined by the consoleMock
                // are available for use.
                for (var key in consoleEmpty) {
                    if (consoleEmpty.hasOwnProperty(key)) {
                        buildMethodProxy(key);
                    }
                }
            } else {
                consoleWrapper = consoleEmpty;
            }

            return consoleWrapper;
        })();

    window.console = consoleProxy;
})();