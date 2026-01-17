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

public class ChannelRoutes extends RouteBuilder {
    @Override
    public void configure() throws JAXBException {
        config.JmsConfig.configure(getCamelContext());

        onException(Exception.class)
                .handled(true)
                .log("Error while processing orders: ${exception.message}")
                .to("file:data/out/errors");

        from("file:data/in?fileName=orders.csv&noop=true")
                .unmarshal().bindy(BindyType.Csv, Order.class)
                .setHeader("size", simple("${body.size}"))
                .split().body()

                //filtrare comenzi invalide
                .filter(simple("${body.amount} > 0"))
                .log("Valid order: ${body.orderId}")

                .setHeader("month", simple("${body.monthFromDate}"))
                .aggregate(header("month"), new MonthlySummaryAggregationStrategy())
                .completionTimeout(2000)
                .resequence(simple("${body.month}"))
                .aggregate(constant(true), new MonthlySummariesAggregationStrategy())
                .completionTimeout(2000)
                .marshal(new JaxbDataFormat(JAXBContext.newInstance(Order.class, MonthlySummary.class)))
                .to("jms:queue:monthlySummaries") // trimie un mesaj in coada -> publisher - consumer
                .log("Done");

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
    }
}
