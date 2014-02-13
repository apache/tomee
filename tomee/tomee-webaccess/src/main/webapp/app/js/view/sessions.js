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

    var deps = ['app/js/templates', 'lib/underscore', 'lib/backbone'];
    define(deps, function (templates) {

        var View = Backbone.View.extend({
            tagName: 'div',
            className: 'ux-sessions panel panel-default',
            events: {
                'click .ux-refresh-btn': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('refresh-sessions', {});
                },
                'click .ux-expire-btn': function (evt) {
                    // TRICK to avoid full page reload.
                    evt.preventDefault();
                    var myLink = $(evt.target);
                    this.trigger('expire-session', {
                        sessionId: myLink.attr('data-session-id'),
                        context: myLink.attr('data-context-id')
                    });
                }
            },
            render: function (data) {
                var me = this;
                me.$el.empty();
                var tplValues = {};
                if (data) {
                    tplValues = data;
                }
                me.$el.append(templates.getValue('sessions', tplValues));
                return me;
            }
        });
        return new View();
    });
}());
