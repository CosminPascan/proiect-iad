package model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MonthlySummary {
    @XmlAttribute(name = "month")
    private String month;

    @XmlAttribute(name = "total")
    private double total;

    public MonthlySummary() {}

    public MonthlySummary(String month) {
        this();
        this.month = month;
    }

    public void addOrderToTotal(Order order) {
        if (order != null) {
            total += order.getAmount();
        }
    }

    public String getMonth() {
        return month;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "MonthlySummary{" +
                "month='" + month + '\'' +
                ", total=" + total +
                '}';
    }
}
