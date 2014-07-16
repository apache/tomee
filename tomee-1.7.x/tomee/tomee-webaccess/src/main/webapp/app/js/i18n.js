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
        'application.name': 'Apache TomEE',
        'visitor': 'visitor',
        'scripting': 'Scripting',
        'source': 'Source Code',
        'output': 'Output',
        'execute': 'Execute',
        'clean.execute': 'Clean and execute',
        'groovy': 'Groovy',
        'javascript': 'JavaScript',
        'log.files': 'Log Files',
        'log.file': 'Choose file',
        'dashboard': 'Dashboard',
        'sessions': 'Sessions',
        'session.id': 'Session Id',
        'session.context': 'Context',
        'session.creation.date': 'Created',
        'session.last.access': 'Accessed',
        'session.expiration.date': 'Expire',
        'scripting.user.realm': 'Realm Name',
        'scripting.user.name': 'User name',
        'scripting.user.password': 'User password',
        'contexts': 'Contexts'
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

    return {
        get: get
    };
});
