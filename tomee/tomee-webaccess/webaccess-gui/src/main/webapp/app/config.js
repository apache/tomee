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
        // https://github.com/requirejs/text
        'text': 'app/lib/require/text',

        // http://lesscss.org/
        'lib/less': 'app/lib/less/less.min',

        'lib/codemirror': 'app/lib/codemirror/lib/codemirror',
        'lib/show-hint': 'app/lib/codemirror/addon/hint/show-hint',
        'lib/javascript-hint': 'app/lib/codemirror/addon/hint/javascript-hint',
        'lib/javascript-mode': 'app/lib/codemirror/mode/javascript/javascript',
        'lib/groovy-mode': 'app/lib/codemirror/mode/groovy/groovy',

        // http://jquery.com/
        'lib/jquery': 'app/lib/jquery/jquery.min',

        // http://twitter.github.com/bootstrap/
        'lib/bootstrap': 'app/lib/bootstrap/js/bootstrap.min',

        // http://handlebarsjs.com/
        'lib/handlebars': 'app/lib/handlebars/handlebars',

        'lib/underscore': 'app/lib/underscorejs/underscore-min',

        'lib/json2': 'app/lib/json2/json2',

        'lib/backbone': 'app/lib/backbone/backbone-min'
    },
    shim: {
        'lib/show-hint': {
            deps: ['lib/codemirror']
        },
        'lib/javascript-hint': {
            deps: ['lib/show-hint']
        },
        'lib/javascript-mode': {
            deps: ['lib/javascript-hint']
        },
        'lib/groovy-mode': {
            deps: ['lib/codemirror']
        },

        // bootstrap depends on jquery, therefore we need to load jquery first
        // http://requirejs.org/docs/api.html#config-shim
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
        },

        'app/js/models': {
            deps: ['lib/underscore']
        }
    }
};