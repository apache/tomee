
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "absoluteOrderingType", propOrder = {
    "nameOrOthers"
})
public class AbsoluteOrdering {

    @XmlElements({
        @XmlElement(name = "name", type = String.class),
        @XmlElement(name = "others", type = OrderingOthers.class)
    })
    protected List<Object> nameOrOthers;

    public List<Object> getNameOrOthers() {
        if (nameOrOthers == null) {
            nameOrOthers = new ArrayList<Object>();
        }
        return this.nameOrOthers;
    }

}
