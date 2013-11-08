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
import org.apache.openjpa.example.gallery.model.Image;

/**
 * Simple validator used to verify that image data is of a supported type
 */
public class ImageValidator implements ConstraintValidator<ImageConstraint, Image> {

    private List<ImageType> allowedTypes = null;
    /**
     * Configure the constraint validator based on the image
     * types it should support.
     * @param constraint the constraint definition
     */
    public void initialize(ImageConstraint constraint) {
        allowedTypes = Arrays.asList(constraint.value());
    }

    /**
     * Validate a specified value.
     */
    public boolean isValid(Image value, ConstraintValidatorContext context) {
        // JSR-303 best practice.  Promotes the use of @NotNull to perform
        // null checking.
        if (value == null) {
            return true;
        }
        
        // All these values will be pre-validated with @NotNull constraints
        // so they are safe to use
        byte[] data = value.getData();
        String fileName = value.getFileName();
        ImageType type = value.getType();
        
        // Verify the GIF type is correct, has the correct extension and
        // the data header is either GIF87 or GIF89
        if (allowedTypes.contains(ImageType.GIF) &&
            type == ImageType.GIF &&
            fileName.endsWith(".gif")) {
            if (data != null && data.length >= 6) {
                String gifHeader = new String(data, 0, 6);
                if (gifHeader.equalsIgnoreCase("GIF87a") ||
                     gifHeader.equalsIgnoreCase("GIF89a")) {
                    return true;
                }
            }
        }
        // Verify the JPEG type is correct, has the correct extension and
        // the data begins with SOI & ends with EOI markers
        if (allowedTypes.contains(ImageType.JPEG) &&
                value.getType() == ImageType.JPEG &&
                (fileName.endsWith(".jpg") ||
                 fileName.endsWith(".jpeg"))) {
            if (data.length >= 4 &&
                    data[0] == 0xff && data[1] == 0xd8 &&
                    data[data.length - 2] == 0xff &&
                    data[data.length - 1] == 0xd9) {
                return true;
            }
        }
        // Unknown file format
        return false;
    }
}
