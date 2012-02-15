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

TOMEE.ApplicationI18N = (function (cfg) {
    var messages = {
        'application.name':'Apache TomEE',
        'application.footer':'Copyright © 2011 The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache and the Apache feather logo are trademarks of The Apache Software Foundation.',

        'app.toolbar.home':'Home',
        'app.toolbar.log':'Log',

        'app.home.menu.unknown':'Unknown',

        'app.home.menu.setup':'Setup',
        'app.home.menu.setup.test':'Testing your setup',
        'app.home.menu.setup.test.title':'Test Results',

        'app.home.menu.tools':'Tools',

        'app.home.menu.tools.jndi':'JNDI Browser',
        'app.home.menu.tools.jndi.title':'OpenEJB JNDI Namespace Browser',

        'app.home.menu.tools.class':'Class Viewer',

        'app.home.menu.tools.ejb':'EJB Viewer',
        'app.home.menu.tools.ejb.title':'OpenEJB Enterprise JavaBeans Viewer',

        'app.home.menu.tools.obj':'Object Invoker',

        'dummy':'dummy'
    };

    var get = function (key) {
        var result = messages[key];
        if (!result) {
            result = '[!' + key + '!]';
            TOMEE.log.error('Missing i18n message. key: "' + key + '"');
        }
        return result;
    }

    return {
        get:get
    };
})();