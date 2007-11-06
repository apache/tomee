
package org.apache.openejb.jee.wls;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sql-query complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sql-query">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sql-shape-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sql" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="database-specific-sql" type="{http://www.bea.com/ns/weblogic/90}database-specific-sql" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sql-query", propOrder = {
    "sqlShapeName",
    "sql",
    "databaseSpecificSql"
})
public class SqlQuery {

    @XmlElement(name = "sql-shape-name")
    protected String sqlShapeName;
    protected String sql;
    @XmlElement(name = "database-specific-sql")
    protected List<DatabaseSpecificSql> databaseSpecificSql;

    /**
     * Gets the value of the sqlShapeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSqlShapeName() {
        return sqlShapeName;
    }

    /**
     * Sets the value of the sqlShapeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSqlShapeName(String value) {
        this.sqlShapeName = value;
    }

    /**
     * Gets the value of the sql property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSql() {
        return sql;
    }

    /**
     * Sets the value of the sql property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSql(String value) {
        this.sql = value;
    }

    /**
     * Gets the value of the databaseSpecificSql property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the databaseSpecificSql property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatabaseSpecificSql().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DatabaseSpecificSql }
     * 
     * 
     */
    public List<DatabaseSpecificSql> getDatabaseSpecificSql() {
        if (databaseSpecificSql == null) {
            databaseSpecificSql = new ArrayList<DatabaseSpecificSql>();
        }
        return this.databaseSpecificSql;
    }

}
