/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package openbook.client;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Utility to load and cache images.
 * Maintains a list of <em>known</em> images.
 * 
 * @author Pinaki Poddar
 *
 */
public class Images {
    
    private static Map<String, Icon> images = new HashMap<String, Icon>();
    
    public static Icon NEXT    = getIcon("images/arrow_right.jpg", 24, -1);
    public static Icon BACK    = getIcon("images/arrow_left.jpg",  24, -1);
    public static Icon DONE    = getIcon("images/done.png");
    public static Icon ERROR   = getIcon("images/error.png");
    public static Icon START   = getIcon("images/start_task.gif");
    
    public static final Icon BUY       = Images.getIcon("images/buy.jpg",       32, -1);
    public static final Icon DELIVERY  = Images.getIcon("images/delivery.jpg",  32, -1);
    public static final Icon SUPPLY    = Images.getIcon("images/supply.jpg",    32, -1);
    public static final Icon SEARCH    = Images.getIcon("images/search.jpg", 24, -1);
    public static final Icon CART      = Images.getIcon("images/cart.jpg",      24, -1);
    
    public static Icon VIEW_UNIT    = Images.getIcon("images/view_unit.jpg",   32, -1);
    public static Icon VIEW_DOMAIN  = Images.getIcon("images/view_domain.jpg", 32, -1);
    public static Icon VIEW_CODE    = Images.getIcon("images/view_code.jpg",   32, -1);
    public static Icon VIEW_QUERY   = Images.getIcon("images/view_query.jpg",  32, -1);
    
    public static final Icon    LOGO_OPENBOOKS = Images.getIcon("images/OpenBooks.jpg", 32, -1);
    public static final Icon    LOGO_OPENJPA   = Images.getIcon("images/openjpa-logo-small.png");
    
    
    public static Icon getIcon(String name) {
        Icon icon = images.get(name);
        if (icon == null) {
            icon = createImageIcon(name);
            images.put(name, icon);
        }
        return icon;
    }
    
    public static Icon getIcon(String name, boolean scale) {
        return getIcon(name, 32, -1);
    }
    
    public static Icon getIcon(String name, int width, int height) {
        Icon icon = getIcon(name);
        if (icon == null) {
            return null;
        }
        icon = new ImageIcon(((ImageIcon)icon).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        return icon;
    }
    
    /** 
     * Returns an ImageIcon, or null if the path was invalid. 
     * 
     **/
    protected static ImageIcon createImageIcon(String path) {
        if (path == null)
            return null;
        URL imgURL = Thread.currentThread().getContextClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            imgURL = Images.class.getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                System.err.println("Couldn't find file: " + path);
                return null;
            }
        }
    }

}
