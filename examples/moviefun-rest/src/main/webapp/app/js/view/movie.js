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

    var deps = ['app/js/templates', 'lib/underscore', 'lib/backbone', 'app/js/id'];
    define(deps, function (templates, underscore) {

        var View = Backbone.View.extend({
            tagName: 'div',
            className: 'modal ux-movie-window',
            events: {
                'click .ux-application': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('show-application', {});
                },
                'click .ux-close': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.remove();
                },
                'click .ux-save': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    var model = me.model;

                    function set(name) {
                        var field = $(me.$el.find('.ux-' + name).get(0));
                        model.set(name, field.val());
                    }

                    set('title');
                    set('director');
                    set('genre');
                    set('rating');
                    set('year');
                    me.trigger('save-model', {
                        model: model
                    });
                }
            },
            render: function () {
                var me = this;
                me.$el.empty();
                me.$el.append(templates.getValue('movie', {
                    title: me.model.get('title'),
                    director: me.model.get('director'),
                    genre: me.model.get('genre'),
                    rating: me.model.get('rating'),
                    year: me.model.get('year'),
                    currentYear: new Date().getFullYear()
                }));
                return me;
            }
        });
        return View;
    });
}());
