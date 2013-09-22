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

var APP_CONFIG = {
    baseUrl: window.ux.ROOT_URL,
    paths: {
        'text': 'app/lib/require/text',
        'lib/less': 'app/lib/less/less.min',
        'lib/jquery': 'app/lib/jquery/jquery.min',
        'lib/bootstrap': 'app/lib/bootstrap/js/bootstrap.min',
        'lib/handlebars': 'app/lib/handlebars/handlebars',
        'lib/underscore': 'app/lib/underscorejs/underscore-min',
        'lib/json2': 'app/lib/json2/json2',
        'lib/backbone': 'app/lib/backbone/backbone-min'
    },
    shim: {
        'lib/bootstrap': {
            deps: ['lib/jquery']
        },
        'lib/underscore': {
            exports: '_'
        },
        'lib/backbone': {
            deps: ['lib/jquery', 'lib/json2', 'lib/underscore']
        },
        'app/js/templates': {
            deps: ['lib/underscore', 'app/js/i18n']
        }
    }
};