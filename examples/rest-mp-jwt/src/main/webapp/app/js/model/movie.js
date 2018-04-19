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

    var deps = ['lib/backbone'];
    define(deps, function () {
        var isString = function (obj) {
            return Object.prototype.toString.call(obj) === '[object String]';
        };
        return Backbone.Model.extend({
            urlRoot: window.ux.ROOT_URL + 'rest/movies',
            idAttribute: 'id',
            toJSON: function () {
                if (!!this.attributes.rating && isString(this.attributes.rating)) {
                    this.attributes.rating = parseInt(this.attributes.rating, 10);
                }
                if (!!this.attributes.year && isString(this.attributes.year)) {
                    this.attributes.year = parseInt(this.attributes.year, 10);
                }
                if (!!this.attributes.id && isString(this.attributes.id)) {
                    this.attributes.id = parseInt(this.attributes.id, 10);
                }
                return this.attributes;
            },
            defaults: {
                rating: 5,
                year: new Date().getFullYear()
            }
        });
    });
}());