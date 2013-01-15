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

(function () {
    'use strict';

    var requirements = ['util/Obj', 'lib/handlebars', 'util/Log'];

    define(requirements, function (utils) {
        var missing = Handlebars.compile('[!{{key}}!]');
        var messages = {
            'application.name': 'Apache TomEE',

            'ms': 'ms',

            'connection.exception': 'Connection exception',
            'connection.exception.message': 'The application is waiting for the server.',

            'application.home': 'Home',

            'application.status': 'Status',
            'application.status.install': 'install',
            'application.status.reinstall': 're-install',
            'application.status.isAgentInstalled': 'Is the agent installed? {{message}}',
            'application.status.isListenerInstalled': 'Is the listener installed? {{message}}',

            'application.jndi': 'JNDI',
            'application.jndi.path': 'Path',

            'application.console': 'Console',
            'application.console.run': 'Execute',
            'application.console.run.error': 'Script error.',
            'application.console.clear.output': 'Clear output',
            'application.console.done': 'Script executed.',
            'application.console.password': '[Your password goes here]',
            'application.console.run.time': 'Time',
            'application.console.run.output.empty': 'Empty',

            'application.log': 'Log',
            'application.log.select.a.file': 'Select a file',

            'application.webservices': 'Webservices',
            'application.webservices.app.name': 'Application',
            'application.webservices.ws.name': 'Name',
            'application.webservices.ws.addr': 'Address',
            'application.webservices.ws.port': 'Port',

            'application.sign.in': 'Sign In',
            'application.sign.out': 'Sign Out',
            'application.log.in': 'Login',
            'application.log.error': 'Login error. Please try again.',
            'application.log.bad': 'Bad user or password. Please try again.',
            'application.log.hello': 'Hello {{userName}}!',
            'application.password': 'Password',

            'dummy': 'dummy'
        };

        utils.forEachKey(messages, function (key, value) {
            var template = Handlebars.compile(value);
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
                console.error('Missing i18n message.', key);
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
}());


