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
    define(deps, function (templates, underscore) {

        var View = Backbone.View.extend({
            tagName: 'div',
            className: 'ux-log-files',
            events: {
                'click .ux-choose-file': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('load-file-options', {});
                },
                'click .ux-refresh-btn': function () {
                    var me = this;
                    var fileNameArea = $(me.$el.find('.ux-choose-file>span').get(0));
                    me.triggerFileLoad(fileNameArea.html());
                },
                'click .ux-scrolldown-btn': function () {
                    var me = this;
                    var contentArea = $(me.$el.find('.panel-body').get(0));
                    contentArea.animate({
                        scrollTop: contentArea.prop("scrollHeight")
                    }, 250);
                }
            },
            triggerFileLoad: function (fileName) {
                var me = this;
                me.ux.selectedHref = fileName;
                me.$el.find('.ux-refresh-btn').addClass('disabled');
                me.$el.find('.ux-choose-file').addClass('disabled');
                var fileNameArea = $(me.$el.find('.ux-choose-file>span').get(0));
                fileNameArea.html(me.ux.selectedHref);
                me.trigger('load-file', {
                    href: me.ux.selectedHref
                });
            },
            fileSelectedCalback: function (evt) {
                // TRICK to avoid full page reload.
                evt.preventDefault();
                var me = this;
                var myLink = $(evt.target);
                me.triggerFileLoad(myLink.attr('href'));
            },
            showList: function (files) {
                var me = this;
                var menu = $(me.$el.find('.ux-choose-file-menu').get(0));
                menu.empty();
                underscore.each(files, function (file) {
                    var link = $(templates.getValue('logfile-link', {
                        href: file
                    }));
                    link.on('click', function (evt) {
                        me.fileSelectedCalback(evt);
                    });
                    menu.prepend(link);
                });
            },
            showFile: function (content) {
                var me = this;
                var contentArea = $(me.$el.find('.panel-body>pre').get(0));
                contentArea.empty();
                var formatted = $('<div/>').text(content).html();
                contentArea.append(formatted);
                me.$el.find('.ux-refresh-btn').removeClass('disabled');
                me.$el.find('.ux-choose-file').removeClass('disabled');
            },
            render: function () {
                var me = this;
                if (!this.options.isRendered) {
                    me.ux = me.ux || {};
                    me.$el.empty();
                    me.$el.append(templates.getValue('logfiles', {}));
                    this.options.isRendered = true;
                }
                if (me.ux.selectedHref) {
                    me.triggerFileLoad(me.ux.selectedHref);
                }
                return this;
            }
        });
        return new View();
    });
}());
