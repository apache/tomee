/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.deployment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.webbeans.deployment.stereotype.IStereoTypeModel;
import org.apache.webbeans.util.Asserts;

/**
 * Manager for the {@link StereoTypeModel} instances.
 * <p>
 * It keeps map with name to model.
 * </p>
 * @version $Rev$ $Date$
 *
 */
public class StereoTypeManager
{
    /**Stereotype model instances, stereotype mode name to model instance*/
    private Map<String, IStereoTypeModel> stereoTypeMap = new ConcurrentHashMap<String, IStereoTypeModel>();

    /**
     * Default constructor
     */
    public StereoTypeManager()
    {

    }

    /**
     * Adds new steretype model instance.
     * @param model new model
     */
    public void addStereoTypeModel(IStereoTypeModel model)
    {
        Asserts.assertNotNull(model, "model parameter can not be null");

        stereoTypeMap.put(model.getName(), model);
    }

    /**
     * Returns model with given name.
     * @param modelName stereotype model name
     * @return model with given name
     */
    public IStereoTypeModel getStereoTypeModel(String modelName)
    {
        Asserts.assertNotNull(modelName, "modelName parameter can not be null");

        if (stereoTypeMap.containsKey(modelName))
        {
            return stereoTypeMap.get(modelName);
        }

        return null;
    }

}
