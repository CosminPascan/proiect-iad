package model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "monthlySummaries")
@XmlAccessorType(XmlAccessType.FIELD)
public class MonthlySummaryCollection {
    @XmlElement(name = "monthlySummary")
    private List<MonthlySummary> monthlySummaries;

    public MonthlySummaryCollection() {
        monthlySummaries = new ArrayList<MonthlySummary>();
    }

    public void addSummary(MonthlySummary summary) {
        monthlySummaries.add(summary);
    }

    public List<MonthlySummary> getMonthlySummaries() {
        return monthlySummaries;
    }
}
