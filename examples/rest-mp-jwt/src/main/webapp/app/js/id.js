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

define(['lib/underscore', 'lib/handlebars'], function (underscore) {
    'use strict';

    var value = underscore.uniqueId('moviefun_');

    var current = function () {
        return value;
    };
    var next = function () {
        value = underscore.uniqueId('moviefun_');
        return value;
    };
    Handlebars.registerHelper('id_current', function (key) {
        return current();
    });
    Handlebars.registerHelper('id_next', function (key) {
        return next();
    });

    return {
        current: current,
        next: next
    };
});
