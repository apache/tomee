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

(function () {
    'use strict';

    var files = [
        'container',
        'application',
        'application-table-row',
        'application-table-paginator-button',
        'load-data-link',
        'movie'
    ];

    function loop(values, callback) {
        var index;
        for (index = 0; index < values.length; index += 1) {
            callback(values[index], index);
        }
    }

    // Preparing the "requirements" paths.
    var requirements = [];
    loop(files, function (file) {
        requirements.push('text!app/js/templates/' + file + '.handlebars');
    });

    define(requirements, function () {
        var templates = {};

        var myArgs = arguments;
        loop(files, function (file, i) {
            templates[file] = Handlebars.compile(myArgs[i]);
        });

        return {
            getValue: function (templateName, cfg) {
                var template = templates[templateName];
                if (!template) {
                    throw 'Template not registered. "' + templateName + '"';
                }
                var result;
                if (cfg) {
                    result = template(cfg);
                } else {
                    result = template({});
                }
                return result;
            }
        };
    });
}());

