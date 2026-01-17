package routes;

import aggregation.MonthlySummariesAggregationStrategy;
import aggregation.MonthlySummaryAggregationStrategy;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import model.MonthlySummary;
import model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

import java.util.ArrayList;
import java.util.List;

public class ChannelRoutes extends RouteBuilder {
    @Override
    public void configure() throws JAXBException {
        config.JmsConfig.configure(getCamelContext());

        onException(Exception.class)
                .handled(true)
                .log("Error while processing orders: ${exception.message}")
                .to("file:data/out/errors?autoCreate=true");

        from("file:data/in?fileName=orders.csv&noop=true")
                .unmarshal().bindy(BindyType.Csv, Order.class)
                .setHeader("size", simple("${body.size}"))
                .wireTap("direct:sortOrders")
                .split().body()

                //filtrare comenzi invalide
                .choice()
                .when(simple("${body.amount} <= 0"))
                .log("Invalid order (amount <= 0): ${body.orderId}")
                .to("jms:queue:invalidOrders")
                .stop()
                .when(simple("${body.customerName} == null || ${body.customerName} == ''"))
                .log("Invalid order (missing customer): ${body.orderId}")
                .to("jms:queue:invalidOrders")
                .stop()
                .end()

                .setHeader("month", simple("${body.monthFromDate}"))
                .aggregate(header("month"), new MonthlySummaryAggregationStrategy())
                .completionTimeout(2000)
                .resequence(simple("${body.month}"))
                .aggregate(constant(true), new MonthlySummariesAggregationStrategy())
                .completionTimeout(2000)
                .marshal(new JaxbDataFormat(JAXBContext.newInstance(Order.class, MonthlySummary.class)))
                .to("jms:queue:monthlySummaries") // trimie un mesaj in coada -> publisher - consumer
                .log("Done");

        from("direct:sortOrders")
                .routeId("sort-orders-by-id")
                // Filtrare comenzi valide (exclude invalide)
                .process(exchange -> {
                    List<Order> allOrders = exchange.getIn().getBody(List.class);
                    List<Order> validOrders = new ArrayList<>();

                    for (Order order : allOrders) {
                        // Păstrează doar comenzile valide
                        if (order.getAmount() > 0 &&
                                order.getCustomerName() != null &&
                                !order.getCustomerName().isEmpty()) {
                            validOrders.add(order);
                        }
                    }

                    // SORTARE după OrderID (crescător)
                    validOrders.sort((o1, o2) -> Integer.compare(o1.getOrderId(), o2.getOrderId()));

                    log.info("Sorted {} valid orders by OrderID", validOrders.size());

                    // CREARE CSV MANUAL
                    StringBuilder csv = new StringBuilder();
                    csv.append("orderId,customerName,city,amount,orderDate\n"); // header

                    for (Order order : validOrders) {
                        csv.append(order.getOrderId()).append(",")
                                .append(order.getCustomerName()).append(",")
                                .append(order.getCity()).append(",")
                                .append(order.getAmount()).append(",")
                                .append(order.getDate()).append("\n");
                    }

                    exchange.getIn().setBody(csv.toString());
                })
                .to("file:data/out/sorted?fileName=all-orders-sorted-by-id-${date:now:yyyyMMdd-HHmmss}.csv&autoCreate=true")
                .log("All valid orders saved (sorted by OrderID)");

        // canal consumer-publisher
        from("jms:queue:monthlySummaries")
                .routeId("queue-to-topic-monthly")
                .log("FROM QUEUE monthlySummaries: ${body}")
                .to("jms:topic:monthlySummaries.events");

        from("jms:topic:monthlySummaries.events")
                .routeId("topic-subscriber-audit")
                .log("AUDIT got monthly summaries")
                .to("file:data/out/audit?fileName=monthlySummaries-${date:now:yyyyMMdd-HHmmssSSS}.xml");

        from("jms:topic:monthlySummaries.events")
                .routeId("topic-subscriber-notify")
                .log("NOTIFY got monthly summaries")
                .to("file:data/out/notify?fileName=monthlySummaries-${date:now:yyyyMMdd-HHmmssSSS}.xml");

        from("jms:queue:invalidOrders")
                .routeId("invalid-orders-handler")
                .log("Processing invalid order: ${body}")
                .convertBodyTo(String.class)  // converteste Order la String
                .to("file:data/out/invalid?fileName=invalid-order-${date:now:yyyyMMdd-HHmmss}.txt");
    }
}
