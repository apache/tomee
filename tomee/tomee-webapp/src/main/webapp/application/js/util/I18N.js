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
        'application.footer':'Copyright © 2012 The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and the Apache feather logo are trademarks of The Apache Software Foundation.',
        'application.logout':'Logout',

        'application.guest':'Guest',

        'application.home':'Home',
        'application.apps':'Apps',

        'application.log':'Log',
        'application.log.load':'Load',
        'application.log.load.everything': 'Everything',
        'application.log.load.last': 'Last {number} lines',

        'application.deployments':'Deployments',

        'application.jdni':'Jndi',
        'application.jdni.load':'Load',
        'application.jdni.lookup':'Lookup',
        'application.jdni.class':'Jndi Class',
        'application.jdni.class.close':'Close',

        'application.saved.objects':'Saved Objects',
        'application.saved.objects.load':'Load',

        'application.console':'Console',
        'application.console.execute':'Execute',

        'application.mdbs':'MDBs',
        'application.ws':'Webservices',

        'application.error.close':'Close',

        'dummy':'dummy'
    };

    var get = function (key) {
        var result = messages[key];
        if (!result) {
            result = '[!' + key + '!]';
            TOMEE.log.error('Missing i18n message. key: "' + key + '"');
        }
        return result;
    };

    return {
        get:get
    };
})();