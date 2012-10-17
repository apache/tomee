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

TOMEE.I18N = (function () {

    var messages = {
        'application.name':'Apache TomEE',

        'ms':'ms',

        'connection.exception':'Connection exception',
        'connection.exception.message':'The application is waiting for the server.',

        'application.console':'Console',
        'application.console.run':'Execute',
        'application.console.clear.output':'Clear output',
        'application.console.done':'Script executed.',
        'application.console.run.time':'Time',
        'application.console.run.output.empty':'Empty',

        'application.log':'Log',
        'application.log.select.a.file':'Select a file',

        'application.webservices':'Webservices',
        'application.webservices.app.name':'Application',
        'application.webservices.ws.name':'Name',
        'application.webservices.ws.addr':'Address',
        'application.webservices.ws.port':'Port',

        'application.sign.in':'Sign In',
        'application.sign.out':'Sign Out',
        'application.log.in':'Login',
        'application.password':'Password',

        'dummy':'dummy'
    };

    var get = function (key) {
        var result = messages[key];
        if (!result) {
            result = '[!' + key + '!]';
            console.error('Missing i18n message.', key);
        }
        return result;
    };

    Handlebars.registerHelper('i18n', function (key) {
        return get(key);
    });

    return {
        get:get
    };
})();