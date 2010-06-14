
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ordering-orderingType", propOrder = {
    "name",
    "others"
})
public class OrderingOrdering {

    protected List<String> name;
    protected OrderingOthers others;

    public List<String> getName() {
        if (name == null) {
            name = new ArrayList<String>();
        }
        return this.name;
    }

    public OrderingOthers getOthers() {
        return others;
    }

    public void setOthers(OrderingOthers value) {
        this.others = value;
    }

}
