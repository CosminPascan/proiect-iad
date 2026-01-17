package aggregation;

import model.MonthlySummary;
import model.MonthlySummaryCollection;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class MonthlySummariesAggregationStrategy implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        MonthlySummary summary = newExchange.getIn().getBody(MonthlySummary.class);
        if (oldExchange == null) {
            MonthlySummaryCollection collection = new MonthlySummaryCollection();
            collection.addSummary(summary);
            newExchange.getIn().setBody(collection);
            return newExchange;
        } else {
            MonthlySummaryCollection collection = oldExchange.getIn().getBody(MonthlySummaryCollection.class);
            collection.addSummary(summary);
            return oldExchange;
        }
    }
}
