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
    baseUrl: 'app/js',
    paths: {
        // https://github.com/requirejs/text
        'text': '../lib/require/text',

        // http://lesscss.org/
        'lib/less': '../lib/less/less-1.3.0.min',

        // http://jquery.com/
        'lib/jquery': '../lib/jquery/jquery-1.7.2.min',

        'lib/codeMirror': '../lib/codemirror/codemirror-2.34/lib/codemirror',

        // http://twitter.github.com/bootstrap/
        'lib/bootstrap': '../lib/bootstrap/2.1.1/js/bootstrap.min',

        // http://handlebarsjs.com/
        'lib/handlebars': '../lib/handlebars/handlebars-1.0.rc.1'
    },
    shim: {
        // bootstrap depends on jquery, therefore we need to load jquery first
        // http://requirejs.org/docs/api.html#config-shim
        'lib/bootstrap': {
            deps: ['lib/jquery']
        }
    }
};