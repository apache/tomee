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

    var deps = ['app/js/templates', 'app/js/i18n', 'lib/backbone', 'lib/javascript-mode', 'lib/groovy-mode'];
    define(deps, function (templates, i18n) {
        var codeMirror = CodeMirror; //making lint happy
        codeMirror.commands.autocomplete = function (cm) {
            codeMirror.showHint(cm, codeMirror.hint.anyword);
        };

        var codes = {
            javascript: templates.getValue('script-sample-javascript', {}),
            groovy: templates.getValue('script-sample-groovy', {})
        };

        var UserForm = Backbone.View.extend({
            tagName: 'div',
            className: 'ux-scripting',

            getValue: function (cls) {
                var me = this;
                var field = $(me.$el.find(cls).get(0));
                return field.val();
            },

            render: function () {
                var me = this;
                me.$el.empty();
                me.$el.append(templates.getValue('scripting-user', {}));
                return me;
            }
        });
        var userForm = new UserForm();
        userForm.render();

        var executeScript = function (evt) {
            if (evt) {
                evt.preventDefault();
            }
            var me = this;
            me.trigger('execute-action', {
                engine: me.editor.getOption("mode"),
                script: me.editor.getValue(),
                user: userForm.getValue('.ux-context-user'),
                password: userForm.getValue('.ux-context-password'),
                realm: userForm.getValue('.ux-context-realm')
            });
            me.$el.find('.ux-execute-script').addClass('disabled');
            me.$el.find('.ux-clean-execute-script').addClass('disabled');
        };

        var cleanOutput = function () {
            var me = this;
            var pre = $(me.$el.find('.ux-script-output>div.panel-body>pre').get(0));
            pre.empty();
        };

        var toggleMaximization = function (panel) {
            var btn = panel.find('.ux-maximize > span');
            if (panel.hasClass('ux-fullscreen')) {
                panel.removeClass('ux-fullscreen');
                panel.addClass('ux-normal');
                btn.addClass('glyphicon-fullscreen');
                btn.removeClass('glyphicon-minus');
            } else {
                panel.addClass('ux-fullscreen');
                panel.removeClass('ux-normal');
                btn.removeClass('glyphicon-fullscreen');
                btn.addClass('glyphicon-minus');
            }
        };

        var maximizePanelOutput = function () {
            var me = this;
            var panel = $(me.$el.find('.ux-script-output').get(0));
            toggleMaximization(panel);
        };

        var maximizePanelSource = function () {
            var me = this;
            var panel = $(me.$el.find('.ux-script-source').get(0));
            toggleMaximization(panel);
            me.fitCodeField();
        };

        var View = Backbone.View.extend({
            tagName: 'div',
            className: 'ux-scripting',
            events: {
                'click .ux-script-output>div.panel-heading>button.ux-clean': cleanOutput,
                'click .ux-script-output>div.panel-heading>button.ux-maximize': maximizePanelOutput,
                'click .ux-script-source>div.panel-heading>button.ux-maximize': maximizePanelSource,
                'click .ux-execute-script': function (evt) {
                    var me = this;
                    executeScript.call(me, evt);
                    me.updatePrimaryExecuteBtn('ux-execute-script');
                },
                'click .ux-clean-execute-script': function (evt) {
                    var me = this;
                    cleanOutput.call(me);
                    executeScript.call(me, evt);
                    me.updatePrimaryExecuteBtn('ux-clean-execute-script');
                },
                'click .ux-source-option': function (evt) {
                    // TRICK to avoid full page reload.
                    evt.preventDefault();
                    var me = this;
                    var myLink = $(evt.target);
                    me.trigger('type-chosen', {
                        name: myLink.attr('name')
                    });
                }
            },

            updatePrimaryExecuteBtn: function (newCls) {
                var me = this;
                var btn = $(me.$el.find('.ux-execute-primary').get(0));
                btn.removeClass('ux-clean-execute-script');
                btn.removeClass('ux-execute-script');
                btn.addClass(newCls);
                if (newCls === 'ux-execute-script') {
                    btn.html(i18n.get('execute'));
                } else {
                    btn.html(i18n.get('clean.execute'));
                }
            },

            showSourceType: function (name) {
                var me = this;
                //saving old code
                if (this.options.isRendered) {
                    codes[me.editor.getOption("mode")] = me.editor.getValue();
                }
                me.editor.setOption("mode", name);
                // swapping sources
                me.editor.setValue(codes[name]); // setting new code
                me.$el.find('.ux-source-choice').html(i18n.get(name));

            },

            fitCodeField: function () {
                var me = this;
                var panelBody = $(me.$el.find('.ux-script-source').get(0));
                me.editor.setSize(panelBody.width() - 15, panelBody.height() - 110);
            },

            renderCallback: function () {
                var me = this;
                me.fitCodeField();
            },

            appendOutput: function (output) {
                var me = this;
                var formatted = $('<div/>').text(output).html();
                var pre = $(me.$el.find('.ux-script-output>div.panel-body>pre').get(0));
                pre.append(formatted);
                me.$el.find('.ux-execute-script').removeClass('disabled');
                me.$el.find('.ux-clean-execute-script').removeClass('disabled');
                var contentArea = $(me.$el.find('.panel-body').get(0));
                contentArea.animate({
                    scrollTop: contentArea.prop("scrollHeight")
                }, 250);
                var sourcePanel = $(me.$el.find('.ux-script-source').get(0));
                if (sourcePanel.hasClass('ux-fullscreen')) {
                    toggleMaximization(sourcePanel);
                    me.fitCodeField();
                }
            },

            render: function () {
                var me = this;
                if (!this.options.isRendered) {
                    me.$el.empty();
                    me.$el.append(templates.getValue('scripting', {}));
                    me.editor = codeMirror.fromTextArea(me.$el.find('textarea').get(0), {
                        extraKeys: {"Ctrl-Space": "autocomplete"}
                    });
                    me.editor.setValue('');
                    me.showSourceType('javascript');
                    this.options.isRendered = true;
                    $(window).resize(function () {
                        me.fitCodeField();
                    });
                    var userBtn = $(me.$el.find('.ux-script-source>div.panel-heading>button.ux-user').get(0));
                    userBtn.popover({
                        html: true,
                        placement: 'bottom',
                        content: '<div class="ux-user-form"/>'
                    });
                    userBtn.on('shown.bs.popover', function () {
                        var form = $(me.$el.find('.ux-user-form').get(0));
                        userForm.$el.detach();
                        form.append(userForm.$el);
                    });
                }
                me.editor.focus();
                return me;
            }
        });
        return new View();
    });
}());


