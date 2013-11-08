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
package org.apache.openjpa.persistence.validation;

import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.ValidationMode;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.persistence.JPAProperties;
import org.apache.openjpa.validation.AbstractValidator;
import org.apache.openjpa.validation.ValidationException;

public class ValidatorImpl extends AbstractValidator {
    
    private static final Localizer _loc = Localizer.forPackage(ValidatorImpl.class);

    private ValidatorFactory _validatorFactory = null;
    private Validator _validator = null;
    private ValidationMode _mode = ValidationMode.AUTO;
    private OpenJPAConfiguration _conf = null;
    private transient Log _log = null;
    
    // A map storing the validation groups to use for a particular event type
    private Map<Integer, Class<?>[]> _validationGroups = new HashMap<Integer,Class<?>[]>();
        
    // Lookup table for event to group property mapping 
    private static HashMap<String, Integer> _vgMapping = new HashMap<String, Integer> ();
            
    static {
        _vgMapping.put(JPAProperties.VALIDATE_PRE_PERSIST, LifecycleEvent.BEFORE_PERSIST);
        _vgMapping.put(JPAProperties.VALIDATE_PRE_REMOVE,  LifecycleEvent.BEFORE_DELETE);
        _vgMapping.put(JPAProperties.VALIDATE_PRE_UPDATE,  LifecycleEvent.BEFORE_UPDATE); 
    }

    /**
     * Default constructor.  Builds a default validator factory, if available
     * and creates the validator.
     * Returns an Exception if a Validator could not be created.
     */
    public ValidatorImpl() {
        initialize();
    }
    
    public ValidatorImpl(Configuration conf) {
        if (conf instanceof OpenJPAConfiguration) {
            _conf = (OpenJPAConfiguration)conf;
            _log = _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
            Object validatorFactory = _conf.getValidationFactoryInstance();
            String mode = _conf.getValidationMode();
            _mode = Enum.valueOf(ValidationMode.class, mode);
            if (validatorFactory != null) {
                if (validatorFactory instanceof ValidatorFactory) {
                    _validatorFactory = (ValidatorFactory)validatorFactory;
                } else {
                    // Supplied object was not an instance of a ValidatorFactory
                    throw new IllegalArgumentException(
                        _loc.get("invalid-factory").getMessage());                
                }
            }
        }
        initialize();
    }
    
    /**
     * Type-specific constructor
     * Returns an Exception if a Validator could not be created.
     * @param validatorFactory Instance of validator factory to use.  Specify
     *        null to use the default factory.
     * @param mode ValdiationMode enum value
     */
    public ValidatorImpl(ValidatorFactory validatorFactory,
        ValidationMode mode) {
        if (mode != null) {
            _mode = mode;
        }
        if (validatorFactory != null) {
            _validatorFactory = validatorFactory;
        }
        initialize();
    }

    /**
     * Common setup code factored out of the constructors
     */
    private void initialize() {
        // only try setting up a validator if mode is not NONE
        if (_mode != ValidationMode.NONE) {
            if (_validatorFactory == null) {
                _validatorFactory = getDefaultValidatorFactory();
            }
            if (_validatorFactory != null) {
                // use our TraversableResolver instead of BV provider one
                _validator = _validatorFactory.usingContext().
                    traversableResolver(new TraversableResolverImpl()).
                    getValidator();
            } else {
                // A default ValidatorFactory could not be created.
                throw new RuntimeException(
                    _loc.get("no-default-factory").getMessage());
            }
            
            // throw an exception if we have no Validator
            if (_validator == null) {
                // A Validator provider could not be created.
                throw new RuntimeException(
                    _loc.get("no-validator").getMessage());
            }

            if (_conf != null) {
                addValidationGroup(JPAProperties.VALIDATE_PRE_PERSIST, _conf.getValidationGroupPrePersist());
                addValidationGroup(JPAProperties.VALIDATE_PRE_UPDATE,  _conf.getValidationGroupPreUpdate());
                addValidationGroup(JPAProperties.VALIDATE_PRE_REMOVE,  _conf.getValidationGroupPreRemove());
            } else {
                // add in default validation groups, which can be over-ridden later
                addDefaultValidationGroups();
            }
        } else {
            // A Validator should not be created based on the supplied ValidationMode.
            throw new RuntimeException(
                _loc.get("no-validation").getMessage());
        }
    }
    
