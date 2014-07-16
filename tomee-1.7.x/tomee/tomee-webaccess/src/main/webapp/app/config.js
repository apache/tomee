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
        'text': 'webjars/requirejs-text/2.0.10/text',

        // http://lesscss.org/
        'lib/less': 'webjars/less/1.6.0/less.min',

        'lib/codemirror': 'webjars/codemirror/3.21/lib/codemirror',
        'lib/show-hint': 'webjars/codemirror/3.21/addon/hint/show-hint',
        'lib/anyword-hint': 'webjars/codemirror/3.21/addon/hint/anyword-hint',
        'lib/javascript-mode': 'webjars/codemirror/3.21/mode/javascript/javascript',
        'lib/groovy-mode': 'webjars/codemirror/3.21/mode/groovy/groovy',

        // http://jquery.com/
        'lib/jquery': 'webjars/jquery/2.1.0/jquery.min',

        // http://twitter.github.com/bootstrap/
        'lib/bootstrap': 'webjars/bootstrap/3.1.0/js/bootstrap.min',

        // http://handlebarsjs.com/
        'lib/handlebars': 'webjars/handlebars/1.2.1/handlebars.min',

        'lib/underscore': 'webjars/underscorejs/1.5.2/underscore-min',

        'underscore.string': 'webjars/underscore.string/2.3.0/underscore.string',

        'lib/json2': 'webjars/json2/20110223/json2.min',

        'lib/backbone': 'webjars/backbonejs/1.0.0/backbone'
    },
    shim: {
        'lib/show-hint': {
            deps: ['lib/codemirror']
        },
        'lib/anyword-hint': {
            deps: ['lib/show-hint']
        },
        'lib/javascript-mode': {
            deps: ['lib/anyword-hint']
        },
        'lib/groovy-mode': {
            deps: ['lib/anyword-hint']
        },

        // bootstrap depends on jquery, therefore we need to load jquery first
        // http://requirejs.org/docs/api.html#config-shim
        'lib/bootstrap': {
            deps: ['lib/jquery']
        },

        'underscore.string': {
            deps: ['lib/underscore']
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