
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderingType", propOrder = {
    "after",
    "before"
})
public class Ordering {

    protected OrderingOrdering after;
    protected OrderingOrdering before;

    public OrderingOrdering getAfter() {
        return after;
    }

    public void setAfter(OrderingOrdering value) {
        this.after = value;
    }

    public OrderingOrdering getBefore() {
        return before;
    }

    public void setBefore(OrderingOrdering value) {
        this.before = value;
    }

}
