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

    var deps = ['app/js/templates', 'app/js/i18n', 'lib/backbone'];
    define(deps, function (templates) {

        var View = Backbone.View.extend({
            el: 'body',

            showView: function (view) {
                var me = this;
                var contentarea = me.$('.ux-contentarea');
                if (me.currentView) {
                    me.currentView.$el.detach();
                }
                me.currentView = view;
                me.currentView.render();
                contentarea.append(me.currentView.el);
                if (view.renderCallback) {
                    view.renderCallback();
                }
                me.$('.ux-app-menu-item').removeClass('active');
                var myMenuItem = me.$('li.ux-app-menu-item.' + view.className);
                myMenuItem.addClass('active');
            },

            render: function () {
                if (this.options.isRendered) {
                    return this;
                }
                var html = templates.getValue('container', {
                    userName: ''
                });
                this.$el.html(html);

                // render it only once
                this.options.isRendered = true;
                return this;
            }
        });

        return new View({});
    });
}());
