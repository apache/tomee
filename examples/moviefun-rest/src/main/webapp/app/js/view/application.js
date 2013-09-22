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

    var deps = ['app/js/templates', 'app/js/view/application-table-row', 'app/js/view/application-table-paginator',
        'lib/underscore', 'lib/backbone'];
    define(deps, function (templates, TableRowView, paginator, underscore) {

        var View = Backbone.View.extend({
            tagName: 'div',
            className: 'ux-application',

            loadDataLink: $(templates.getValue('load-data-link', {})),

            events: {
                'click .ux-application': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('navigate', {
                        path: 'application'
                    });
                },
                'click .ux-add-btn': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('add', {});
                }
            },

            render: function () {
                var me = this;
                if (!this.options.isRendered) {
                    me.$el.empty();
                    me.$el.append(templates.getValue('application', {}));
                    me.loadDataLink.on('click', function (evt) {
                        evt.preventDefault();
                        me.trigger('load-sample', {});
                    });
                    me.options.isRendered = true;
                }
                return this;
            },

            addRows: function (rows) {
                var me = this;
                var tbody = $(me.$el.find('tbody').get(0));
                tbody.empty();
                paginator.$el.detach();
                me.loadDataLink.detach();
                underscore.each(rows, function (model) {
                    var row = new TableRowView({
                        model: model
                    });
                    row.on('delete', function (data) {
                        me.trigger('delete', data);
                    });
                    row.on('edit', function (data) {
                        me.trigger('edit', data);
                    });
                    tbody.append(row.render().$el);
                });
                var addButton = $(me.$el.find('.ux-add-btn').get(0));
                if (underscore.isEmpty(rows)) {
                    addButton.before(me.loadDataLink);
                } else {
                    addButton.before(paginator.$el);
                }
            }
        });
        return new View();
    });
}());
