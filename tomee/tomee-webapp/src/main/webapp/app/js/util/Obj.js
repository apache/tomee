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

    // http://www.quirksmode.org/js/keys.html
    function keyCodeToString(keyCode) {
        if(keyCode === 16) {
            return 'shift';
        }

        if(keyCode === 17) {
            return 'control';
        }

        if(keyCode === 18) {
            return 'alt';
        }

        // Numbers or Letters
        if (keyCode >= 48 && keyCode <= 57 || //Numbers
            keyCode >= 65 && keyCode <= 90) { //Letters
            return String.fromCharCode(keyCode);
        }

        if (keyCode >= 112 && keyCode <= 123) { //F1 -> F12
            return 'F' + (keyCode - 111);
        }

        return null;
    }

    function isPrimitive(value) {
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
    }

    function getSafe(obj, defaultValue) {
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
    }

    function toArray(obj, objBuilder) {
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
    }

    function getArray(obj) {
        if (!obj) {
            return [];
        }

        if (obj instanceof Array) {
            return obj;
        }

        return [obj];
    }

    function getObject(obj) {
        if (!obj) {
            return {};
        }
        return obj;
    }

    function stringFormat(str, values) {
        var result = str;
        for (var key in values) {
            var reg = new RegExp("\\{" + key + "\\}", "gm");
            result = result.replace(reg, values[key]);
        }
        return result;
    }

    function forEach(value, callback) {
        var arr = getArray(value);
        for (var i = 0; i < arr.length; i++) {
            callback(arr[i], i);
        }
    }

    return {
        keyCodeToString: keyCodeToString,
        isPrimitive:isPrimitive,
        getSafe:getSafe,
        toArray:toArray,
        getArray:getArray,
        getObject:getObject,
        stringFormat:stringFormat,
        forEach:forEach
    }
})();