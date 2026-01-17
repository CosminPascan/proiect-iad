package aggregation;

import model.MonthlySummary;
import model.Order;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class MonthlySummaryAggregationStrategy implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            Order order = newExchange.getIn().getBody(Order.class);
            if (order != null) {
                MonthlySummary summary = new MonthlySummary(order.getMonthFromDate());
                summary.addOrderToTotal(order);
                newExchange.getIn().setBody(summary);
            }
            return newExchange;
        } else {
            MonthlySummary summary = oldExchange.getIn().getBody(MonthlySummary.class);
            Order order = newExchange.getIn().getBody(Order.class);
            if (order != null) {
                if (summary == null) {
                    summary = new MonthlySummary(order.getMonthFromDate());
                }
                summary.addOrderToTotal(newExchange.getIn().getBody(Order.class));
                oldExchange.getIn().setBody(summary);
            }
            return oldExchange;
        }
    }
}
