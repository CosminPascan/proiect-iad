package model;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@CsvRecord(separator = ",", skipFirstLine = true)
public class Order {

    @DataField(pos = 1)
    private int orderId;

    @DataField(pos = 2)
    private String customerName;

    @DataField(pos = 3)
    private String city;

    @DataField(pos = 4)
    private double amount;

    @DataField(pos = 5)
    private String date;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonthFromDate() {
        LocalDate date = LocalDate.parse(this.date);
        return date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