    /**
     * Add a validation group for the specific property.  The properties map
     * to a specific lifecycle event.  To disable validation for a group, set
     * the validation group to null.
     * 
     * @param validationGroupName
     * @param vgs
     */
    public void addValidationGroup(String validationGroupName, Class<?>...vgs) {
        Integer event = findEvent(validationGroupName);
        if (event != null) {
            _validationGroups.put(event, vgs);
            return;
        } else {
            // There were no events found for group "{0}".
            throw new IllegalArgumentException(
                _loc.get("no-group-events", validationGroupName).getMessage());
        }
    }
            
    /**
     * Add a validation group for a specified event.  Event definitions
     * are defined in LifecycleEvent.  To disable validation for a group, set
     * the validation group to null.
     * 
     * @param event
     * @param validationGroup
     */
    public void addValidationGroup(Integer event, Class<?>... validationGroup) {
        _validationGroups.put(event, validationGroup);        
    }
    
    /**
     * Add the validation group(s) for the specified event.  Event definitions
     * are defined in LifecycleEvent
     * @param event
     * @param group
     */
    public void addValidationGroup(String validationGroupName, String group) {
        Integer event = findEvent(validationGroupName);
        if (event != null) {
            Class<?>[] vgs = getValidationGroup(validationGroupName, group);
            if (vgs != null) {
                addValidationGroup(event, vgs);
            }
        } else {
            // There were no events found for group "{0}".
            throw new IllegalArgumentException(
                _loc.get("no-group-events", validationGroupName).getMessage());
        }
    }

    /**
     * Converts a comma separated list of validation groups into an array
     * of classes.
     * @param group
     * @return
     */
    private Class<?>[] getValidationGroup(String vgName, String group) {
        Class<?>[] vgGrp = null;
        if (group == null || group.trim().length() == 0) {
            return null;
        }
        String[] strClasses = group.split(",");
        if (strClasses.length > 0) {
            vgGrp = new Class<?>[strClasses.length];
            for (int i = 0; i < strClasses.length; i++) {
                try {
                    vgGrp[i] =  Class.forName(StringUtils.trim(strClasses[i]));
                } catch (Throwable t) {
                    throw new IllegalArgumentException(
                        _loc.get("invalid-validation-group", StringUtils.trim(strClasses[i]),
                            vgName).getMessage(), t);
                }
            }            
        }
        return vgGrp;
    }

    /**
     * Return the validation groups to be validated for a specified event
     * @param event Lifecycle event id
     * @return An array of validation groups
     */
    public Class<?>[] getValidationGroup(Integer event) {
        return _validationGroups.get(event);
    }
    
    /**
     * Returns whether the Validator is validating for the 
     * specified event.  Based on whether validation groups are specified for
     * the event.
     * 
     * @param event the event to check for validation
     * @return returns true if validating for this particular event
     */
    public boolean isValidating(Integer event) {
        return (_validationGroups.get(event) != null);
    }

    /**
     * Returns the validation constraints for the specified class
     * 
     * @param cls Class for which constraints to return
     * @return The validation bean descriptor
     */
    public BeanDescriptor getConstraintsForClass(Class<?> cls) {
        return _validator.getConstraintsForClass(cls);
    }

