/**
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
package org.superbiz.enventries;

/**
 * With a java.beans.PropertyEditor, you can go way beyond the built-in
 * types that OpenEJB supports and can extend dependency injection to
 * just about anywhere.
 *
 * In the world of electric guitars, two types of pickups are used: humbucking, and single-coil.
 * Guitarists often refer to their guitars as HSS, meaning a guitar with 1 humbucker and
 * 2 single coil pickups, and so on.  This little PropertyEditor supports that shorthand notation.
 *
 * @version $Revision$ $Date$
 */
//START SNIPPET: code
public class PickupEditor extends java.beans.PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        text = text.trim();

        if (text.equalsIgnoreCase("H")) setValue(Pickup.HUMBUCKER);
        else if (text.equalsIgnoreCase("S")) setValue(Pickup.SINGLE_COIL);
        else throw new IllegalStateException("H and S are the only supported Pickup aliases");
    }
}
//END SNIPPET: code
