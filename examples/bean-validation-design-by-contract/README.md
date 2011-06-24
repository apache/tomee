# Bean Validation - Design By Contract

Bean Validation (aka JSR 303) contains an optional appendix dealing with method validation.

Some implementions of this JSR implement this appendix (Apache bval, Hibernate validator for example).

OpenEJB provides an interceptor which allows you to use this feature to do design by contract.

# Design by contract

The goal is to be able to configure with a finer grain your contract. In the example you specify
the minimum centimeters a sport man should jump at pole vaulting:

    @Local
    public interface PoleVaultingManager {
        int points(@Min(120) int centimeters);
    }

# Usage

To use this feature you have to add the BeanValidationAppendixInterceptor interceptor.

In unit test simply put a properties in your context properties:

    properties.setProperty(BeanContext.USER_INTERCEPTOR_KEY, BeanValidationAppendixInterceptor.class.getName());

In a production environment or in tomcat add the properties in conf/system.properties for example:

   org.apache.openejb.default.system.interceptors = org.apache.openejb.bval.BeanValidationAppendixInterceptor

# Errors

If a parameter is not validated an exception is thrown, it is an EJBException wrapping a ConstraintViolationException:

    try {
        gamesManager.addSportMan("I lose", "EN");
        fail("no space should be in names");
    } catch (EJBException wrappingException) {
        assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
        ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
        assertEquals(1, exception.getConstraintViolations().size());
    }
