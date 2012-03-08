package jug.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Vote {
    @Id
    @GeneratedValue
    private long id;

    @Enumerated(EnumType.STRING)
    private Value value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
