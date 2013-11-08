/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.trader.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HTML;

/**
 * A set of static utilities to create decorated HTML labels for price and price changes.
 * 
 * @author Pinaki Poddar
 *
 */
public class FormatUtil {
    public static final NumberFormat priceFormat  = NumberFormat.getFormat("#,##0.00");
    public static final NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
    public static final NumberFormat volumeFormat = NumberFormat.getFormat("#,##0");
    
    /**
     * Creates a HTML for the formatted price without any style.
     */
    public static HTML formatPrice(double price) {
        return formatPrice(price, false);
    }
    
    /**
     * Creates a HTML label for the formatted price with a style based on the signum of the price.
     * The positive and negative values are formatted with CSS styles <code>positive</code> and
     * <code>negative</code> respectively.
     */
    public static HTML formatPrice(double price, boolean style) {
        HTML label = new HTML();
        label.setText(priceFormat.format(price));
        if (style)
            label.addStyleName(price >= 0 ? "positive" : "negative");
        return label;
    }
    
    /**
     * Creates a HTML label for the given integer.
     */
    public static HTML formatVolume(int volume) {
        HTML label = new HTML();
        label.setText(volumeFormat.format(volume));
        return label;
    }
    
    /**
     * Creates a HTML label for the difference of two given numbers and applies style based
     * on the sign of the difference. Optionally adds percentage change.
     * 
     * @param p1 first value
     * @param p2 second value
     * @param pct if true, adds a percentage change
     */
    public static HTML formatChange(double p1, double p2, boolean pct) {
        String raw = changeFormat.format(p1-p2);
        if (pct && p1 != 0) {
            double delta = p1 - p2;
            String pctraw = changeFormat.format(100*delta/p1)+"%";
            raw += " (" + pctraw + ")";
        }
        String style = p1 >= p2 ? "positive" : "negative";
        HTML html = new HTML();
        html.setText(raw);
        html.addStyleName(style);
        return html;
    }
    
    /**
     * Creates a HTML label for the given value and applies style based on the sign.
     */
    public static HTML formatChange(double c) {
        HTML html = new HTML();
        String raw = changeFormat.format(c);
        String style = c >= 0 ? "positive" : "negative";
        html.setText(raw);
        html.addStyleName(style);
        return html;
    }
}