    /**
     * Validates a given instance
     * 
     * @param <T> The instance to validate
     * @param arg0 The class, of type T to validate
     * @param event The event id
     * @return ValidationException if the validator produces one or more
     *         constraint violations.
     */
    @Override
    public <T> ValidationException validate(T arg0, int event) { 
        if (!isValidating(event))
            return null;
        Set<ConstraintViolation<T>> violations = AccessController.doPrivileged(
                J2DoPrivHelper.validateAction(_validator, arg0, getValidationGroup(event)));

        if (violations != null && violations.size() > 0) {
            return new ValidationException(
                new ConstraintViolationException(
                    // A validation constraint failure occurred for class "{0}".
                    _loc.get("validate-failed",
                        arg0.getClass().getName()).getMessage(),
                    (Set)violations),
                true);
        }
        return null;
    }

    /**
     * Validates a property of a given instance
     * 
     * @param <T> The instance to validate
     * @param arg0 The property to validate
     * @param property The property to validate
     * @param event The event id
     * @return ValidationException if the validator produces one or more
     *         constraint violations.
     */
    @Override
    public <T> ValidationException validateProperty(T arg0, String property,
        int event) {
        if (!isValidating(event))
            return null;
        Set<ConstraintViolation<T>> violations = 
            _validator.validateProperty(arg0, property, 
                getValidationGroup(event));
        if (violations != null && violations.size() > 0) {
            return new ValidationException(
                new ConstraintViolationException(
                    // A validation constraint failure occurred for 
                    // property "{1}" in class "{0}".
                    _loc.get("valdiate-property-failed",
                        arg0.getClass().getName(),property).getMessage(),
                    (Set)violations),
                true);
        }
        return null;
    }

    /**
     * Validates a value based upon the constraints applied to a given class
     * attribute.
     * @param <T> The instance type to base validation upon
     * @param arg0 The class of type T to validate
     * @param arg1 The property to validate
     * @param arg2 The property value to validate
     * @param event The event id
     * @return ValidationException if the validator produces one or more
     *         constraint violations.
     */
    @Override
    public <T> ValidationException validateValue(Class<T> arg0, 
        String arg1, Object arg2, int event)  {
        if (!isValidating(event))
            return null;
        Set<ConstraintViolation<T>> violations = 
            _validator.validateValue(arg0, arg1, arg2, 
                getValidationGroup(event));
        if (violations != null && violations.size() > 0) {
            return new ValidationException(
                new ConstraintViolationException(
                    // A validation constraint failure occurred for 
                    // value "{2}" of property "{1}" in class "{0}".
                    _loc.get("validate-value-failed", arg0.getClass().getName(),
                        arg1, arg2.toString()).getMessage(),                    
                    (Set)violations),
                true);
        }
        return null;
    }

    /**
     * Returns whether validation is active for the given event.
     * 
     * @param <T>
     * @param arg0 Type being validated
     * @param event event type
     * @return true if validation is active for the specified event
     */
    @Override
    public <T> boolean validating(T arg0, int event) {
        // TODO: This method will also make a determination based upon which 
        // groups are validating and the group defined on the class
        return isValidating(event);
    }
    
    // Lookup the lifecycle event id for the validationProperty
    private Integer findEvent(String validationProperty) {
        return _vgMapping.get(validationProperty);
    }
    
    // Get the default validator factory
    private ValidatorFactory getDefaultValidatorFactory() {
        ValidatorFactory factory = null;
        try {
            factory = AccessController.doPrivileged(J2DoPrivHelper.buildDefaultValidatorFactoryAction());
        } catch (javax.validation.ValidationException e) {
            if (_log != null && _log.isTraceEnabled())
                _log.trace(_loc.get("factory-create-failed"), e);
        }
        return factory;
    }
    
    // Per JSR-317, the pre-persist and pre-update groups will validate using
    // the default validation group and pre-remove will not validate (no 
    // validation group)
    private void addDefaultValidationGroups() {
        addValidationGroup(JPAProperties.VALIDATE_PRE_PERSIST, javax.validation.groups.Default.class);
        addValidationGroup(JPAProperties.VALIDATE_PRE_UPDATE,  javax.validation.groups.Default.class);
    }
}
