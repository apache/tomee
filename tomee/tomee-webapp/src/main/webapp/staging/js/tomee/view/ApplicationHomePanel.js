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

TOMEE.ApplicationHomePanel = function (cfg) {
    "use strict";

    var channel = cfg.channel;
    var model = cfg.jndiModel;

    var elements = (function () {
        var tpl = [
            '<div id="myCarousel" class="carousel slide">',
            '    <div class="carousel-inner">',
            '        <div class="item active" style="height: 300px">',
            '            <div class="carousel-caption">',
            '                <h4>First Thumbnail label</h4>',
            '                <p>Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit.</p>',
            '            </div>',
            '        </div>',
            '        <div class="item" style="height: 300px">',
            '            <div class="carousel-caption">',
            '                <h4>Second Thumbnail label</h4>',
            '                <p>Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit.</p>',
            '            </div>',
            '        </div>',
            '        <div class="item" style="height: 300px">',
            '            <div class="carousel-caption">',
            '                <h4>Third Thumbnail label</h4>',
            '                <p>Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit.</p>',
            '            </div>',
            '        </div>',
            '    </div>',
            '    <a class="left carousel-control" href="#myCarousel" data-slide="prev">&lsaquo;</a>',
            '</div>'
        ];

        //create the element
        var all = $(tpl.join(''));
        return {
            all: all
        };
    })();

    return {
        getEl: function () {
            return elements.all;
        }
    };
};