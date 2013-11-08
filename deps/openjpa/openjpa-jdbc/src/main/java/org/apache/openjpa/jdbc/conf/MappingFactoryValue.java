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
package org.apache.openjpa.jdbc.conf;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.meta.MetaDataPlusMappingFactory;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.MetaDataFactory;

/**
 * Handles the complex logic of creating a {@link MetaDataFactory} for
 * combined metadata and mapping.
 *
 * @author Abe White
 * @nojavadoc
 */
public class MappingFactoryValue
    extends PluginValue {

    private static final Localizer _loc = Localizer.forPackage
        (MappingFactoryValue.class);

    private String[] _metaFactoryDefaults = null;
    private String[] _mappedMetaFactoryDefaults = null;

    public MappingFactoryValue(String prop) {
        super(prop, false);
    }

    /**
     * Default setting for a given <code>MetaDataFactory</code> alias setting.
     * If a <code>MappingFactory</code> value is not supplied, we check these
     * defaults against the <code>MetaDataFactory</code> setting. If the
     * <code>MetaDataFactory</code> does not have a default, we assume it
     * handles both metadata and mapping factory.
     */
    public void setMetaDataFactoryDefault(String metaAlias,
        String mappingAlias) {
        _metaFactoryDefaults = setAlias(metaAlias, mappingAlias,
            _metaFactoryDefaults);
    }

    /**
     * If the <code>Mapping</code> property is set, we check these defaults
     * before checking metadata factory defaults.
     */
    public void setMappedMetaDataFactoryDefault(String metaAlias,
        String mappingAlias) {
        _mappedMetaFactoryDefaults = setAlias(metaAlias, mappingAlias,
            _mappedMetaFactoryDefaults);
    }

    /**
     * Intantiate a {@link MetaDataFactory} responsible for both metadata and
     * mapping.
     */
    public MetaDataFactory instantiateMetaDataFactory(Configuration conf,
        PluginValue metaPlugin, String mapping) {
        return instantiateMetaDataFactory(conf, metaPlugin, mapping, true);
    }

    /**
     * Intantiate a {@link MetaDataFactory} responsible for both metadata and
     * mapping.
     */
    public MetaDataFactory instantiateMetaDataFactory(Configuration conf,
        PluginValue metaPlugin, String mapping, boolean fatal) {
        String clsName = getClassName();
        String props = getProperties();
        String metaClsName = metaPlugin.getClassName();
        String metaProps = metaPlugin.getProperties();

        // if no mapping factory set, check for default for this factory
        if (StringUtils.isEmpty(clsName)) {
            String def;
            if (!StringUtils.isEmpty(mapping)) {
                def = unalias(metaPlugin.alias(metaClsName),
                    _mappedMetaFactoryDefaults, true);
                if (def != null)
                    clsName = unalias(def);
            }
            if (StringUtils.isEmpty(clsName)) {
                def = unalias(metaPlugin.alias(metaClsName),
                    _metaFactoryDefaults, true);
                if (def != null)
                    clsName = unalias(def);
            }
        }

        // if mapping factory and metadata factory the same, combine
        // into metadata factory
        if (clsName != null && clsName.equals(metaClsName)) {
            if (props != null && metaProps == null)
                metaProps = props;
            else if (props != null)
                metaProps += "," + props;
            clsName = null;
            props = null;
        }

        // instantiate factories
        MetaDataFactory map = (MetaDataFactory) newInstance(clsName,
            MetaDataFactory.class, conf, fatal);
        MetaDataFactory meta;
        if (map != null
            && map.getClass().getName().indexOf("Deprecated") != -1) {
            // deprecated mapping factories take over metadata too, so we have
            // to special-case them to treat them like metadata factory only
            meta = map;
            map = null;
        } else {
            meta = (MetaDataFactory) metaPlugin.newInstance
                (metaClsName, MetaDataFactory.class, conf, fatal);
        }

        // configure factories.  if only meta factory, allow user to specify
        // its mapping properties in the mapping factory setting
        if (map == null && props != null) {
            if (metaProps == null)
                metaProps = props;
            else
                metaProps += ", " + props;
        }
        Configurations.configureInstance(map, conf, props,
            (fatal) ? getProperty() : null);
        Configurations.configureInstance(meta, conf, metaProps,
            (fatal) ? metaPlugin.getProperty() : null);

        Log log = conf.getLog(JDBCConfiguration.LOG_METADATA);
        if (log.isTraceEnabled()) {
            log.trace(_loc.get("meta-factory", meta));
            if (map != null)
                log.trace(_loc.get("map-factory", map));
        }

        // if no mapping setting, return meta factory alone, assuming it handles
        // both metadata and mapping
        MetaDataFactory ret = null;
        if(map == null ) { 
            ret = meta;
        }
        else {
            if( conf instanceof OpenJPAConfiguration) {
                ret = new MetaDataPlusMappingFactory(meta, map, (OpenJPAConfiguration) conf);
            }
            else {
                ret = new MetaDataPlusMappingFactory(meta, map);
            }
        }
        
        return ret; 
        
    }
}
