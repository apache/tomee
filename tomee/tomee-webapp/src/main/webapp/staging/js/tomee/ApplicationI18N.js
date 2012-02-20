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

TOMEE.ApplicationI18N = (function () {
    "use strict";

    var messages = {
        'application.name': 'Apache TomEE',
        'application.footer': 'Copyright © 2011 The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and the Apache feather logo are trademarks of The Apache Software Foundation.',

        'app.toolbar.home': 'Home',
        'app.toolbar.jndi': 'Jndi',
        'app.toolbar.test': 'Test',
        'app.toolbar.help': 'Help',

        'app.home.menu.unknown': 'Unknown',

        'app.home.menu.setup': 'Setup',
        'app.home.menu.setup.test': 'Testing your setup',
        'app.home.menu.setup.test.title': 'Test Results',

        'app.home.menu.setup.test.testname.key.homeSet': 'openejb.home is set',
        'app.home.menu.setup.test.testname.key.homeExists': 'openejb.home exists',
        'app.home.menu.setup.test.testname.key.homeDirectory': 'openejb.home is a directory',
        'app.home.menu.setup.test.testname.key.libDirectory': 'has lib directory',

        'app.home.menu.setup.test.testname.key.openEjbInstalled': 'Were the OpenEJB classes installed',
        'app.home.menu.setup.test.testname.key.ejbsInstalled': 'Were the EJB classes installed',
        'app.home.menu.setup.test.testname.key.openEjbStarted': 'Was OpenEJB initialized (aka started)',
        'app.home.menu.setup.test.testname.key.testLookup': 'Performing a test lookup',

        'app.home.menu.tools': 'Tools',

        'app.home.menu.tools.jndi.browser': 'Jndi browser',
        'app.home.menu.tools.jndi.browser.info': 'Click to see more information about the object',
        'app.home.menu.tools.jndi.name': 'Name',

        'dummy': 'dummy'
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
        get: get
    };
})();