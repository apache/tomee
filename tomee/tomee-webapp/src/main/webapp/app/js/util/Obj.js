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

TOMEE.utils = (function () {

    var isPrimitive = function (value) {
        if ('number' === (typeof value)) {
            return true;
        }

        if ('string' === (typeof value)) {
            return true;
        }

        if ('boolean' === (typeof value)) {
            return true;
        }

        return false;
    };

    var getSafe = function (obj, defaultValue) {
        if (obj instanceof Function) {
            try {
                return obj();

            } catch (ex) {
                return defaultValue;
            }
        }

        if (obj) {
            return obj;
        }
        return defaultValue;
    };

    var toArray = function (obj, objBuilder) {
        if (!obj) {
            return [];
        }

        if (obj instanceof Array) {
            return obj;
        }

        var result = [];
        for (var key in obj) {
            result.push(objBuilder(key, obj[key]));
        }

        return result;
    };

    var getArray = function (obj) {
        if (!obj) {
            return [];
        }

        if (obj instanceof Array) {
            return obj;
        }

        return [obj];
    };

    var getObject = function (obj) {
        if (!obj) {
            return {};
        }
        return obj;
    };

    var stringFormat = function (str, values) {
        var result = str;
        for (var key in values) {
            var reg = new RegExp("\\{" + key + "\\}", "gm");
            result = result.replace(reg, values[key]);
        }
        return result;
    };

    var forEach = function (value, callback) {
        var arr = getArray(value);
        for(var i = 0; i < arr.length; i++) {
            callback(arr[i], i);
        }
    };

    return {
        isPrimitive:isPrimitive,
        getSafe:getSafe,
        toArray:toArray,
        getArray:getArray,
        getObject:getObject,
        stringFormat:stringFormat,
        forEach:forEach
    }
})();