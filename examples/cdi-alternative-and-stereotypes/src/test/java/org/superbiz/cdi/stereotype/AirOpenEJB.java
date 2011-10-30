package org.superbiz.cdi.stereotype;

/**
 * @author rmannibucau
 *
 * without @Mock annotation which specifies this class as an alternative
 * you'll have this exception:
 *
 * Caused by: javax.enterprise.inject.AmbiguousResolutionException: There is more than one api type with : org.superbiz.cdi.stereotype.Society with qualifiers : Qualifiers: [@javax.enterprise.inject.Default()]
 * for injection into Field Injection Point, field name :  society, Bean Owner : [Journey, Name:null, WebBeans Type:ENTERPRISE, API Types:[org.superbiz.cdi.stereotype.Journey,java.lang.Object], Qualifiers:[javax.enterprise.inject.Any,javax.enterprise.inject.Default]]
 * found beans:
 * AirOpenEJB, Name:null, WebBeans Type:MANAGED, API Types:[org.superbiz.cdi.stereotype.Society,org.superbiz.cdi.stereotype.AirOpenEJB,java.lang.Object], Qualifiers:[javax.enterprise.inject.Any,javax.enterprise.inject.Default]
 * LowCostCompanie, Name:null, WebBeans Type:MANAGED, API Types:[org.superbiz.cdi.stereotype.Society,org.superbiz.cdi.stereotype.LowCostCompanie,java.lang.Object], Qualifiers:[javax.enterprise.inject.Any,javax.enterprise.inject.Default]
 *
 * because 2 implementations match the same injection point (Journey.society).
 */
@Mock
public class AirOpenEJB implements Society {
    @Override
    public String category() {
        return "simply the best";
    }
}
