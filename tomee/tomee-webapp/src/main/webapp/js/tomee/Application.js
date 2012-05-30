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
 * This is the entry point for our javascript application.
 * DO NOT add any logic here. All business logic should be implemented in the ApplicationController object.
 * That is ok to add global utility methods here.
 */
"use strict";
var TOMEE = {};
$(document).ready(function () {
    TOMEE.ApplicationController();
});

TOMEE.log = {
    info: function (msg) {
        //it is really needed to access console via window in ie8 or we get a "console is undefined" error
        if (window.console && window.console.info) {
            window.console.info(msg);
        }
    },
    error: function (msg) {
        //it is really needed to access console via window in ie8 or we get a "console is undefined" error
        if (window.console && window.console.error) {
            window.console.error(msg);
        }
    }
};

TOMEE.utils = {
    getArray: function (obj) {
        if (!obj) {
            return [];
        }

        if (obj instanceof Array) {
            return obj;
        }

        return [obj];
    },

    getObject: function (obj) {
        if (!obj) {
            return {};
        }
        return obj;
    },

    stringFormat: function() {
        var s = arguments[0];
        for (var i = 0; i < arguments.length - 1; i++) {
            var reg = new RegExp("\\{" + i + "\\}", "gm");
            s = s.replace(reg, arguments[i + 1]);
        }

        return s;
    }
};