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

TOMEE.storage = (function () {

    var defaultStore = (function() {
        var myStore = {};

        return {
            getItem: function(key) {
                return myStore[key];
            },
            setItem: function(key, value) {
                myStore[key] = value;
            }
        };

    })();

    var session = TOMEE.utils.getSafe(window.sessionStorage, defaultStore);
    var local = TOMEE.utils.getSafe(window.localStorage, defaultStore);

    return {
        getSession: function(key) {
            return session.getItem(key);
        },
        setSession: function(key, value) {
            session.setItem(key, value);
        },
        getLocal: function(key) {
            return local.getItem(key);
        },
        setLocal: function(key, value) {
            local.setItem(key, value);
        }
    }
})();