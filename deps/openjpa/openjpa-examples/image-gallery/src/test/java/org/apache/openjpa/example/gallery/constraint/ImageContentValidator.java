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
package org.apache.openjpa.example.gallery.constraint;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.openjpa.example.gallery.ImageType;

/**
 * Simple check that file format is of a supported type
 */
public class ImageContentValidator implements ConstraintValidator<ImageContent, byte[]> {

    private List<ImageType> allowedTypes = null;
    /**
     * Configure the constraint validator based on the image
     * types it should support.
     * @param constraint the constraint definition
     */
    public void initialize(ImageContent constraint) {
        allowedTypes = Arrays.asList(constraint.value());
    }

    /**
     * Validate a specified value.
     */
    public boolean isValid(byte[] value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        // Verify the GIF header is either GIF87 or GIF89
        if (allowedTypes.contains(ImageType.GIF)) {
            String gifHeader = new String(value, 0, 6);
            if (value.length >= 6 &&
                (gifHeader.equalsIgnoreCase("GIF87a") ||
                 gifHeader.equalsIgnoreCase("GIF89a"))) {
                return true;
            }
        }
        // Verify the JPEG begins with SOI & ends with EOI
        if (allowedTypes.contains(ImageType.JPEG)) {
            if (value.length >= 4 &&
                value[0] == 0xff && value[1] == 0xd8 &&
                value[value.length - 2] == 0xff &&
                value[value.length -1] == 0xd9) {
                return true;
            }
        }
        // Unknown file format
        return false;
    }
}
