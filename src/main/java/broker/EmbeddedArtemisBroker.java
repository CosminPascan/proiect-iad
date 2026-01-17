package broker;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

public class EmbeddedArtemisBroker {
    private static EmbeddedActiveMQ broker;

    public static void start() throws Exception {
        if (broker != null) return;

        var config = new ConfigurationImpl()
                .setPersistenceEnabled(false)
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("tcp", "tcp://127.0.0.1:61616");

        broker = new EmbeddedActiveMQ();
        broker.setConfiguration(config);
        broker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { broker.stop(); } catch (Exception ignored) {}
        }));
    }
}
