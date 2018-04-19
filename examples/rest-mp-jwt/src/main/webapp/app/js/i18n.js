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

define(['lib/underscore', 'lib/handlebars', 'app/js/log'], function (underscore) {
    'use strict';

    var missing = Handlebars.compile('[!{{key}}!]');
    var messages = {
        'application.name': 'Moviefun',
        'load.dada': 'Click here to load sample data',
        'movies': 'Movies',
        'title': 'Title',
        'director': 'Director',
        'genre': 'Genre',
        'rating': 'Rating',
        'year': 'Year',
        'add.movie': 'Add movie',
        'movie': 'Movie',
        'close': 'Close',
        'save': 'Save',

        'dummy': ''
    };

    underscore.each(underscore.keys(messages), function (key) {
        var template = Handlebars.compile(messages[key]);
        messages[key] = template;
    });

    var get = function (key, values) {
        var template = messages[key];
        var cfg = values;
        if (!template) {
            template = missing;
            cfg = {
                key: key
            };
            window.console.error('Missing i18n message.', key);
        }
        return template(cfg);
    };

    Handlebars.registerHelper('i18n', function (key) {
        return get(key);
    });

    return {
        get: get
    };
});
