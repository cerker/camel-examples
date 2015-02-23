package model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType( XmlAccessType.PROPERTY )
public class Order {

    private String orderNumber;

    public String getOrderNumber() {
        return orderNumber;
    }

    @XmlElement
    public void setOrderNumber( String orderNumber ) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber='" + orderNumber + '\'' +
                '}';
    }
}
