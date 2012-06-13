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

TOMEE.el = (function () {
    var getBorderSize = function (el) {
        return el.outerHeight(true) - el.height();
    };

    var getElMap = function (elCfg) {
        var elMap = {};
        mountEl(elCfg, elMap);
        return elMap;
    };

    var mountEl = function (elCfg, elMap) {
        if (elCfg.el) {
            elCfg.el.detach();

            if (elCfg.elName) {
                elMap[elCfg.elName] = elCfg.el;
            }
            //we dont need to construct it. it is already done
            return elCfg.el;
        }

        var el = $('<' + elCfg.tag + '></' + elCfg.tag + '>');

        if (elCfg.elName) {
            elMap[elCfg.elName] = el;
        }

        (function () {
            var attrs = elCfg.attributes;
            for (var key in attrs) {
                el.attr(key, attrs[key]);
            }
        })();

        (function () {
            var listeners = elCfg.listeners;
            for (var key in listeners) {
                el.bind(key, listeners[key]);
            }
        })();

        if (elCfg.cls) {
            el.addClass(elCfg.cls);
        }

        var children = TOMEE.utils.getArray(elCfg.children);
        for (var i = 0; i < children.length; i++) {
            el.append(mountEl(children[i], elMap));
        }

        if (elCfg.html) {
            el.html(elCfg.html);
        }

        if (elCfg.createCallback) {
            elCfg.createCallback(el);
        }

        return el;
    };

    return {
        getElMap:getElMap,
        getBorderSize:getBorderSize,
        getLocationValue:function (value) {
            if ($.isNumeric(value)) {
                return value + 'px';
            } else {
                return TOMEE.utils.getSafe(value, '0px');
            }
        }
    }
})();